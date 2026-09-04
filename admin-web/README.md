# DDAS Admin Web

Administrative web application for the Dam Disaster Alert System.

## Stack

- Next.js App Router
- React and TypeScript
- Tailwind CSS
- ESLint

## First-time setup

```powershell
cd admin-web
Copy-Item .env.example .env.local
npm install
npm run dev
```

Open <http://localhost:3000>.

The local API URL defaults to `http://localhost:8080`. Override it through
`NEXT_PUBLIC_API_BASE_URL` in `.env.local`. Do not commit real secrets; variables
prefixed with `NEXT_PUBLIC_` are exposed to the browser and must never contain
secrets.

## Later starts

```powershell
cd admin-web
npm run dev
```

## Checks

```powershell
npm run lint
npm run build
```

Place domain modules under `src/features`, shared UI under `src/components`, and
shared API/auth/map infrastructure under `src/lib`.
