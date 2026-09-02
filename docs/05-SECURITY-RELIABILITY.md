# Security & Reliability Checklist

## Authentication / authorization
- server-side role checks
- server-side dam-scope checks
- token expiry/refresh policy
- account disable support
- no client-only authorization

## Alerts
- alert committed before FCM/SMS call
- notification outbox retry
- audit who triggered/cancelled
- idempotency key
- affected zones persisted
- users outside zones excluded
- emergency confirmation UX
- duplicate notification tolerance

## Sensor ingestion
- unique credential per IoT device/gateway
- plaintext key shown only at creation/rotation
- key never appears in telemetry payloads or audit logs
- rotation/revocation supported
- broker ACL prevents cross-device/cross-dam publishing
- authenticate sensor/gateway
- TLS in deployed environment
- validate sensor belongs to dam/topic
- reject malformed payloads
- record received time separately from device measurement time
- tolerate duplicate MQTT deliveries
- detect stale/offline sensor

## GIS
- validate geometry
- no invalid/self-intersecting public zones
- perform authoritative zone membership server-side
- public endpoints expose only approved layers

## Uploads
- file size limit
- image MIME/type validation
- generated object key
- no executable uploads
- never trust original filename for storage path

## Privacy
- household data is not public
- exact home points are never returned to other civilians
- minimize collected family data
- access to assistance-related household data is restricted and audited

## Infrastructure
- DB not exposed publicly
- Redis not exposed publicly
- MinIO admin not exposed publicly
- regular database backups
- restore drill before real deployment
- TLS certificates
- secret rotation process
- central logs
- disk-space monitoring
