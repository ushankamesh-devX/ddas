# Initial Team Kickoff

## Day 1
All four developers:
- review master design
- agree naming and V1 scope
- initialize Spring Boot / Next.js / React Native
- run local Docker infrastructure
- open master Bruno collection
- protect `main`
- configure PR review rule

## Day 2
- Dev 1: auth/dam/sensor backend skeleton
- Dev 2: mobile shell + notification integration skeleton
- Dev 3: admin shell + MapLibre map skeleton
- Dev 4: household/community skeleton + MQTT/CI/MinIO baseline

## Day 3–5
Target first vertical integration:
- test dam exists
- one simulated sensor publishes MQTT
- backend stores reading
- admin sees current value
- Bruno health/auth/dam smoke collection runs

## Week 2
- GIS zones and public map
- sensor visibility
- household association
- alert persistence/outbox

## Week 3
- FCM end-to-end
- alert acknowledgement
- safe locations/routes
- report submission/moderation

## Week 4+
- hardening
- offline mobile behavior
- cross-dam authorization
- failure testing
- deployment VM
- backup/restore validation

## Rule
Do not let each domain stay disconnected until the end. Every week must end with at least one integrated demonstration.
