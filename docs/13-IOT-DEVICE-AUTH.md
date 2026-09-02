# IoT Device Authentication, Provisioning and Key Rotation

## V1 decision

Each IoT device/gateway has:
- a unique `deviceId`
- a unique generated `deviceKey`
- a dam assignment
- zero or more assigned sensors
- a lifecycle state

For MQTT:

```text
username = deviceId
password = deviceKey
clientId = deviceId
```

The device key is used during MQTT connection authentication. It is **not included inside every telemetry payload**.

## Why the key must not be in telemetry JSON

Avoid:

```json
{
  "deviceId": "...",
  "deviceKey": "secret",
  "value": 81.4
}
```

because telemetry payloads may appear in logs, MQTT debugging tools, stored raw telemetry or monitoring systems.

Instead authenticate at MQTT connection time:

```text
MQTT CONNECT
  username = deviceId
  password = deviceKey
```

Then send only telemetry:

```json
{
  "messageId": "gw-12-000123",
  "measuredAt": "2026-09-02T12:00:00Z",
  "readings": [
    {
      "sensorId": "sensor-uuid",
      "value": 81.4,
      "quality": "GOOD"
    }
  ]
}
```

## Device and sensor are different concepts

A device is the authenticated network identity. A sensor is a measurement source.

Example:

```text
IoT Gateway 12
  +-- water sensor
  +-- rainfall sensor
  +-- wind sensor
  +-- temperature sensor
```

Therefore V1 uses:
- `iot_device`
- `sensor`
- `iot_device_sensor`

A standalone MQTT-capable sensor is simply one IoT device linked to one sensor.

## Device creation

Admin selects **Create IoT Device**.

Spring Boot:
1. creates `iot_device`
2. generates a cryptographically secure random `deviceKey`
3. provisions the MQTT credential
4. stores only safe credential metadata/verifier information
5. returns the plaintext key **once**
6. writes an audit event

Example response:

```json
{
  "id": "device-uuid",
  "name": "Spillway Gateway 01",
  "status": "ACTIVE",
  "credentials": {
    "deviceId": "device-uuid",
    "deviceKey": "ddk_qG...random...",
    "shownOnce": true
  },
  "mqtt": {
    "host": "mqtt.example.org",
    "port": 8883,
    "topic": "dams/dam-uuid/devices/device-uuid/telemetry"
  }
}
```

A later GET of the device must never return the plaintext device key.

## Key generation

Generate at least 256 bits of cryptographically secure random data.

Suggested display format:

```text
ddk_<random-url-safe-secret>
```

The prefix is only for recognition and secret scanning.

## Key storage

Never store the raw device key as plaintext in PostgreSQL.

Depending on the Mosquitto integration, store only:
- credential ID
- device ID
- key prefix
- verifier/hash or broker credential reference
- status
- created time
- last-used time
- rotated/revoked time

If Mosquitto manages the password verifier internally, the application DB should keep metadata and broker reference rather than duplicate the raw secret.

## Rotation

Admin selects **Rotate Device Key**.

Backend:
1. generates a new random key
2. replaces the broker credential
3. marks the old credential `ROTATED`
4. creates new active credential metadata
5. returns the new plaintext key once
6. audits the rotation

V1 uses **immediate rotation**. The old key stops working immediately.

If field deployments later require zero-downtime credential rollover, V2 can support overlapping credentials/grace windows.

## Revocation

Device lifecycle:
- `ACTIVE`
- `DISABLED`
- `REVOKED`

Disabled or revoked devices must not authenticate.

Revoking one device must not affect credentials for any other device.

## MQTT topic authorization

Authentication answers: "Who are you?"

Authorization answers: "What may you publish?"

Recommended telemetry topic:

```text
dams/{damId}/devices/{deviceId}/telemetry
```

Heartbeat topic:

```text
dams/{damId}/devices/{deviceId}/heartbeat
```

Device A must never be allowed to publish into another device or another dam's topic namespace.

Broker ACLs enforce this.

## Backend validation after broker authentication

Spring Boot still validates:
- device exists
- device is active
- topic dam matches device dam
- each sensor in the payload is assigned to that device
- values are valid
- timestamps are acceptable
- duplicate `messageId` is handled idempotently

Broker authentication does not replace business validation.

## Device management API

```text
GET    /api/v1/dams/{damId}/iot-devices
POST   /api/v1/dams/{damId}/iot-devices
GET    /api/v1/dams/{damId}/iot-devices/{deviceId}
PATCH  /api/v1/dams/{damId}/iot-devices/{deviceId}

POST   /api/v1/dams/{damId}/iot-devices/{deviceId}/rotate-key
POST   /api/v1/dams/{damId}/iot-devices/{deviceId}/revoke
POST   /api/v1/dams/{damId}/iot-devices/{deviceId}/disable
POST   /api/v1/dams/{damId}/iot-devices/{deviceId}/enable

POST   /api/v1/dams/{damId}/iot-devices/{deviceId}/sensors/{sensorId}
DELETE /api/v1/dams/{damId}/iot-devices/{deviceId}/sensors/{sensorId}
```

## HTTP ingestion fallback

If HTTP ingestion is later added:

```text
X-Device-Id: <deviceId>
Authorization: DeviceKey <deviceKey>
```

The JSON payload still excludes the key.

Use HTTPS only.

## Audit

Audit:
- device created
- key generated
- key rotated
- device disabled/enabled
- device revoked
- sensor assigned/unassigned

Never store the raw key in the audit log.

## Production transport

Production MQTT must use TLS.
