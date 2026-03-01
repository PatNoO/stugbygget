# Firebase Notes

## Rules
- Source of truth: `firebase/firestore.rules`

## Emulator Tests (planned)
Suggested command sequence:
1. `firebase emulators:start --only firestore`
2. Run rule tests for member/non-member/unauthenticated access.

The Kotlin app code assumes project-scoped data under `projects/{projectId}`.
