# ADR-001: Use a modular monolith

Status: Accepted

Decision: one Spring Boot deployable with clear internal domain modules.

Reason: four-person team, strong transactional needs, simpler local/production operations.

Revisit when: measured scaling or organizational boundaries justify extracting a component.
