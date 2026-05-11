# Clan Applicants Webhook Plugin
A tiny plugin to capture the game message:
- `PlayerX has applied to join your clan.`

It sends the notification to the URL specified in the plugin settings.

## JSON payload
```
{
  "applicant": "PlayerX",
  "message": "PlayerX has applied to join your clan.",
  "seenBy": "PlayerY",
  "timestamp": "2026-05-11T08:42:15.123Z"
}
```
- `seenBy` is the player who has the invitations open on the clan recruitment board.

## Security
It's recommended to add a security key your webhook URL in the GET params.

## Error handling UX
The plugin will post a game message if there is a problem with the webhook URL specified, when an applicant applies.
It will tell you the HTTP error code.
