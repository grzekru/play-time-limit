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
		name = "Daily limit (minutes)",
		description = "Total play time allowed today before alerts start"
	)
	default int limitMinutes()
	{
		return 60;
	}

	@Range(min = 1, max = 60)
	@ConfigItem(
		keyName = "reminderIntervalMinutes",
		name = "Reminder every (minutes)",
		description = "How often to repeat alerts after you go over the limit"
	)
	default int reminderIntervalMinutes()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "chatWarning",
		name = "Chat warning",
		description = "Send warning messages to the game chat"
	)
	default boolean chatWarning()
	{
		return true;
	}

	@ConfigItem(
		keyName = "desktopNotification",
		name = "Desktop notification",
		description = "Show a desktop notification when over the limit"
	)
	default boolean desktopNotification()
	{
		return true;
	}

	@ConfigItem(
		keyName = "beep",
		name = "Sound alert",
		description = "Play a short system beep with each warning"
	)
	default boolean beep()
	{
		return true;
	}
}
