# Master System Design — Dam Disaster Alert System V1

## 1. Purpose

The system monitors dam telemetry in real time, gives dam operators an operational dashboard, provides civilians with public dam information, and supports reliable alerting and evacuation guidance during emergencies.

The V1 goal is not to model every possible disaster-management workflow. The goal is a reliable end-to-end path:

**sensor → MQTT → backend → persisted telemetry → operator dashboard → emergency alert → mobile notification → evacuation map**

## 2. Architecture decision

Use a **modular monolith** Spring Boot backend.

```text
Sensors/Gateways
      |
    MQTT
      v
 Eclipse Mosquitto
      |
      v
+--------------------------+
| Spring Boot              |
|--------------------------|
| auth                     |
| dam-monitoring           |
| alerts                   |
| evacuation               |
| community                |
| notification             |
| audit                    |
+------------+-------------+
             |
   +---------+----------+--------------+
   |                    |              |
PostgreSQL/PostGIS    MinIO          Redis*
source of truth       photos         optional
```

`*` Redis is not required until a concrete need is demonstrated.

### Why not microservices?

The team has four developers. Microservices would increase deployment, networking, transactions, observability and testing overhead without solving a current scaling problem.

The backend should remain internally modular so a high-throughput component, such as telemetry ingestion, can be extracted later if justified.

## 3. Production deployment

Initial production target: one VM with Docker Compose.

```text
Internet
   |
HTTPS
   v
Reverse Proxy
   |
   +--> Next.js container
   |
   +--> Spring Boot container
            |
            +--> PostGIS container
            +--> Mosquitto container
            +--> MinIO container
            +--> Redis container (optional)
```

Only ports required by users/sensors are exposed. PostgreSQL, Redis and MinIO admin ports remain private.

Docker provides portability. It does not by itself provide scalability. The backend must remain stateless so replicas can be added later.

## 4. Business domains

### 4.1 Dam & Monitoring

Owns:
- dams
- dam operational state
- sensors
- sensor readings
- gates
- sensor placement
- live telemetry
- public/private sensor exposure
- sensor health / last-seen state

### 4.2 Alerts & Notifications

Owns:
- alert creation
- severities
- affected risk zones
- recipient resolution
- notification outbox
- push delivery
- acknowledgements
- alert history
- delivery status

### 4.3 Evacuation & Emergency Response

Owns:
- risk zones
- safe locations
- evacuation routes
- zone-route assignments
- route status
- emergency operational state
- public evacuation map
- offline evacuation snapshot contract

### 4.4 Community & Information

Owns:
- civilian accounts/households
- household members
- dam association
- public news
- citizen reports
- report photos
- report verification/publication
- public feed

## 5. Shared platform concerns

Shared entities/contracts must be reviewed by at least one developer outside the owning domain:

- user identity
- roles/permissions
- dam identity
- audit log
- API error envelope
- pagination
- GeoJSON conventions
- notification device tokens
- schema naming
- common timestamps

## 6. Roles

Initial roles:

- `SUPER_ADMIN`
- `DAM_ADMIN`
- `DAM_ENGINEER`
- `DAM_OPERATOR`
- `FIELD_OFFICER`
- `CIVILIAN`

Permissions are dam-scoped where applicable. A `DAM_ENGINEER` for Dam A must not automatically operate Dam B.

Emergency activation should require a dedicated permission such as `ALERT_EMERGENCY_TRIGGER`, not merely an "admin" label.

## 7. Dam status and alert severity

Keep two concepts separate.

### Dam operational state
- `NORMAL`
- `WATCH`
- `WARNING`
- `EMERGENCY`

### Alert severity
- `INFO`
- `WATCH`
- `WARNING`
- `EVACUATE`
- `EMERGENCY`

A sensor threshold may create an operator warning/recommendation, but V1 should not automatically evacuate civilians from a single sensor reading.

## 8. Sensor model

Do not create a separate table per sensor type.

A sensor has:
- identity
- type
- unit
- map location
- thresholds
- visibility
- status
- last-seen time

Readings are stored in a generic `sensor_reading` table.

Public visibility:
- `PRIVATE`
- `PUBLIC_SUMMARY`
- `PUBLIC`

`PUBLIC_SUMMARY` must never leak serial numbers, calibration data, maintenance details or sensitive exact location if the operator chooses to hide it.

## 9. MQTT

Use Eclipse Mosquitto.

Recommended topic convention:

```text
dams/{damId}/sensors/{sensorId}/telemetry
dams/{damId}/gates/{gateId}/status
```

Recommended V1 telemetry QoS: **QoS 1**.

The backend must tolerate duplicate MQTT messages. Sensor readings therefore support an optional external message identifier for idempotency.

Do not add Kafka in V1.

