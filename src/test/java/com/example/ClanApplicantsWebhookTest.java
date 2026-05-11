package com.clan.applicants.webhook;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClanApplicantsWebhookTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanApplicantsWebhookPlugin.class);
		RuneLite.main(args);
	}
}