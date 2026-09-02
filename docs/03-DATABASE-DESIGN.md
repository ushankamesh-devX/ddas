# Database Design Guide

The canonical V1 schema is in:

`backend/src/main/resources/db/migration/V1__initial_schema.sql`

## Core conventions

- PostgreSQL 16+ recommended
- PostGIS enabled
- UUID primary keys using `gen_random_uuid()`
- `timestamptz` for time
- names use snake_case
- geometry SRID: 4326
- check constraints for small stable status vocabularies
- foreign keys explicitly defined
- indexes on common tenant/dam and time access paths
- PostgreSQL is the source of truth

## Multi-dam isolation

Every dam-owned resource contains `dam_id`.

Backend authorization must validate the caller's membership/permission for that `dam_id`.

Never trust a client-supplied dam ID without authorization checks.

## Sensor readings

Do not prematurely introduce a time-series database.

Start with PostgreSQL. If volume grows:
1. measure
2. index
3. consider time partitioning
4. retain/aggregate old raw data
5. only then evaluate specialized infrastructure

## Geometry

Spatial indexes use GiST.

Important backend operations:
- `ST_Contains` / `ST_Covers` for household/user zone membership
- distance queries for nearby safe locations
- geometry validation before persistence

## Files

Photos are not stored as byte arrays in PostgreSQL.

`report_image.object_key` refers to MinIO/S3-compatible storage.


## IoT device credentials

`iot_device` represents the authenticated network device/gateway.

`iot_device_sensor` maps that device to one or more sensors.

`iot_device_credential` stores credential metadata. The plaintext device key must never be stored in PostgreSQL.

Rotation and revocation are audited.
