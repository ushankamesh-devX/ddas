# Repository Initialization Guide

Use this after creating the empty GitHub repository.

## 1. Copy starter contents

Copy all files from this package into the repository root.

## 2. Initialize applications

### Backend
Create Spring Boot with at minimum:
- Java 21
- Maven wrapper
- Spring Web
- Spring Security
- Validation
- Spring Data JPA
- PostgreSQL driver
- Flyway
- Actuator

Add separately:
- PostGIS/Hibernate Spatial support
- MQTT client
- FCM SDK when alert delivery work starts
- MinIO/S3 client when report images start

Do not delete `backend/src/main/resources/db/migration/V1__initial_schema.sql`.

### Admin
Initialize Next.js with TypeScript.

### Mobile
Initialize React Native with TypeScript. Use the team's chosen workflow consistently (Expo development build or React Native CLI).

## 3. Protect main

GitHub branch protection:
- PR required
- at least 1 approval
- required CI checks once stable
- block force push
- block branch deletion

## 4. Add developers

Each developer:
1. clones repository
2. reads master design
3. reads own dev plan
4. runs local infrastructure
5. opens Bruno collection
6. creates a feature branch
7. verifies a PR can trigger CI

## 5. Create project board/issues

Recommended epics:
- Foundation
- Dam & Monitoring
- Alerts & Notifications
- Evacuation & Emergency
- Community & Information
- Infrastructure/CI
- Security/Hardening
- Deployment

## 6. First shared decisions

Before feature coding, agree:
- package namespace
- JWT/session implementation
- API response wrappers
- OpenAPI generation approach
- DTO naming
- validation conventions
- test fixture strategy
- how CI creates admin/civilian fixture users

## 7. First integration target

Do not begin with all features at once.

Target:
```text
simulated sensor
  -> MQTT
  -> backend
  -> PostgreSQL
  -> admin current value
```

Then:
```text
operator alert
  -> DB/outbox
  -> FCM
  -> mobile
  -> acknowledgement
```
