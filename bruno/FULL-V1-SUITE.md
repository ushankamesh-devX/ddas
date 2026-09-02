# Full V1 Bruno/Black-Box Suite

The repository now contains the V1 API contract suite grouped into:

- health
- authentication
- dam/monitoring
- IoT provisioning
- alerts
- evacuation/emergency
- community
- cross-domain/privacy

## What Bruno covers

Bruno covers HTTP black-box behavior:

- success paths
- validation
- missing/invalid authentication
- role authorization
- cross-dam isolation
- public/private data boundaries
- idempotency contracts
- workflow state contracts
- one-time IoT key exposure contracts

## What Bruno does not directly cover

MQTT broker authentication/ACL behavior is a broker-level black-box test and lives in:

`02-dam-monitoring/iot-devices/MQTT-BLACKBOX-TESTS.md`

Those tests belong in the same CI pipeline, but should use an MQTT client/CLI rather than pretending they are HTTP requests.

## Fixture requirements

To make the suite deterministic, the backend CI profile should create:

- admin for Dam A
- operator/engineer for Dam A
- civilian in Dam A target zone
- admin for Dam B
- Dam A and Dam B
- target zone in each dam
- public/private/public-summary sensors
- one safe location/route
- one IoT test device and assigned sensor where needed

Never point destructive CI collections at production.
