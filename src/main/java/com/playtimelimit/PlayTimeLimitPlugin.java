package com.playtimelimit;

import com.google.inject.Provides;
import java.awt.Toolkit;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Play Time Limit",
	description = "Tracks daily playtime and reminds you to take a break after you hit your limit",
	tags = {"time", "limit", "break", "session"}
)
public class PlayTimeLimitPlugin extends Plugin
{
	private static final String BUILD_MARKER = "PTL_LOCAL_TOTAL_1_1_2";
	private static final String STATE_GROUP = "play-time-limit-state";
	private static final String STATE_DAY_KEY = "trackedDay";
	private static final String STATE_SECONDS_KEY = "dailyPlayedSeconds";
	private static final String STATE_TOTAL_SECONDS_KEY = "totalPlayedSeconds";
	private static final int MIN_PERSIST_INTERVAL_SECONDS = 10;
	private static final String FIRST_WARNING_PREFIX = "Limit reached";
	private static final String REMINDER_WARNING_PREFIX = "Still over limit";

	@Inject
	private Client client;

	@Inject
	private Notifier notifier;

	@Inject
	private PlayTimeLimitConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PlayTimeLimitOverlay overlay;

	private LocalDate trackedDay;
	private long dailyPlayedSeconds;
	private long totalPlayedSeconds;
	private long sessionPlayedSeconds;
	private long unsavedSeconds;
	private Instant lastAccrualTick;
	private Instant lastWarning;
	private boolean limitExceeded;
	private boolean flashOn;

	@Override
	protected void startUp()
	{
		log.debug("Play Time Limit started");
		log.info("Play Time Limit build marker: {}", BUILD_MARKER);
		configManager.setConfiguration(STATE_GROUP, "buildMarker", BUILD_MARKER);
		overlayManager.add(overlay);
		loadState();
		ensureToday();
	}

	@Override
	protected void shutDown()
	{
		log.debug("Play Time Limit stopped");
		overlayManager.remove(overlay);
		persistState(true);
		resetRuntimeState();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.LOGGED_IN)
		{
			if (lastAccrualTick == null)
			{
				sessionPlayedSeconds = 0;
			}
			lastAccrualTick = Instant.now();
			ensureToday();
		}