### 9.1 IoT device authentication

Each MQTT-capable device/gateway has a unique `deviceId` and rotatable `deviceKey`.

MQTT connection:

```text
username = deviceId
password = deviceKey
```

The key is not included in telemetry JSON.

The operator can rotate, disable or revoke a device credential independently. The broker must apply topic ACLs so a device can publish only to its own authorized namespace.

A network device may own several physical sensors through `iot_device_sensor`.

See `docs/13-IOT-DEVICE-AUTH.md`.

## 10. Real-time dashboard

The browser should not poll every second.

Use:
- MQTT for device → backend
- SSE for backend → admin dashboard

Example:
`GET /api/v1/dams/{damId}/telemetry/stream`

WebSocket is unnecessary unless a real bidirectional requirement appears.

## 11. Geographic model

Use:
- `POINT` for sensors, gates, safe locations, household home points
- `POLYGON` for dam areas and risk zones
- `LINESTRING` for evacuation routes

Database: PostGIS SRID 4326.

API boundary: GeoJSON.

The backend remains authoritative for authorization and spatial decisions. Do not rely only on map-client calculations for emergency targeting.

## 12. Mobile map

The mobile map can show:
- dam outline
- public sensors
- current public sensor summaries
- current dam public status
- risk zones approved for public display
- safe locations
- evacuation routes
- verified/public citizen incidents
- user location

Admin decides which operational objects are public.

## 13. Mapping stack

Recommended:
- React Native: MapLibre React Native
- Next.js: MapLibre GL JS
- Development basemap: OpenFreeMap/OpenStreetMap-compatible source
- Production public-safety deployment: use a provider with an SLA or self-host relevant regional tiles

Dam-specific GIS data always comes from the platform API/PostGIS, not from the basemap provider.

## 14. Alerts and transactional outbox

Critical alert workflow:

```text
Operator confirms alert
       |
       v
DB transaction
  - insert alert
  - insert affected zones
  - insert recipients
  - insert notification_outbox
       |
     COMMIT
       |
       v
background delivery worker
       |
       +--> FCM
       +--> SMS provider later
       +--> other channels later
```

Never make alert persistence depend on FCM being available.

Outbox entries are retried with bounded backoff and retain delivery status.

## 15. Push vs SMS

Primary V1 mobile delivery: Firebase Cloud Messaging.

SMS must be behind a provider interface. There is no assumption of free unlimited production SMS.

The mobile application is one emergency channel, not the only safety mechanism in a real deployment.

## 16. Civilian reports

Report lifecycle:
- `SUBMITTED`
- `UNDER_REVIEW`
- `VERIFIED`
- `REJECTED`
- `PUBLIC`

User-generated content must not be visually confused with official alerts.

Photos go to object storage. The DB stores metadata/object keys.

## 17. Offline mobile requirements

The mobile app should cache the latest:
- dam public summary
- active emergency alert
- risk-zone snapshot
- evacuation routes
- safe locations
- emergency instructions

The app must clearly display the age of cached data.

## 18. Security baseline

- TLS in deployed environments
- passwords hashed using Spring Security supported strong password encoder
- access + refresh token strategy or secure session strategy defined centrally
- role and dam-scope authorization enforced server-side
- upload type/size validation
- rate limiting for abuse-sensitive public endpoints
- secrets only through environment/secret store
- no production secrets committed to Git
- audit critical operator actions
- separate public DTOs from internal entities

## 19. Audit events

Must audit at minimum:
- emergency activation/cancellation
- alert creation/cancellation
- risk-zone changes
- evacuation-route changes
- safe-location status changes
- sensor threshold changes
- sensor disable/enable
- public/private visibility changes
- staff permission changes
- citizen report verification/rejection

## 20. Reliability principles

1. PostgreSQL is the source of truth.
2. A Redis outage must not destroy core correctness.
3. Alert persistence occurs before external delivery.
4. External calls have timeouts.
5. Critical requests should support idempotency where duplicate submission is plausible.
6. Health endpoints exist for infrastructure and deployment verification.
7. DB backups and restore testing are required before production.
8. The deployed mobile app must tolerate temporary network loss.

## 21. Scaling path

### Stage 1
One VM, Docker Compose.

### Stage 2
Move PostgreSQL or object storage to dedicated infrastructure if required.

### Stage 3
Run multiple stateless Spring Boot instances behind a load balancer.

### Stage 4
If measured telemetry load justifies it, separate telemetry ingestion/storage.

Do not optimize for Stage 4 while building Stage 1.

## 22. Definition of done

A feature is done only when:
- implementation is complete
- authorization is enforced
- validation exists
- unit/integration tests pass
- Bruno black-box tests exist
- API docs/contracts are updated
- relevant audit event exists
- CI passes
- no secrets are committed
