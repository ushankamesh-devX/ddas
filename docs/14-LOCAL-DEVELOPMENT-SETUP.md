# Local Development Setup

This guide takes a new developer from a clean clone to a running local stack with PostgreSQL/PostGIS, Mosquitto, MinIO, the Spring Boot backend, and the admin web application.

## Prerequisites

Install:

- Git
- Java 21
- Node.js 22 and npm
- Docker Desktop on Windows/macOS, or Docker Engine with Compose on Linux

Maven does not need to be installed globally. The repository includes the Maven Wrapper.

Verify the prerequisites:

```bash
java -version
node --version
npm --version
docker version
docker compose version
```

Java must report version 21. Node must report version 22. Docker must report both a client and a running server/engine.

## 1. Create local environment files

The committed `.env.example` files contain local-development defaults only. The copied `.env` files are ignored by Git.

PowerShell:

```powershell
Copy-Item infra/.env.example infra/.env
Copy-Item backend/.env.example backend/.env
```

Bash:

```bash
cp infra/.env.example infra/.env
cp backend/.env.example backend/.env
```

The corresponding values must agree across the two files:

| Infrastructure | Backend | Purpose |
| --- | --- | --- |
| `POSTGRES_USER` | `DB_USERNAME` | PostgreSQL login |
| `POSTGRES_PASSWORD` | `DB_PASSWORD` | PostgreSQL password |
| `MINIO_ROOT_USER` | `MINIO_ACCESS_KEY` | Local MinIO access key |
| `MINIO_ROOT_PASSWORD` | `MINIO_SECRET_KEY` | Local MinIO secret key |

The local Mosquitto configuration currently has `allow_anonymous true`, so `MQTT_USERNAME` and `MQTT_PASSWORD` are intentionally empty. This is for local development only.

## 2. Start local infrastructure

From the repository root:

```bash
docker compose -f infra/docker-compose.dev.yml pull
docker compose -f infra/docker-compose.dev.yml up -d
docker compose -f infra/docker-compose.dev.yml ps
```

The first command downloads the required images. The second command creates the containers, network, and persistent named volumes.

Expected services:

| Service | Address | Expected state |
| --- | --- | --- |
| PostgreSQL/PostGIS | `localhost:5432` | `healthy` |
| Mosquitto | `localhost:1883` | running |
| MinIO API | `http://localhost:9000` | running |
| MinIO console | `http://localhost:9001` | running |

If PostgreSQL is still starting, follow its logs:

```bash
docker compose -f infra/docker-compose.dev.yml logs -f postgres
```

Continue after the log contains `database system is ready to accept connections`. Press `Ctrl+C` to stop following the logs; this does not stop the container.

Verify PostgreSQL:

```bash
docker compose -f infra/docker-compose.dev.yml exec postgres pg_isready -U damapp -d dam_alert
```

Expected result:

```text
/var/run/postgresql:5432 - accepting connections
```

Verify MinIO:

```bash
curl -i http://localhost:9000/minio/health/live
```

The response must have HTTP status `200`.

## 3. Download backend dependencies and run tests

PowerShell:

```powershell
Set-Location backend
.\mvnw.cmd clean test
```

Bash:

```bash
cd backend
./mvnw clean test
```

The wrapper downloads Maven and the declared dependencies on the first run. The command must end with `BUILD SUCCESS`.

## 4. Start the backend with local infrastructure

The local launchers load `backend/.env` and activate the `local` Spring profile.

PowerShell:

```powershell
.\run-local.ps1
```

Bash:

```bash
./run-local.sh
```

On the first successful startup, Flyway applies `V1__initial_schema.sql` and
`V2__refresh_tokens.sql`, and Hibernate validates the resulting schema. The log
must eventually contain `Started DdasApplication`.

Keep this terminal running while testing the application.

Running `mvnw spring-boot:run` without the local launcher uses the database-free `standalone` profile. Use the local launcher for database feature development.

## 5. Verify backend health

From another terminal:

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

Spring Boot may also include `liveness` and `readiness` groups.

## 6. Verify Flyway and PostGIS

Run these commands from the repository root.

Verify the migration:

```bash
docker compose -f infra/docker-compose.dev.yml exec postgres psql -U damapp -d dam_alert -c "SELECT version, description, success FROM flyway_schema_history;"
```

Expected successful rows:

```text
1 | initial schema | t
2 | refresh tokens | t
```

Verify required PostgreSQL extensions:

```bash
docker compose -f infra/docker-compose.dev.yml exec postgres psql -U damapp -d dam_alert -c "SELECT extname FROM pg_extension ORDER BY extname;"
```

The result must include `pgcrypto`, `postgis`, and `plpgsql`. PostGIS may also create `tiger` and `topology` schemas; those are PostGIS support objects, not application tables.

List only application-schema tables:

```bash
docker compose -f infra/docker-compose.dev.yml exec postgres psql -U damapp -d dam_alert -c "\dt public.*"
```

The result must include `app_user`, `dam`, `sensor`, `sensor_reading`, `alert`,
`notification_outbox`, `refresh_token`, and `flyway_schema_history`, along with
the other application tables.

