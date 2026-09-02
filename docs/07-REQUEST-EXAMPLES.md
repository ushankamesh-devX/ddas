# Request Examples

## Create sensor

```json
{
  "code": "WL-001",
  "name": "Reservoir Water Level",
  "sensorType": "WATER_LEVEL",
  "unit": "m",
  "location": {
    "type": "Point",
    "coordinates": [80.1234, 7.1234]
  },
  "visibility": "PUBLIC_SUMMARY",
  "exposeExactLocation": false,
  "warningThreshold": 80,
  "criticalThreshold": 90
}
```

## Create risk zone

```json
{
  "code": "CRIT-01",
  "name": "Critical Zone 1",
  "severity": "CRITICAL",
  "evacuationRequired": true,
  "publicVisible": true,
  "geometry": {
    "type": "Polygon",
    "coordinates": [[
      [80.1200, 7.1200],
      [80.1300, 7.1200],
      [80.1300, 7.1300],
      [80.1200, 7.1300],
      [80.1200, 7.1200]
    ]]
  }
}
```

## Create alert

Header:
`Idempotency-Key: <uuid>`

```json
{
  "severity": "EVACUATE",
  "title": "Evacuate immediately",
  "message": "Move to the assigned safe location using the approved route.",
  "recommendedAction": "Open the evacuation map and follow official instructions.",
  "evacuationRequired": true,
  "riskZoneIds": ["<zone-uuid>"]
}
```

## MQTT reading

Topic:
`dams/{damId}/sensors/{sensorId}/telemetry`

```json
{
  "messageId": "sensor-123-20260902T120000Z",
  "measuredAt": "2026-09-02T12:00:00Z",
  "value": 81.42,
  "quality": "GOOD"
}
```


## IoT MQTT authentication

```text
username = <deviceId>
password = <generated deviceKey>
```

Topic:

```text
dams/{damId}/devices/{deviceId}/telemetry
```

Payload:

```json
{
  "messageId": "gw-12-000123",
  "measuredAt": "2026-09-02T12:00:00Z",
  "readings": [
    {
      "sensorId": "<sensor-uuid>",
      "value": 81.42,
      "quality": "GOOD"
    }
  ]
}
```

Do not include `deviceKey` in the payload.
