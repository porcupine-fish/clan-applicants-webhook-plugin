# Clan Applicants Webhook Plugin
A tiny plugin to capture the game message:
- `PlayerX has applied to join your clan.`

It sends the notification to the URL specified in the plugin settings.

## Security
It's recommended to add a security key your webhook URL in the GET params.

## Error handling UX
The plugin will post a game message if there is a problem with the webhook URL specified, when an applicant applies.
It will tell you the HTTP error code.
