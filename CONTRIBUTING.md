# Contributing

1. Pull latest `main`.
2. Create a short-lived feature branch.
3. Implement one coherent feature.
4. Add/update tests.
5. Add/update Bruno requests for API behavior.
6. Add a Flyway migration for schema changes.
7. Update relevant docs.
8. Open PR.
9. CI must pass.
10. At least one teammate reviews.

## Commit examples
- `feat(alerts): add acknowledgement endpoint`
- `feat(evacuation): add safe location status`
- `fix(sensors): hide exact location from public summary`
- `test(alerts): add cross-dam authorization cases`

## No direct production assumptions
Do not hard-code:
- domain names
- public IPs
- credentials
- dam IDs
- FCM secrets
- SMS provider secrets
