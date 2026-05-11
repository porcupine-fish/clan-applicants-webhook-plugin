package com.clan.applicants.webhook;

public class ClanApplicantsWebhookPayload
{
	public final String applicant;
	public final String message;
	public final String seenBy;
	public final String timestamp;

	public ClanApplicantsWebhookPayload(
		String applicant,
		String message,
		String seenBy,
		String timestamp
	)
	{
		this.applicant = applicant;
		this.message = message;
		this.seenBy = seenBy;
		this.timestamp = timestamp;
	}
}