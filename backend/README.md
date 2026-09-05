# DDAS Backend

The backend is a Java 21, Maven, Spring Boot modular monolith. The shared
foundation currently provides JWT authentication, dam-scoped authorization,
GeoJSON/PostGIS helpers, audit persistence, Flyway migrations, and health
checks.

## Profiles

- `standalone` is the default. It starts without PostgreSQL so the application
  and `/actuator/health` can be checked quickly. Business APIs are denied.
- `local` connects to the Docker development infrastructure and loads
  deterministic local fixtures.
- `ci` connects to the CI PostgreSQL service and loads deterministic CI
  fixtures.

## Start locally

From the repository root, first copy the example environment files and start
the infrastructure as described in `docs/14-LOCAL-DEVELOPMENT-SETUP.md`.

PowerShell:

```powershell
Set-Location backend
.\run-local.ps1
```

Bash:

```bash
cd backend
./run-local.sh
```

Health is available at `http://localhost:8080/actuator/health`.

## Foundation API

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`
- `GET /api/v1/dams`
- `POST /api/v1/dams` (`SUPER_ADMIN` only)
- `GET /api/v1/dams/{damId}`
- `GET /api/v1/dams/{damId}/staff`

## Dam monitoring API

- Dam updates/state: `PUT /api/v1/dams/{damId}`, `PATCH /api/v1/dams/{damId}/state`
- Sensors: `/api/v1/dams/{damId}/sensors`
- Sensor history: `/api/v1/dams/{damId}/sensors/{sensorId}/readings`
- Latest telemetry: `/api/v1/dams/{damId}/telemetry/latest`
- Authenticated SSE: `/api/v1/dams/{damId}/telemetry/stream`
- Gates: `/api/v1/dams/{damId}/gates`
- IoT devices: `/api/v1/dams/{damId}/iot-devices`
- Public dams/maps/sensors: `/api/v1/public/dams/**`

MQTT ingestion subscribes at QoS 1 to
`dams/+/devices/+/telemetry` when `MQTT_ENABLED=true`. It validates the topic's
dam/device identity, device lifecycle, sensor assignment, value/timestamp shape,
and `messageId` idempotency before committing readings.

Local fixture accounts all use the password configured by
`APP_FIXTURE_PASSWORD` (the example local value is `ChangeMe123!`):

| Account | Access |
| --- | --- |
| `admin@example.test` | platform `SUPER_ADMIN` |
| `civilian@example.test` | platform `CIVILIAN`, no dam staff access |
| `operator@example.test` | Dam A operator |
| `engineer@example.test` | Dam A engineer |
| `admin-b@example.test` | Dam B administrator |

Dam A is `00000000-0000-0000-0000-000000000001`; Dam B is
`00000000-0000-0000-0000-000000000002`.

These identities are development fixtures, not production credentials.

## Shared application contracts

Other modules should inject these contracts rather than reading auth or dam
repositories directly:

- `CurrentUserProvider` supplies the authenticated, active user.
- `DamReader` supplies authoritative dam data.
- `DamAccessChecker` enforces live dam membership and roles.
- `GeometryMapper` validates GeoJSON coordinate order and creates SRID 4326
  JTS geometries.
- `AuditService` records security-sensitive and state-changing operations in
  `audit_log` in the caller's transaction.

## Verification

```bash
./mvnw test
cd ../bruno
npm ci
npm run test:local:smoke
```

The Bruno foundation suite is intentionally smaller than the complete V1
contract. Each domain owner enables and expands their domain tests as its APIs
are implemented.
