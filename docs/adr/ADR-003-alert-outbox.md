# ADR-003: Critical notifications use a transactional outbox

Status: Accepted

Decision: persist alert and outbox rows in the same database transaction, then deliver asynchronously.

Reason: external push/SMS outages must not cause alert loss.
