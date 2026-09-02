# Dev 4 — Community & Information

## Mission
Own civilian onboarding, household context, public information and citizen reporting.

## Backend
- user profile integration
- household CRUD
- household members
- home location / dam association
- device registration coordination with Dev 2
- citizen report submission
- report review/verification/publication
- report image metadata / MinIO integration
- news CRUD/publishing
- public news/report feeds

## Admin web
- report moderation
- report map/list
- news editor/publishing
- household/people operational views with strict authorization

## Mobile
- profile/household onboarding
- select dam
- home location
- household members
- submit report + photo
- own reports
- public verified reports
- news feed

## Shared platform ownership
Dev 4 is the suggested primary maintainer of:
- Docker Compose
- CI workflow
- Bruno runner integration
- MinIO configuration

This does not mean Dev 4 writes everyone else's tests.

## Bruno
Must implement full tests under:
`bruno/05-community`

Critical cases:
- household data not public
- user cannot access another household
- report upload metadata validated
- unverified report not represented as official
- only authorized staff can verify/publish
- civilian cannot publish news

## First milestone
Civilian registers household → submits report → operator verifies → report becomes public.
