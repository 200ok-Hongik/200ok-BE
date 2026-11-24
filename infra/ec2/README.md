# EC2 deployment

The development workflow builds the application image, pushes it to ECR and deploys it to EC2 through AWS Systems Manager. MySQL, Redis and RabbitMQ remain private Docker services, the application binds to localhost, and Caddy is the only public container. Runtime configuration is kept in the server-only `.env` file; Compose does not duplicate application settings.

## Server prerequisites

- Ubuntu EC2 with Docker Engine, Docker Compose plugin and AWS CLI
- An EC2 instance role that can pull images from the selected ECR repository and put objects in the scan-image and backup S3 locations
- `/opt/ssok/infra/ec2/.env`, copied from `.env.example`, owned by the deployment user and mode `600`

Do not make MySQL, Redis or RabbitMQ ports public. Persistent Docker volumes survive normal deployments; never run `docker compose down -v` unless deleting the data intentionally.

## GitHub environment

Create a `development` environment with:

- Secret: `AWS_DEPLOY_ROLE_ARN`
- Variables: `ECR_REPOSITORY`, `EC2_INSTANCE_ID`

The AWS role must trust GitHub's OIDC provider and should only permit pushing to the selected ECR repository and sending deployment commands to the selected EC2 instance. The server pulls ECR images using its own instance role, so no permanent AWS access key or SSH private key is stored in GitHub or on EC2.

For S3 image delivery, keep the bucket private and put CloudFront in front of it. Set `S3_PUBLIC_BASE_URL` to the CloudFront domain. The app stores that stable public URL in the database.

## Files

- `compose.yml`: production services (app, MySQL, Redis, RabbitMQ and Caddy)
- `.env.example`: complete runtime-variable template; the real `.env` stays untracked
- `deploy.sh`: validates the environment, pulls the selected ECR image and reconciles the services
- `backup.sh`: uploads MySQL backups to the configured private S3 location

## FCM push

Enable the Firebase Cloud Messaging API in the Firebase project used by the client. The backend needs a service account authorized to send messages in that project.

Set `NOTIFICATION_PUSH_PROVIDER=fcm` in the server-only `.env`. Put the service-account JSON, encoded as a single base64 line, in `FIREBASE_CREDENTIALS_BASE64` in the same file. On Linux, `base64 -w0 /path/to/service-account.json` produces the required value. Treat it as a private key. Recreate the app container through the usual deployment process to apply the settings. For local runs, `GOOGLE_APPLICATION_CREDENTIALS` can point to the JSON file instead.

The client should send its Firebase project’s FCM registration token with the current user's bearer token:

| When | Request | JSON body |
| --- | --- | --- |
| Login or FCM token refresh | `POST /api/notifications/devices` | `{"token":"<FCM token>","deviceType":"WEB"}` |
| Logout, before discarding the bearer token | `DELETE /api/notifications/devices` | `{"token":"<FCM token>"}` |
| Verify push delivery | `POST /api/notifications/test` | Optional `{"itemName":"투명 페트병"}` |
| Load the last 10 reminders | `GET /api/notifications/recent` | |
| Change reminder preferences | `PATCH /api/users/me/notifications` | `{"enabled":true}` |

Use `WEB`, `ANDROID`, or `IOS` as the device type. On token rotation, remove the previous token if it is available. The test endpoint returns the number of devices for which FCM accepted the message. It does not create an inbox entry. Scheduled reminders run at 09:00 Asia/Seoul by default for incomplete disposal schedules due that day and respect the user's notification preference. Push data includes `type` and, for scheduled reminders, `calendarId` and `disposalDecisionId` when available.
