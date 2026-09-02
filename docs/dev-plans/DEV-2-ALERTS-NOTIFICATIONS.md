# Dev 2 — Alerts & Notifications

## Mission
Own the end-to-end warning delivery path from operator action to civilian acknowledgement.

## Backend
- alert creation/read/cancel
- alert severity
- alert-zone association
- recipient resolution
- notification outbox
- delivery attempt state
- FCM provider abstraction/integration
- acknowledgement
- delivery summary
- idempotency for alert creation
- alert audit events

## Admin web
- alert creation wizard
- affected-zone selection
- severity/instructions
- confirmation control
- alert history
- delivery summary

## Mobile
- push token registration
- notification handling
- alert inbox
- emergency alert screen
- acknowledgement
- deep link from notification

## Bruno
Must implement full tests under:
`bruno/03-alerts`

Critical cases:
- civilian cannot create alert
- staff from Dam A cannot alert Dam B
- only affected users become recipients
- duplicate idempotency key does not create duplicate alert
- persisted alert exists even if notification provider fails
- cancellation is audited
- acknowledgement works once and remains idempotent

## First milestone
Operator creates alert → DB/outbox → FCM → mobile receives → user acknowledges.
