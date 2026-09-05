param(
    [Parameter(Mandatory = $true)][Guid]$DamId,
    [Parameter(Mandatory = $true)][Guid]$DeviceId,
    [Parameter(Mandatory = $true)][Guid]$SensorId,
    [decimal]$Value = 81.4,
    [ValidateSet("GOOD", "SUSPECT", "BAD", "UNKNOWN")][string]$Quality = "GOOD"
)

$ErrorActionPreference = "Stop"
$messageId = "sim-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
$payload = @{
    messageId = $messageId
    measuredAt = [DateTime]::UtcNow.ToString("o")
    readings = @(@{ sensorId = $SensorId.ToString(); value = $Value; quality = $Quality })
} | ConvertTo-Json -Depth 5 -Compress

docker compose --env-file "$PSScriptRoot/../.env" -f "$PSScriptRoot/../docker-compose.dev.yml" exec -T mosquitto `
    mosquitto_pub -h localhost -q 1 -t "dams/$DamId/devices/$DeviceId/telemetry" -m $payload

Write-Output "Published $messageId for sensor $SensorId"