		if (gameState == GameState.LOGIN_SCREEN)
		{
			lastAccrualTick = null;
			flashOn = false;
			limitExceeded = false;
			persistState(true);
		}
	}

	@Schedule(period = 1, unit = ChronoUnit.SECONDS)
	public void checkPlayLimit()
	{
		ensureToday();

		if (!isLoggedIn())
		{
			return;
		}

		Instant now = Instant.now();
		if (lastAccrualTick == null)
		{
			lastAccrualTick = now;
			return;
		}

		long elapsedSeconds = Duration.between(lastAccrualTick, now).getSeconds();
		if (elapsedSeconds > 0)
		{
			dailyPlayedSeconds += elapsedSeconds;
			totalPlayedSeconds += elapsedSeconds;
			sessionPlayedSeconds += elapsedSeconds;
			unsavedSeconds += elapsedSeconds;
			lastAccrualTick = now;
			persistState(false);
		}

		long limitSeconds = Math.max(1, config.limitMinutes()) * 60L;
		limitExceeded = dailyPlayedSeconds >= limitSeconds;
		if (!limitExceeded)
		{
			flashOn = false;
			return;
		}

		long playedMinutes = dailyPlayedSeconds / 60;
		int limitMinutes = config.limitMinutes();
		if (lastWarning == null)
		{
			sendWarning(playedMinutes, limitMinutes, true);
			return;
		}

		long secondsFromLastWarning = Duration.between(lastWarning, now).getSeconds();
		long reminderIntervalSeconds = Math.max(1, config.reminderIntervalMinutes()) * 60L;

		if (secondsFromLastWarning >= reminderIntervalSeconds)
		{
			sendWarning(playedMinutes, limitMinutes, false);
		}
	}

	@Schedule(period = 500, unit = ChronoUnit.MILLIS)
	public void updateFlash()
	{
		if (!isLoggedIn() || !limitExceeded)
		{
			flashOn = false;
			return;
		}

		flashOn = !flashOn;
	}

	private void sendWarning(long playedMinutes, int limitMinutes, boolean firstWarning)
	{
		String prefix = firstWarning ? FIRST_WARNING_PREFIX : REMINDER_WARNING_PREFIX;
		String message = String.format("%s: played %d min today (limit %d min). Time for a break.", prefix, playedMinutes, limitMinutes);

		if (config.chatWarning())
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		}

		if (config.desktopNotification())
		{
			notifier.notify(message);
		}

		if (config.beep())
		{
			Toolkit.getDefaultToolkit().beep();
		}

		lastWarning = Instant.now();
		log.debug("Play limit warning sent: {}", message);
	}

	private boolean isLoggedIn()
	{
		return client.getGameState() == GameState.LOGGED_IN;
	}

	boolean shouldFlashRed()
	{
		return flashOn && limitExceeded && isLoggedIn();
	}

	private void resetRuntimeState()
	{
		lastAccrualTick = null;
		lastWarning = null;
		limitExceeded = false;
		sessionPlayedSeconds = 0;
		flashOn = false;
	}

	private void ensureToday()
	{
		LocalDate today = currentDay();
		if (trackedDay == null)
		{
			trackedDay = today;
			persistState(true);
			return;
		}

		if (!today.equals(trackedDay))
		{
			trackedDay = today;
			dailyPlayedSeconds = 0;
			unsavedSeconds = 0;
			lastWarning = null;
			limitExceeded = false;
			flashOn = false;
			persistState(true);
		}
	}

	private void loadState()
	{
		String dayValue = configManager.getConfiguration(STATE_GROUP, STATE_DAY_KEY);
		if (dayValue != null && !dayValue.isEmpty())
		{
			try
			{
				trackedDay = LocalDate.parse(dayValue);
			}
			catch (RuntimeException ex)
			{
				log.debug("Invalid stored day '{}', resetting state", dayValue, ex);
			}
		}

		String secondsValue = configManager.getConfiguration(STATE_GROUP, STATE_SECONDS_KEY);
		if (secondsValue != null && !secondsValue.isEmpty())
		{
			try
			{
				dailyPlayedSeconds = Math.max(0L, Long.parseLong(secondsValue));
			}
			catch (NumberFormatException ex)
			{
				log.debug("Invalid stored seconds '{}', resetting to 0", secondsValue, ex);
				dailyPlayedSeconds = 0L;
			}
		}

		String totalSecondsValue = configManager.getConfiguration(STATE_GROUP, STATE_TOTAL_SECONDS_KEY);
		if (totalSecondsValue != null && !totalSecondsValue.isEmpty())
		{
			try
			{
				totalPlayedSeconds = Math.max(0L, Long.parseLong(totalSecondsValue));
			}
			catch (NumberFormatException ex)
			{
				log.debug("Invalid stored total seconds '{}', resetting to 0", totalSecondsValue, ex);
				totalPlayedSeconds = 0L;
			}
		}

		long limitSeconds = Math.max(1, config.limitMinutes()) * 60L;
		limitExceeded = dailyPlayedSeconds >= limitSeconds;
	}

	private void persistState(boolean force)
	{
		if (!force && unsavedSeconds < MIN_PERSIST_INTERVAL_SECONDS)
		{
			return;
		}

		if (trackedDay != null)
		{
			configManager.setConfiguration(STATE_GROUP, STATE_DAY_KEY, trackedDay.toString());
		}
		configManager.setConfiguration(STATE_GROUP, STATE_SECONDS_KEY, Long.toString(dailyPlayedSeconds));
		configManager.setConfiguration(STATE_GROUP, STATE_TOTAL_SECONDS_KEY, Long.toString(totalPlayedSeconds));
		unsavedSeconds = 0;
	}

	long getTotalPlayedSeconds()
	{
		return totalPlayedSeconds;
	}

	long getDailyPlayedSeconds()
	{
		return dailyPlayedSeconds;
	}

	long getSessionPlayedSeconds()
	{
		return sessionPlayedSeconds;
	}

	long getClockPlayedSeconds()
	{
		return config.showTodayTotalOnClock() ? dailyPlayedSeconds : sessionPlayedSeconds;
	}

	int getDailyLimitMinutes()
	{
		return config.limitMinutes();
	}

	boolean shouldRenderClock()
	{
		return isLoggedIn();
	}

	private LocalDate currentDay()
	{
		return LocalDate.now(ZoneId.systemDefault());
	}

	@Provides
	PlayTimeLimitConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayTimeLimitConfig.class);
	}
}
