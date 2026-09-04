# Domain Integration Contracts

## Implemented shared Java contracts

The concrete package root is `com.damalert.ddas`.

| Contract | Consumers | Rule |
| --- | --- | --- |
| `common.security.CurrentUserProvider` | all protected modules | Use it for the authenticated user; do not decode JWTs in domain controllers. |
| `dam.application.DamReader` | alert, evacuation, community | Use it for authoritative dam lookup; do not inject `DamRepository`. |
| `dam.application.DamAccessChecker` | all dam-owned use cases | Enforce membership/role on every private dam operation. Membership is checked from the database, not trusted from token claims. |
| `common.geo.GeometryMapper` | dam, evacuation, community | Validate GeoJSON and create JTS geometry with SRID 4326. Coordinate order is longitude, latitude. |
| `common.audit.AuditService` | all privileged mutations | Record the event inside the use-case transaction. Do not insert into `audit_log` directly. |

The common API error envelope is implemented under `common.error`. Modules
throw typed API exceptions and do not create their own incompatible error
formats.

## Ownership boundaries

- Dev 1 owns the auth/dam foundation and the dam-monitoring module.
- Dev 2 owns alert/outbox/notification code and consumes dam identities through
  `DamReader`.
- Dev 3 owns evacuation/emergency code and consumes dam authorization through
  `DamAccessChecker`.
- Dev 4 owns community/household/storage code and must keep public DTOs separate
  from private household/report entities.

Changing a shared interface is a contract change and requires review from each
affected owner. Additive methods are preferred to exposing another module's
repository.

## Dam → Alerts
Alerts reference a `dam_id` and target risk zones belonging to the same dam.

## Evacuation → Alerts
Alert targeting consumes risk-zone identities. Alert logic must not create shadow copies of zones.

## Household → Alerts
Recipient resolution determines whether a household/user belongs to an affected zone using authoritative PostGIS calculations.

## Dam → Mobile public map
Only explicit public DTOs/layers are returned.

## Evacuation → Mobile
Public evacuation snapshot is designed for caching and includes a server-generated timestamp/version.

## Community → Public feed
News is official content.
Citizen reports remain citizen-generated content even after verification.

## MQTT → Dam
MQTT payload identifies/derives dam and sensor. Backend validates the mapping against registered entities.

## Alert → Notification
Notification transport is an adapter. Alert domain correctness does not depend on Firebase/Twilio response.
