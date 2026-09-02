# Bruno Master Test Matrix

The collection is contract-first. The existing `.bru` files establish the main happy-path flows. Each developer expands their domain until the matrix below is satisfied.

## Global security matrix

Every protected endpoint should have appropriate coverage for:

| Case | Expected |
|---|---|
| no token | 401 |
| malformed/expired token | 401 |
| correct token, wrong role | 403 |
| correct role, wrong dam | 403 or 404 according to agreed anti-enumeration policy |
| invalid resource ID | 404 |
| invalid request body | 400 |
| unexpected internal details in error | must not leak |

## Auth

- admin login succeeds
- civilian login succeeds
- bad password fails
- disabled account fails
- refresh succeeds
- invalid refresh fails
- `/auth/me` returns current identity

## Dam & monitoring

### IoT device authentication
- create device returns generated key only once
- GET device never returns plaintext key
- rotate key returns a new key only once
- old key fails after rotation
- revoked device cannot authenticate
- disabled device cannot authenticate
- Device A cannot publish Device B topic
- device for Dam A cannot publish Dam B topic
- device can submit readings only for assigned sensors
- malformed credentials fail authentication


- create/read/update dam
- list only permitted dams for staff
- create/read/update/delete sensor
- duplicate sensor code within same dam rejected
- same sensor code across different dams permitted if desired by schema
- invalid point rejected
- sensor from another dam cannot be modified
- private sensor absent from public endpoint
- public summary hides internal metadata/exact location when disabled
- public sensor only exposes approved history
- telemetry latest works
- unknown sensor telemetry rejected
- duplicate MQTT external message ID is idempotent
- gate CRUD and cross-dam isolation
- public dam map contains only public layers

## Alerts

- warning creation succeeds
- emergency alert creation succeeds
- unauthorized roles cannot create
- wrong-dam staff cannot create
- invalid zone ID rejected
- zone belonging to different dam rejected
- duplicate idempotency key returns same logical result / no duplicate
- recipients are limited to target zones
- alert persists if delivery provider fails
- delivery summary counts are coherent
- user sees only their alerts
- acknowledgement is idempotent
- cancellation requires permission
- cancelled alert remains historically available
- alert audit event exists

## Evacuation

- risk zone CRUD
- invalid polygon rejected
- self-intersecting geometry rejected or normalized according to backend policy
- safe location CRUD
- negative capacity rejected
- route CRUD
- route zone and safe location must belong to same dam
- blocked route excluded from active recommendation
- closed safe location not recommended
- public evacuation snapshot excludes private/inactive layers
- emergency activation requires dedicated permission
- activation idempotent
- clear requires permission
- activation/clear audited

## Community

- get/update own household
- cannot read another household
- household home location never appears in public APIs
- add/update/delete household member
- submit citizen report
- duplicate report retry is idempotent
- invalid upload metadata rejected
- unverified report not in public feed
- authorized staff can review
- civilian cannot review/publish
- verified/public report clearly remains citizen-generated
- create/update/publish/archive news
- civilian cannot publish news
- only published news appears publicly

## Smoke tag

Keep a small `smoke` set capable of validating a deployment quickly:

1. health
2. admin login
3. create/read dam or read seeded dam
4. create/read sensor
5. public sensors
6. create zone
7. public evacuation snapshot
8. create alert
9. civilian alerts
10. submit report

Do not make post-deploy smoke destructive in production. Production smoke should use read-only endpoints or dedicated test fixtures.


## Collection completion status

The repository now provides concrete Bruno request files for the main V1 HTTP positive/negative/security contracts.

Broker-level MQTT authentication/ACL cases are documented separately because they require an MQTT client rather than HTTP Bruno requests.

During implementation, developers may adjust exact 400/403/404/409 expectations only through reviewed contract changes. Do not silently weaken a security assertion just to make CI green.
