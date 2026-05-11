package com.clan.applicants.webhook;

import lombok.Value;

@Value
public class ClanApplicantsWebhookPayload
{
	String applicant;
	String message;
	String seenBy;
	String timestamp;
}