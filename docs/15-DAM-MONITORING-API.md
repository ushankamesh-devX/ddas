# Dam monitoring implementation

Dev 1 is implemented as the `monitoring` backend module, the admin monitoring
console, the Expo public mobile client, and the Bruno collection under
`bruno/02-dam-monitoring`.

## Private API

All private operations are dam-scoped and call `DamAccessChecker`.

| Capability | Endpoints |
| --- | --- |
| Dams | `GET/POST /api/v1/dams`, `GET/PUT/DELETE /api/v1/dams/{damId}`, `PATCH /api/v1/dams/{damId}/state` |
| Sensors | `GET/POST /api/v1/dams/{damId}/sensors`, `GET/PUT/DELETE /api/v1/dams/{damId}/sensors/{sensorId}` |
| History | `GET /api/v1/dams/{damId}/sensors/{sensorId}/readings` |
| Live telemetry | `GET /api/v1/dams/{damId}/telemetry/latest`, `GET /api/v1/dams/{damId}/telemetry/stream` |
| Gates | `GET/POST /api/v1/dams/{damId}/gates`, `PUT/DELETE /api/v1/dams/{damId}/gates/{gateId}` |
| IoT devices | `GET/POST /api/v1/dams/{damId}/iot-devices`, `GET/PATCH /api/v1/dams/{damId}/iot-devices/{deviceId}` |
| Credentials | `POST .../rotate-key`, `POST .../disable`, `POST .../enable`, `POST .../revoke` |
| Assignment | `POST/DELETE .../iot-devices/{deviceId}/sensors/{sensorId}` |

## Public API

- `GET /api/v1/public/dams`
- `GET /api/v1/public/dams/{damId}`
- `GET /api/v1/public/dams/{damId}/map`
- `GET /api/v1/public/dams/{damId}/sensors`
- `GET /api/v1/public/dams/{damId}/sensors/{sensorId}/readings`

Private sensors return no public record. Public DTOs exclude thresholds,
metadata, firmware/device data, credential material, and maintenance details.
`PUBLIC_SUMMARY` never includes exact location. A `PUBLIC` sensor includes its
point only when `exposeExactLocation` is enabled.

## MQTT contract

Backend subscription:

```text
dams/+/devices/+/telemetry
```

Payload:

```json
{
  "messageId": "gateway-12-000123",
  "measuredAt": "2026-09-05T12:00:00Z",
  "readings": [
    { "sensorId": "sensor-uuid", "value": 81.4, "quality": "GOOD" }
  ]
}
```

The connection credential is not part of the JSON. The ingestion service checks
the device and topic dam, active lifecycle state, each device/sensor assignment,
timestamp limits, numeric precision, and duplicate `messageId`. Successful
commits emit an SSE telemetry event.

`BrokerCredentialProvisioner` is the platform seam for Dev 4's production
Mosquitto credential/ACL integration. The repository's anonymous Mosquitto
configuration is development-only; production still requires TLS and broker
ACLs from `docs/13-IOT-DEVICE-AUTH.md`.

## Verification

```powershell
Set-Location backend
.\mvnw.cmd test

Set-Location ../admin-web
npm run lint
npm run build

Set-Location ../mobile
npm run typecheck

Set-Location ../bruno
npm run test:local:dev1
```

The sensor simulator is documented under `infra/sensor-simulator`.
