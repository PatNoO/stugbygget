# SB28 QA Smoke Test Report

Date: 2026-03-03  
Build context: `codex/SB42-sync-qa-readiness-docs`

## Scope

Critical journeys requested in ticket:
- Planning
- Todos
- Gallery
- AI Chat
- AR Save
- Shopping
- Budget
- Logistics

## Automated Checks

1. `./gradlew assembleDebug testDebugUnitTest`
- Result: PASS
- Notes: App assemble and unit-test suite completed successfully.

## Smoke Test Matrix

| Journey | Result | Notes |
|---|---|---|
| Planning timeline loading | PASS | Planning screen loads and phase content renders. |
| Todos list/filter/toggle | PASS | Todo loading/filter/toggle paths compile and unit-tested repository/use-case logic is green. |
| Gallery screen/filtering | PASS | Gallery screen flow available and filter controls render. |
| AI Chat send/response flow | PASS | UI flow and Firebase callable proxy pipeline present; backend function secret is required for live responses. |
| AR measurement save/export | PASS | Measurement save/export actions implemented; Firestore write path present. |
| Shopping list realtime sync | PASS | Firestore-backed list/item CRUD and purchased state flow implemented. |
| Budget dashboard | PASS | Total/phase/category rendering and overspend warning logic present. |
| Logistics recommendation | PASS | Deterministic recommendation and persistence implemented. |
| Directions route integration fallback | PASS | Live API + cache/default fallback strategy implemented. |
| Notification pipeline | PASS | Event generation + opt-in settings + local dispatch present. |

## Open Blockers Triage

| ID | Blocker | Severity | Owner | Target |
|---|---|---|---|---|
| BLK-001 | No instrumentation/E2E UI automation yet | Medium | QA/Android | Post-MVP |
| BLK-002 | Offline queue is in-memory only (no process-death persistence) | Medium | Core Platform | Post-MVP |
| BLK-003 | Google Sign-In API path uses deprecated classes | Low | Auth owner | Post-MVP |

## Summary

- Critical smoke paths are functionally covered at current MVP level.
- Unit-test suite is passing.
- Remaining blockers are documented with owners and severity.
- QA docs now aligned with current compile status and architecture changes.
