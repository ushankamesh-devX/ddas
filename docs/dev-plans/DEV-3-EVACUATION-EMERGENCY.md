# Dev 3 — Evacuation & Emergency Response

## Mission
Own risk mapping and the actions civilians should take during an emergency.

## Backend
- risk zone CRUD
- safe location CRUD/status/capacity
- evacuation route CRUD
- route-zone association
- public evacuation snapshot
- emergency operational state
- activate/clear emergency
- spatial validation
- zone membership utilities

## Admin web
- draw/edit risk-zone polygons
- place safe locations
- draw routes
- assign route to zone/safe location
- mark route blocked/closed
- activate/clear emergency workflow

## Mobile
- evacuation tab
- current zone
- safe locations
- route display
- emergency map
- offline evacuation cache integration

## Bruno
Must implement full tests under:
`bruno/04-evacuation`

Critical cases:
- invalid geometry rejected
- route from wrong dam rejected
- public endpoint excludes private/inactive data
- closed route is not recommended
- emergency activation requires permission
- duplicate activation handled safely
- clear action audited

## First milestone
Admin draws zone + route + safe location → mobile sees public evacuation map.
