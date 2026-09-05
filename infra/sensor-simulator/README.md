# Sensor simulator

Provision an IoT device in the admin console, assign a sensor to it, then publish a QoS 1 reading through the local Mosquitto container:

```powershell
.\infra\sensor-simulator\publish-once.ps1 `
  -DamId <dam-uuid> `
  -DeviceId <device-uuid> `
  -SensorId <sensor-uuid> `
  -Value 81.4
```

The device key is intentionally not placed in telemetry JSON. Local Mosquitto allows anonymous clients; deployed environments must replace that development-only broker policy with the credential/ACL adapter described in `docs/13-IOT-DEVICE-AUTH.md`.
