# CI/CD Plan

## CI stages

1. checkout
2. backend compile/test
3. frontend lint/test/build
4. mobile lint/test
5. start integration dependencies
6. run backend against test database
7. wait for health check
8. Bruno HTTP black-box smoke/regression tests
9. MQTT broker black-box tests (auth + ACL + telemetry)
10. build Docker images
9. build Docker images
10. publish reports

## CD stages

For `main` or a release tag:
1. build immutable images
2. tag with commit SHA
3. push to registry
4. deploy to staging VM
5. run Flyway migration through backend startup or a dedicated migration command
6. health check
7. Bruno staging smoke tests
8. manual approval for production when the team reaches that stage
9. deploy production
10. post-deploy smoke tests

## Bruno

The collection in `/bruno` is the master API black-box suite.

Examples:
```bash
cd bruno
bru run --env environments/local.bru
```

CI report:
```bash
bru run --env environments/ci.bru --reporter-junit results.xml
```

Pin the Bruno CLI version in CI.

## Important deployment rule

A failed post-deploy health/smoke test must not silently pass. The pipeline should fail loudly and preserve logs.

## Rollback

V1 rollback:
- keep previous Docker image tag
- redeploy previous image
- database migrations must be backward-compatible where practical

Avoid destructive migrations in the same release that immediately removes columns used by the previous application version.
