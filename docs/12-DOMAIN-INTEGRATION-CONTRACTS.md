# Domain Integration Contracts

## Dam → Alerts
Alerts reference a `dam_id` and target risk zones belonging to the same dam.

## Evacuation → Alerts
Alert targeting consumes risk-zone identities. Alert logic must not create shadow copies of zones.

## Household → Alerts
Recipient resolution determines whether a household/user belongs to an affected zone using authoritative PostGIS calculations.

## Dam → Mobile public map
Only explicit public DTOs/layers are returned.

## Evacuation → Mobile
Public evacuation snapshot is designed for caching and includes a server-generated timestamp/version.

## Community → Public feed
News is official content.
Citizen reports remain citizen-generated content even after verification.

## MQTT → Dam
MQTT payload identifies/derives dam and sensor. Backend validates the mapping against registered entities.

## Alert → Notification
Notification transport is an adapter. Alert domain correctness does not depend on Firebase/Twilio response.
