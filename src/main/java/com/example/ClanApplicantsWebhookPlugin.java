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
import net.runelite.api.events.ChatMessage;
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
	name = "Clan Application Webhook",
	description = "Sends clan application game messages to a webhook URL"
)
public class ClanApplicationPlugin extends Plugin
{
	private static final Pattern APPLICATION_PATTERN =
		Pattern.compile("^(.+?) has applied to join your clan\\.$");

	private static final MediaType JSON =
		MediaType.parse("application/json; charset=utf-8");

	private final Gson gson = new Gson();

	@Inject
	private Client client;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private ClanApplicationConfig config;

	@Provides
	ClanApplicationConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanApplicationConfig.class);
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
			return;
		}

		ClanApplicationPayload payload = new ClanApplicationPayload(
			applicant,
			message,
			client.getUsername(),
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
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (response)
				{
					if (!response.isSuccessful())
					{
						log.warn("Clan application webhook returned HTTP {}", response.code());
					}
				}
			}
		});
	}
}