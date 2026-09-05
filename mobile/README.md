# DDAS Public Safety mobile app

Expo SDK 57 / React Native public monitoring client.

It provides:

- public dam status and safety messages
- public sensor readings and health state
- exact location cards only when the API explicitly exposes a location
- allowed public sensor history
- pull-to-refresh behavior

## Run

```powershell
Copy-Item .env.example .env.local
npm install
npm start
```

For an Android emulator, `10.0.2.2` reaches a backend on the host computer. A
physical phone needs `EXPO_PUBLIC_API_BASE_URL` set to the computer's reachable
LAN address.

## Verify

```powershell
npm run typecheck
```
