# ADR-002: PostgreSQL + PostGIS is the source of truth

Status: Accepted

Decision: business and GIS data remain in PostgreSQL/PostGIS in V1.

Reason: transactional consistency, mature GIS operations, fewer moving parts.

Revisit when: measured telemetry storage load exceeds practical PostgreSQL design after indexing/partitioning/retention work.
