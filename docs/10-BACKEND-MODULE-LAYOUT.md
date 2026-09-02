# Spring Boot Module Layout

Recommended package structure inside one deployable application:

```text
com.yourorg.damalert
|
+-- common
|   +-- api
|   +-- error
|   +-- security
|   +-- geo
|   +-- audit
|
+-- auth
|   +-- api
|   +-- application
|   +-- domain
|   +-- persistence
|
+-- dam
|   +-- api
|   +-- application
|   +-- domain
|   +-- persistence
|   +-- mqtt
|
+-- alert
|   +-- api
|   +-- application
|   +-- domain
|   +-- persistence
|   +-- notification
|
+-- evacuation
|   +-- api
|   +-- application
|   +-- domain
|   +-- persistence
|
+-- community
    +-- api
    +-- application
    +-- domain
    +-- persistence
    +-- storage
```

## Dependency rule

Prefer:
`api -> application -> domain`

Persistence/adapters implement interfaces needed by application/domain.

Do not make every module call every other repository directly.

Cross-domain operations should go through explicit application services.

## DTO rule

Separate:
- request DTO
- internal/domain representation
- operator response DTO
- public response DTO

This is especially important for sensors, household data and reports.

## Controller rule

Controllers should:
- authenticate/authorize through configured security
- validate transport input
- call application service
- map response

Controllers should not contain spatial/business workflows.

## Transaction rule

Transactions belong around application use cases, especially:
- alert + recipients + outbox
- emergency activation + audit
- report review + publication state
