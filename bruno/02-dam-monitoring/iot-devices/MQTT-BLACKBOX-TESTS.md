# MQTT Broker-Level Black-Box Tests

These are part of the V1 CI contract but are **not HTTP Bruno requests**.

Run them in a dedicated CI step using an MQTT CLI/client because they test broker authentication and ACL behavior.

## Required cases

1. valid device ID + valid key → connect succeeds
2. valid device ID + wrong key → connect fails
3. unknown device ID → connect fails
4. rotated old key → connect fails
5. new rotated key → connect succeeds
6. disabled device → connect fails
7. revoked device → connect fails
8. Device A publishes own topic → succeeds
9. Device A publishes Device B topic → denied
10. Dam A device publishes Dam B topic → denied
11. device publishes assigned sensor reading → backend accepts
12. device publishes unassigned sensor ID → backend rejects/ignores and audits/logs according to policy
13. duplicate `messageId` → one logical sensor reading
14. malformed telemetry → rejected without crashing consumer

## Suggested CI tooling

Use `mosquitto_pub` / `mosquitto_sub` or a tiny deterministic test script.

Example pattern:

```bash
mosquitto_pub \
  -h localhost \
  -p 1883 \
  -u "$DEVICE_ID" \
  -P "$DEVICE_KEY" \
  -t "dams/$DAM_ID/devices/$DEVICE_ID/telemetry" \
  -m '{"messageId":"ci-1","measuredAt":"...","readings":[]}'
```

For production-like CI, run the TLS-authenticated broker configuration rather than anonymous development mode.
