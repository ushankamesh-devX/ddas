# Admin and Mobile Structure

## Next.js admin

Suggested feature folders:

```text
src/
  app/
  features/
    auth/
    dams/
    monitoring/
    alerts/
    evacuation/
    reports/
    news/
    people/
  components/
  lib/
    api/
    map/
    auth/
```

Keep API access behind typed client functions. Do not scatter raw `fetch()` URLs throughout components.

Map layers should be feature-specific but share one map foundation.

## React Native

Suggested feature folders:

```text
src/
  navigation/
  features/
    auth/
    home/
    dam-map/
    alerts/
    evacuation/
    reports/
    news/
    profile/
  services/
    api/
    notifications/
    offline/
    location/
  storage/
  components/
```

## Mobile offline

The offline layer should version/cache an evacuation snapshot by dam and timestamp.

Never silently show stale emergency data as current. Display the last updated time.

## State management

Do not add a large state framework by default.

Start with:
- server-state/query library if the team chooses one
- local component state
- small application context/store for auth and emergency state

Adopt additional global state only when repeated pain justifies it.
