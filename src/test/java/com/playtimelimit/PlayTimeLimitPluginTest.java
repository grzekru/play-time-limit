package com.playtimelimit;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PlayTimeLimitPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PlayTimeLimitPlugin.class);
		RuneLite.main(args);
	}
}