# EC2 deployment

The development workflow builds the application image, pushes it to ECR and deploys it to EC2 over SSH. MySQL and Redis remain private Docker services, the application binds to localhost, and Caddy is the only public container.

## Server prerequisites

- Ubuntu EC2 with Docker Engine, Docker Compose plugin and AWS CLI
- An EC2 instance role that can pull images from the selected ECR repository and put objects in the scan-image and backup S3 locations
- `/opt/ssok/infra/ec2/.env`, copied from `.env.example`, owned by the deployment user and mode `600`
- The GitHub Actions runner's SSH public key in the deployment user's `authorized_keys`

Do not make MySQL or Redis ports public. Persistent Docker volumes survive normal deployments; never run `docker compose down -v` unless deleting the data intentionally.

## GitHub environment

Create a `development` environment with:

- Secrets: `AWS_DEPLOY_ROLE_ARN`, `EC2_HOST`, `EC2_SSH_PRIVATE_KEY`, `EC2_KNOWN_HOSTS`
- Variables: `ECR_REPOSITORY`, `EC2_USER` (normally `ubuntu`)

The AWS role must trust GitHub's OIDC provider and should only permit pushing to the selected ECR repository. The server pulls ECR images using its own instance role, so no permanent AWS access key is stored in GitHub or on EC2.

For S3 image delivery, keep the bucket private and put CloudFront in front of it. Set `S3_PUBLIC_BASE_URL` to the CloudFront domain. The app stores that stable public URL in the database.
