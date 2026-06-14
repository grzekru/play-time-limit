package com.playtimelimit;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("play-time-limit")
public interface PlayTimeLimitConfig extends Config
{
	@Range(min = 1, max = 1440)
	@ConfigItem(
		keyName = "limitMinutes",
		name = "Daily total limit (minutes)",
		description = "Total time played today before alerts and red flashing start"
	)
	default int limitMinutes()
	{
		return 60;
	}

	@Range(min = 1, max = 60)
	@ConfigItem(
		keyName = "reminderIntervalMinutes",
		name = "Reminder interval (minutes)",
		description = "How often to repeat alerts after crossing the limit"
	)
	default int reminderIntervalMinutes()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "chatWarning",
		name = "Show chat warning",
		description = "Show warning message in chat after crossing the limit"
	)
	default boolean chatWarning()
	{
		return true;
	}

	@ConfigItem(
		keyName = "desktopNotification",
		name = "Desktop notification",
		description = "Show desktop notification after crossing the limit"
	)
	default boolean desktopNotification()
	{
		return true;
	}

	@ConfigItem(
		keyName = "beep",
		name = "Play beep",
		description = "Play a system beep for stronger alerting"
	)
	default boolean beep()
	{
		return true;
	}
}
