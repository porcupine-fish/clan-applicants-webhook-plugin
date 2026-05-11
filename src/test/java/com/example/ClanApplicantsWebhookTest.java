package com.clan.applicants.webhook;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClanApplicantsWebhookTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanApplicantsWebhook.class);
		RuneLite.main(args);
	}
}