# Master Bruno Collection

This is the single shared black-box API collection.

## Folders
- `00-health`
- `01-auth`
- `02-dam-monitoring`
- `03-alerts`
- `04-evacuation`
- `05-community`

## Important

The requests are a **V1 executable contract scaffold**. Some IDs and authentication data are populated dynamically by earlier requests. The collection assumes the backend provides deterministic CI test users/fixtures.

Each developer must expand their domain with:
- success
- validation
- authentication
- authorization
- cross-dam isolation
- idempotency/state-conflict
- public data leakage tests where applicable

Do not commit real passwords or tokens.

## Run locally
```bash
cd bruno
bru run --env environments/local.bru
```

## CI
```bash
bru run --env environments/ci.bru --reporter-junit results.xml
```

Use a pinned Bruno CLI version in CI.