## 7. Verify deterministic local fixtures

The `local` profile creates these records idempotently. All accounts use the
`APP_FIXTURE_PASSWORD` value from `backend/.env`.

| Account | Expected access |
| --- | --- |
| `admin@example.test` | all dams as `SUPER_ADMIN` |
| `civilian@example.test` | no private dam access |
| `operator@example.test` | Dam A only |
| `engineer@example.test` | Dam A only |
| `admin-b@example.test` | Dam B only |

Dam A is `00000000-0000-0000-0000-000000000001`; Dam B is
`00000000-0000-0000-0000-000000000002`.

## 8. Verify Mosquitto

Start a subscriber in one terminal:

```bash
docker compose -f infra/docker-compose.dev.yml exec mosquitto mosquitto_sub -h localhost -p 1883 -t "ddas/test" -v
```

Publish from a second terminal:

```bash
docker compose -f infra/docker-compose.dev.yml exec mosquitto mosquitto_pub -h localhost -p 1883 -t "ddas/test" -m "hello"
```

The subscriber must receive:

```text
ddas/test hello
```

## 9. Verify MinIO

Open `http://localhost:9001` and sign in with `MINIO_ROOT_USER` and `MINIO_ROOT_PASSWORD` from `infra/.env`.

The backend currently contains MinIO connection configuration but does not yet create the application bucket automatically. Bucket provisioning should be added with the storage integration.

## 10. Install and start the admin web application

From the repository root, create the local frontend environment file and install dependencies:

PowerShell:

```powershell
Copy-Item admin-web/.env.example admin-web/.env.local
Set-Location admin-web
npm install
npm run dev
```

Bash:

```bash
cp admin-web/.env.example admin-web/.env.local
cd admin-web
npm install
npm run dev
```

Open `http://localhost:3000`. Keep this terminal running during frontend development. The local frontend sends API requests to `http://localhost:8080` through `NEXT_PUBLIC_API_BASE_URL`.

Before sharing changes, verify the frontend:

```bash
npm run lint
npm run build
```

## 11. Run the backend black-box foundation gate

Keep the backend running, then use another terminal:

```bash
cd bruno
npm ci
npm run test:local:smoke
```

The expected summary is `10 (10 Passed)`. The suite verifies the running
application rather than mocks: health, authentication, refresh rotation,
current-user identity, and same-dam/cross-dam authorization.

## Second and subsequent startups

Do not recreate the `.env` files, pull the images, or initialize the database again. Docker and PostgreSQL reuse the existing named volumes.

Start Docker Desktop or Docker Engine first.

In terminal 1, start the infrastructure from the repository root.

PowerShell:

```powershell
Set-Location D:\Git\ddas
docker compose -f infra/docker-compose.dev.yml up -d
docker compose -f infra/docker-compose.dev.yml ps
```

Bash:

```bash
cd /path/to/ddas
docker compose -f infra/docker-compose.dev.yml up -d
docker compose -f infra/docker-compose.dev.yml ps
```

In terminal 2, start the backend.

PowerShell:

```powershell
Set-Location D:\Git\ddas\backend
.\run-local.ps1
```

Bash:

```bash
cd /path/to/ddas/backend
./run-local.sh
```

Flyway checks `flyway_schema_history`, recognizes every applied migration, and
only runs newer migrations. It does not recreate the schema.

Verify the backend from a separate terminal:

```bash
curl http://localhost:8080/actuator/health
```

The response must report `"status":"UP"`.

In terminal 3, start the admin web application. Dependencies only need to be installed again when `package-lock.json` changes.

PowerShell:

```powershell
Set-Location D:\Git\ddas\admin-web
npm run dev
```

Bash:

```bash
cd /path/to/ddas/admin-web
npm run dev
```

Open `http://localhost:3000`.

## Stopping local development

Stop the backend with `Ctrl+C`.

Stop and remove the containers while preserving named-volume data:

```bash
docker compose -f infra/docker-compose.dev.yml down
```

Do not use `down -v` during normal development. The `-v` option permanently deletes the local PostgreSQL, Mosquitto, and MinIO volumes.

## Troubleshooting

### PostgreSQL password authentication failed

First confirm that `POSTGRES_USER`/`POSTGRES_PASSWORD` in `infra/.env` match `DB_USERNAME`/`DB_PASSWORD` in `backend/.env`.

PostgreSQL applies its initialization credentials only when creating an empty data volume. If the volume was created with an older password and contains no data that must be preserved, reset the complete disposable local stack:

```bash
docker compose -f infra/docker-compose.dev.yml down -v
docker compose -f infra/docker-compose.dev.yml up -d
```

This deletes all local infrastructure data. Never use it for a shared or non-disposable environment.

### Docker returns a pipe, daemon, or HTTP 500 error

Confirm that Docker Desktop is running in Linux-container mode. Restart Docker Desktop, wait for its engine to become ready, and rerun `docker version` before starting Compose.

### A port is already in use

Check ports `5432`, `1883`, `9000`, `9001`, `8080`, and `3000`. Stop the conflicting local process or deliberately update the relevant service configuration.
