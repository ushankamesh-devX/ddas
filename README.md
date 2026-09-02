# Dam Disaster Alert System — V1 Starter

A modular-monolith disaster monitoring and public alert platform for dams.

## Product surfaces

- **Spring Boot backend** — REST API, security, MQTT ingestion, alert workflows, GIS rules.
- **Next.js admin web** — dam operations, live telemetry, mapping, alerts, evacuation configuration, reports/news.
- **React Native mobile app** — public dam status, public sensors, emergency alerts, evacuation, reports, news, household profile.
- **PostgreSQL + PostGIS** — source of truth and geospatial model.
- **Eclipse Mosquitto** — MQTT broker for sensors/gateways.
- **MinIO** — object storage for civilian report photos.
- **Redis** — intentionally optional in V1; add only when a measured need appears.
- **Bruno** — master black-box API collection and CI regression tests.
- **Docker Compose** — local infrastructure and VM deployment.
- **GitHub Actions** — CI baseline.

## Team ownership

1. **Dev 1 — Dam & Monitoring**
2. **Dev 2 — Alerts & Notifications**
3. **Dev 3 — Evacuation & Emergency Response**
4. **Dev 4 — Community & Information**

All four are full-stack developers. Ownership means "primary accountable person", not exclusive access.

## Start here

1. Read `docs/00-MASTER-DESIGN.md`.
2. Read `docs/01-DEVELOPMENT-WORKFLOW.md`.
3. Each developer reads their file under `docs/dev-plans/`.
4. Start infrastructure:
   ```bash
   docker compose -f infra/docker-compose.dev.yml up -d
   ```
5. Run Spring Boot, Next.js and React Native directly during development.
6. Open `bruno/` as the shared Bruno collection.
7. No API feature is complete until its Bruno tests pass and API documentation is updated.

## Contract rules

- API prefix: `/api/v1`
- IDs: UUID
- Time: UTC ISO-8601
- DB schema changes: Flyway only
- API geometry: GeoJSON
- Database geometry: PostGIS SRID 4326
- Source of truth: PostgreSQL
- Images/files: MinIO/object storage, not PostgreSQL blobs
- Critical alerts: persist first, notify second via transactional outbox
- Alert severity and emergency operational state are related but not identical concepts
- Public/private operational data is explicit, never inferred

This starter intentionally avoids Kubernetes, Kafka, CQRS, event sourcing, Elasticsearch and microservices.


## IoT credentials

V1 MQTT devices use a unique rotatable `deviceId + deviceKey`. The key authenticates the MQTT connection and is never embedded in telemetry payloads. See `docs/13-IOT-DEVICE-AUTH.md`.

## Full V1 black-box suite

See `bruno/FULL-V1-SUITE.md`. HTTP contracts live in Bruno; MQTT broker auth/ACL tests are defined separately under the IoT folder and should run in CI with an MQTT CLI/client.
