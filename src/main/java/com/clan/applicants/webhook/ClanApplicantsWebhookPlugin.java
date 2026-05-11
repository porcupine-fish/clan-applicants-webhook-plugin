package com.clan.applicants.webhook;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.io.IOException;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(
	name = "Clan Applicants Webhook",
	description = "Sends clan applicant game messages to a webhook URL"
)
public class ClanApplicantsWebhookPlugin extends Plugin
{
	private static final Pattern APPLICATION_PATTERN =
		Pattern.compile("^(.+?) has applied to join your clan\\.$");

	private static final MediaType JSON =
		MediaType.parse("application/json; charset=utf-8");

	@Inject
	private Client client;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Gson gson;

	@Inject
	private ClanApplicantsWebhookConfig config;

	@Provides
	ClanApplicantsWebhookConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanApplicantsWebhookConfig.class);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = event.getMessage();
		Matcher matcher = APPLICATION_PATTERN.matcher(message);

		if (!matcher.matches())
		{
			return;
		}

		String applicant = matcher.group(1).trim();

		sendApplication(applicant, message);
	}

	private void sendApplication(String applicant, String message)
	{
		String webhookUrl = config.webhookUrl();

		if (webhookUrl == null || webhookUrl.trim().isEmpty())
		{
			log.warn("Clan application webhook URL is not configured");
			postFailureMessage("Webhook URL not configured");
			return;
		}

		String seenBy = getSeenBy();

		ClanApplicantsWebhookPayload payload = new ClanApplicantsWebhookPayload(
			applicant,
			message,
			seenBy,
			Instant.now().toString()
		);

		RequestBody body = RequestBody.create(
			JSON,
			gson.toJson(payload)
		);

		Request request = new Request.Builder()
			.url(webhookUrl)
			.post(body)
			.header("Content-Type", "application/json")
			.header("User-Agent", "RuneLite-Clan-Application-Webhook")
			.build();

		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Failed to send clan application webhook", e);
				postFailureMessage(e.getClass().getSimpleName());
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (response)
				{
					if (!response.isSuccessful())
					{
						String responseBody = response.body() != null
							? response.body().string()
							: "No body";

						log.warn(
							"Clan application webhook failed. HTTP {} Body: {}",
							response.code(),
							responseBody
						);

						postFailureMessage("HTTP " + response.code());
					}
				}
				catch (Exception e)
				{
					log.warn("Failed reading clan application webhook response", e);
					postFailureMessage("Invalid response");
				}
			}
		});
	}

	private String getSeenBy()
	{
		Player localPlayer = client.getLocalPlayer();

		if (localPlayer != null && localPlayer.getName() != null && !localPlayer.getName().trim().isEmpty())
		{
			return localPlayer.getName().trim();
		}

		if (client.getUsername() != null && !client.getUsername().trim().isEmpty())
		{
			return client.getUsername().trim();
		}

		return "Unknown";
	}

	private void postFailureMessage(String error)
	{
		chatMessageManager.queue(
			QueuedMessage.builder()
				.type(ChatMessageType.GAMEMESSAGE)
				.runeLiteFormattedMessage(
					"<col=ff0000>Post to Clan Applications Webhook failure:</col> "
						+ error
				)
				.build()
		);
	}
}