package com.clan.applicants.webhook;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ClanApplicants")
public interface ClanApplicantsWebhookConfig extends Config
{
	@ConfigItem(
		keyName = "webhookUrl",
		name = "Webhook URL",
		description = "URL to POST clan application messages to"
	)
	default String webhookUrl()
	{
		return "";
	}
}