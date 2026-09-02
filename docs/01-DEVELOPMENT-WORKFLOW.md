# Development Workflow

## Branching

Use trunk-based development with short-lived feature branches.

Examples:
- `feat/dam-sensor-registration`
- `feat/alert-acknowledgement`
- `fix/public-sensor-data-leak`

`main` is protected. No direct pushes.

## Pull request requirements

A PR must:
- reference its issue/task
- explain API/schema changes
- include tests
- include Bruno changes for API behavior
- include Flyway migration when schema changes
- pass CI
- receive at least one review

Changes to shared contracts require review from another domain owner.

## Database rule

Never manually edit shared/deployed databases.

All schema changes are:
```text
backend/src/main/resources/db/migration/V{n}__description.sql
```

Never edit an already-applied migration after it has been shared. Add a new migration.

## API version

All product APIs begin with:
`/api/v1`

Health/actuator endpoints are exempt.

## Standard HTTP semantics

- `200` success/read/update where appropriate
- `201` resource created
- `204` successful deletion/action with no body
- `400` invalid request
- `401` unauthenticated
- `403` authenticated but forbidden
- `404` resource not found
- `409` state conflict / duplicate conflict
- `422` optional for semantically invalid geometry/business requests; if the team prefers simpler semantics, standardize on 400 instead
- `500` unexpected server failure

## Error envelope

```json
{
  "code": "SENSOR_NOT_FOUND",
  "message": "Sensor does not exist.",
  "details": {},
  "requestId": "7f0c...",
  "timestamp": "2026-09-02T12:00:00Z"
}
```

Do not return Java stack traces or internal exception class names to clients.

## Pagination

```text
?page=0&size=20&sort=createdAt,desc
```

Response:
```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalItems": 0,
  "totalPages": 0
}
```

## Geometry

Requests/responses use GeoJSON geometry objects.

Example point:
```json
{
  "type": "Point",
  "coordinates": [80.1234, 7.1234]
}
```

GeoJSON coordinate order is longitude, latitude.

## Bruno ownership

One master collection lives in `/bruno`.

The developer implementing an endpoint is responsible for its Bruno tests.

Minimum test classes:
1. success path
2. unauthenticated
3. unauthorized
4. validation failure
5. cross-dam isolation where relevant
6. public/private leakage where relevant
7. state conflict/idempotency where relevant

## CI

Every PR should eventually run:
- backend compile
- backend unit tests
- backend integration tests
- Next.js lint/build/tests
- React Native lint/tests
- local integration stack
- Bruno smoke/API tests
- Docker image build

## Local development

Run code directly:
- Spring Boot directly
- Next.js directly
- React Native directly

Run infrastructure in Docker:
- PostGIS
- Mosquitto
- MinIO
- Redis only when used

## Secrets

Commit:
- `.env.example`
- non-secret local defaults

Never commit:
- private keys
- Firebase service account
- real SMS API key
- production DB credentials
- JWT signing secrets
