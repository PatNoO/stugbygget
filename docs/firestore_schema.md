# Firestore Schema (Issue P04)

All project data is scoped under `projects/{projectId}`.

## Required Collections
- `projects/{projectId}/info`
- `projects/{projectId}/phases`
- `projects/{projectId}/todos`
- `projects/{projectId}/photos`
- `projects/{projectId}/rooms`
- `projects/{projectId}/measurements`
- `projects/{projectId}/materials`
- `projects/{projectId}/prices`
- `projects/{projectId}/shopping_lists`
- `projects/{projectId}/budget`
- `projects/{projectId}/transport`
- `projects/{projectId}/chat`

## Access Model
- Auth required for all reads/writes.
- User UID must exist in `projects/{projectId}/info.teamMembers`.
- Cross-project access is denied by default.

## Rule Verification Plan
- Use Firestore emulator tests to validate `allow`/`deny` for:
  - Team member read/write in own project.
  - Non-member read/write denial.
  - Unauthenticated read/write denial.
