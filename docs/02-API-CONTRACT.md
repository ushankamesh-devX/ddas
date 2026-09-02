# V1 API Contract

This is the initial contract. Developers may extend it through reviewed PRs.

## Authentication

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

## Users / households

- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`
- `GET /api/v1/households/me`
- `PUT /api/v1/households/me`
- `POST /api/v1/households/me/members`
- `PATCH /api/v1/households/me/members/{memberId}`
- `DELETE /api/v1/households/me/members/{memberId}`
- `POST /api/v1/devices`
- `DELETE /api/v1/devices/{deviceId}`

## Dam & monitoring

Admin/operator:
- `GET /api/v1/dams`
- `POST /api/v1/dams`
- `GET /api/v1/dams/{damId}`
- `PATCH /api/v1/dams/{damId}`
- `GET /api/v1/dams/{damId}/staff`
- `POST /api/v1/dams/{damId}/staff`
- `DELETE /api/v1/dams/{damId}/staff/{userId}`

IoT devices:
- `GET /api/v1/dams/{damId}/iot-devices`
- `POST /api/v1/dams/{damId}/iot-devices`
- `GET /api/v1/dams/{damId}/iot-devices/{deviceId}`
- `PATCH /api/v1/dams/{damId}/iot-devices/{deviceId}`
- `POST /api/v1/dams/{damId}/iot-devices/{deviceId}/rotate-key`
- `POST /api/v1/dams/{damId}/iot-devices/{deviceId}/revoke`
- `POST /api/v1/dams/{damId}/iot-devices/{deviceId}/disable`
- `POST /api/v1/dams/{damId}/iot-devices/{deviceId}/enable`
- `POST /api/v1/dams/{damId}/iot-devices/{deviceId}/sensors/{sensorId}`
- `DELETE /api/v1/dams/{damId}/iot-devices/{deviceId}/sensors/{sensorId}`

Sensors:
- `GET /api/v1/dams/{damId}/sensors`
- `POST /api/v1/dams/{damId}/sensors`
- `GET /api/v1/dams/{damId}/sensors/{sensorId}`
- `PATCH /api/v1/dams/{damId}/sensors/{sensorId}`
- `DELETE /api/v1/dams/{damId}/sensors/{sensorId}`
- `GET /api/v1/dams/{damId}/sensors/{sensorId}/readings?from=&to=`
- `GET /api/v1/dams/{damId}/telemetry/latest`
- `GET /api/v1/dams/{damId}/telemetry/stream` (SSE)

Gates:
- `GET /api/v1/dams/{damId}/gates`
- `POST /api/v1/dams/{damId}/gates`
- `PATCH /api/v1/dams/{damId}/gates/{gateId}`
- `DELETE /api/v1/dams/{damId}/gates/{gateId}`

Public:
- `GET /api/v1/public/dams`
- `GET /api/v1/public/dams/{damId}`
- `GET /api/v1/public/dams/{damId}/map`
- `GET /api/v1/public/dams/{damId}/sensors`
- `GET /api/v1/public/dams/{damId}/sensors/{sensorId}/readings`

## Alerts

Operator/admin:
- `GET /api/v1/dams/{damId}/alerts`
- `POST /api/v1/dams/{damId}/alerts`
- `GET /api/v1/dams/{damId}/alerts/{alertId}`
- `POST /api/v1/dams/{damId}/alerts/{alertId}/cancel`
- `GET /api/v1/dams/{damId}/alerts/{alertId}/delivery-summary`

Civilian:
- `GET /api/v1/alerts/me`
- `GET /api/v1/alerts/me/{alertId}`
- `POST /api/v1/alerts/me/{alertId}/acknowledge`

## Evacuation / emergency response

Risk zones:
- `GET /api/v1/dams/{damId}/risk-zones`
- `POST /api/v1/dams/{damId}/risk-zones`
- `PATCH /api/v1/dams/{damId}/risk-zones/{zoneId}`
- `DELETE /api/v1/dams/{damId}/risk-zones/{zoneId}`

Safe locations:
- `GET /api/v1/dams/{damId}/safe-locations`
- `POST /api/v1/dams/{damId}/safe-locations`
- `PATCH /api/v1/dams/{damId}/safe-locations/{safeLocationId}`
- `DELETE /api/v1/dams/{damId}/safe-locations/{safeLocationId}`

Routes:
- `GET /api/v1/dams/{damId}/evacuation-routes`
- `POST /api/v1/dams/{damId}/evacuation-routes`
- `PATCH /api/v1/dams/{damId}/evacuation-routes/{routeId}`
- `DELETE /api/v1/dams/{damId}/evacuation-routes/{routeId}`

Emergency state:
- `POST /api/v1/dams/{damId}/emergency/activate`
- `POST /api/v1/dams/{damId}/emergency/clear`
- `GET /api/v1/dams/{damId}/emergency`

Public:
- `GET /api/v1/public/dams/{damId}/evacuation`
- `GET /api/v1/public/dams/{damId}/safe-locations`
- `GET /api/v1/public/dams/{damId}/evacuation-routes`

## Community

Reports:
- `POST /api/v1/reports`
- `GET /api/v1/reports/me`
- `GET /api/v1/dams/{damId}/reports`
- `GET /api/v1/dams/{damId}/reports/{reportId}`
- `POST /api/v1/dams/{damId}/reports/{reportId}/review`
- `POST /api/v1/dams/{damId}/reports/{reportId}/publish`
- `GET /api/v1/public/dams/{damId}/reports`

Report images:
- recommended flow is upload through backend or presigned object-storage URL, while the backend owns authorization and metadata.

News:
- `GET /api/v1/dams/{damId}/news`
- `POST /api/v1/dams/{damId}/news`
- `PATCH /api/v1/dams/{damId}/news/{newsId}`
- `DELETE /api/v1/dams/{damId}/news/{newsId}`
- `POST /api/v1/dams/{damId}/news/{newsId}/publish`
- `GET /api/v1/public/dams/{damId}/news`

## Idempotency

Critical write endpoints should accept:
`Idempotency-Key: <uuid>`

At minimum:
- alert creation
- emergency activation
- report submission where retry from weak mobile connectivity is likely

## Public DTO rule

Never serialize JPA entities directly.

Create explicit public DTOs so internal fields cannot accidentally leak.
