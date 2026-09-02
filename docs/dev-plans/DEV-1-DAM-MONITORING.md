# Dev 1 — Dam & Monitoring

## Mission
Own the end-to-end dam monitoring experience.

## Backend
- dam CRUD and state
- dam staff read/integration points
- sensor CRUD
- IoT device provisioning, generated keys, rotation and revocation
- IoT device-to-sensor assignment
- sensor visibility rules
- sensor reading ingestion model
- gate CRUD
- MQTT subscriber integration with Dev 4/platform support
- latest telemetry API
- sensor history API
- SSE telemetry stream
- public dam/sensor DTOs

## Admin web
- dam overview
- live monitoring cards/charts
- sensor management table
- place/edit sensors on map
- gate management
- public visibility controls

## Mobile
- public dam summary
- public sensor list/map cards
- allowed historical data

## Bruno
Must implement full tests under:
`bruno/02-dam-monitoring`

Critical cases:
- cross-dam access denied
- private sensor absent from public APIs
- `PUBLIC_SUMMARY` hides technical fields
- malformed geometry rejected
- unknown sensor rejected
- stale/offline sensor behavior

## First milestone
Sensor simulator/MQTT → backend → DB → admin live value.

## Interfaces with others
- Dev 2 consumes dam state for alert context.
- Dev 3 consumes dam geometry and sensors as map layers.
- Dev 4 owns shared infrastructure and MQTT broker configuration.
