---
name: linear-implement-ticket
description: "Implement a Linear ticket end-to-end: look up the ticket, implement the feature following project standards, then post a structured comment. Use when asked to implement a ticket, work on a Linear issue, or start a ticket (e.g. 'Implement ticket SB-74')."
argument-hint: "Linear ticket ID, e.g. SB-74"
allowed-tools: Read, Edit, Write, Glob, Grep, Bash(git checkout dev), Bash(git pull origin dev), Bash(git checkout -b), Bash(git branch --show-current), Bash(./gradlew lint), Bash(./gradlew assembleDebug), mcp__linear__get_issue, mcp__linear__save_comment
disable-model-invocation: false
---

# Linear Ticket Implementation Workflow

Use this skill to implement a Linear ticket end-to-end, following all StugBygget coding standards and architecture rules.

The ticket to implement is: `$ARGUMENTS`

---

## Step 1 — Look up the ticket

1. Use `mcp__linear__get_issue` to fetch the full ticket: title, description, acceptance criteria, labels, and any linked resources.
2. Identify the ticket type: **feature / bug / chore / spike**.
3. Note the **layer** label (frontend / backend / fullstack) and the **module** (planning / todos / gallery / ai-chat / room-planner / ar-measure / materials / shopping / budget / logistics).
4. Note whether the ticket is **UI only** — if so, all values must use mock/placeholder data and every backend integration point must have a `// TODO: wire to backend` comment.
5. If the ticket touches **Firestore schema**, stop and confirm the schema change with the developer before writing any code. Schema changes must be documented in `docs/firestore_schema.md`.
6. If the ticket touches **auth or security rules**, stop and require explicit developer confirmation before proceeding.
7. Note any specific starting instruction from the user (e.g. "start with the ViewModel") — reference it in the final comment.

---

## Step 2 — Create the feature branch

1. Check out `dev` and pull latest:
   ```bash
   git checkout dev
   git pull origin dev
   ```
2. Create the feature branch using the ticket ID and a short slug:
   ```bash
   git checkout -b claude/SB<ID>-short-description
   ```
   Example: `claude/SB74-add-phase-progress-bar`

3. Confirm the branch is active:
   ```bash
   git branch --show-current
   ```

---

## Step 3 — Plan the implementation

1. Read the existing code in the relevant feature module before making any changes.
2. Identify which files need to change across the Clean Architecture layers:

   | Layer | Location |
   |-------|----------|
   | UI (Composables) | `feature/<module>/ui/<Name>Screen.kt` |
   | UiState | `feature/<module>/ui/<Name>UiState.kt` |
   | ViewModel | `feature/<module>/ui/<Name>ViewModel.kt` |
   | ViewModelFactory | `feature/<module>/ui/<Name>ViewModelFactory.kt` |
   | UseCase | `domain/usecase/<Name>UseCase.kt` |
   | Repository interface | `domain/repository/<Name>Repository.kt` |
   | Domain model | `domain/model/<Name>.kt` |
   | Firebase data source | `data/firebase/firestore/<Name>FirestoreDataSource.kt` |
   | DI bindings | `di/` |
   | Shared UI components | `ui/components/` |
   | Theme / colors | `ui/theme/Color.kt`, `ui/theme/Theme.kt` |

3. Check for existing use-cases, repositories, and components before creating new ones.
4. Confirm the full layer chain needed: does this ticket require changes all the way to the data layer, or is it UI + ViewModel only?

---

## Step 4 — Implement the feature

Follow all architecture and coding standards from `CLAUDE.md` and `ARCHITECTURE_GUARD.md`:

### Architecture rules (STRICT)
- **No Firebase/API imports inside Composables** — ever.
- **No business logic in Composables** — route all logic through ViewModel.
- **ViewModel must not call SDKs directly** — go through UseCases or Repositories.
- **Repositories are the only boundary** for Firestore, Storage, Cloud Functions, and external APIs.
- Domain models must be **framework-independent** (no Android imports).
- Business rules and calculations belong in **UseCases**.
- `MutableStateFlow` must remain **private** in ViewModel; expose only `StateFlow`.

### UiState pattern
Every screen must define a `UiState` data class:
```kotlin
data class <Name>UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
    // ... screen-specific fields
)
```
One-time events (navigation, snackbars) must use `SharedFlow` or an event wrapper — not UiState fields.

### State coverage
Every screen must handle:
- [ ] Loading state
- [ ] Error state
- [ ] Empty state (where applicable)

### Style and conventions
- Use **Material 3** components throughout.
- Use **color tokens from `ui/theme/Color.kt`** — never hardcode color values.
- Use **KDoc** for non-trivial business logic (calculations, budgeting, transport, measurement transforms).
- Follow existing naming conventions in the module being modified.
- Convert technical failures to user-friendly messages in the ViewModel.

### Firestore conventions
- All project data under `projects/{projectId}/...` — see `docs/firestore_schema.md`.
- Validate input **before** every write.
- Reads must be side-effect free.
- If this ticket introduces a new collection or field → update `docs/firestore_schema.md`.

### UI-only tickets
- Use mock/placeholder data for all values.
- Add `// TODO: wire to backend` at every integration point.

### Decision points
| Condition | Action |
|-----------|--------|
| New Firestore collection or field | Update `docs/firestore_schema.md` |
| New shared UI component | Place in `ui/components/` |
| New domain model | Place in `domain/model/`, no Android imports |
| New DI binding | Add to the relevant module in `di/` |
| Touches auth or security rules | Stop — require developer confirmation |

---

## Step 5 — Run validation checks

1. Run lint:
   ```bash
   ./gradlew lint
   ```
   Fix all errors before continuing. Warnings are acceptable but note them.

2. Run a debug build to verify compilation:
   ```bash
   ./gradlew assembleDebug
   ```
   The build must succeed with no errors.

---

## Step 6 — Post an implementation comment on the ticket

Use `mcp__linear__save_comment` to post a structured comment on the ticket:

```markdown
## ✅ <TICKET-ID> Implementation Complete

Task: <Linear ticket URL>

### Summary
<One or two sentences describing what was implemented and why.>

### What Was Done
1. **Data Layer**
   - <item>

2. **Domain / ViewModel Layer**
   - <item>

3. **UI Layer**
   - <item>

### Acceptance Criteria Coverage
- [ ] AC1: <description>
- [ ] AC2: <description>

### Manual Test Steps
1. <Step 1>
2. <Step 2>

### Notes / Tradeoffs
- <Any relevant notes, UI-only placeholders, deferred work, or schema changes>
```

Omit layers that were not touched. If the ticket was UI-only, note which values are placeholders and which backend integration points have `// TODO` markers.

---

## Completion checks

The workflow is complete only when all are true:

- [ ] Ticket fetched and all acceptance criteria addressed.
- [ ] Feature branch created from `dev` following `claude/SB<ID>-...` convention.
- [ ] Architecture layering respected — no Firebase/API in Composables, no SDK calls in domain.
- [ ] UiState defined with loading, error, and empty states handled.
- [ ] No hardcoded color values — Material 3 theme tokens used throughout.
- [ ] UI-only tickets use mock data with `// TODO: wire to backend` markers.
- [ ] Firestore schema changes documented in `docs/firestore_schema.md`.
- [ ] Auth / security rule changes confirmed with developer before implementation.
- [ ] `./gradlew lint` passes with no errors.
- [ ] `./gradlew assembleDebug` succeeds.
- [ ] Structured implementation comment posted to the Linear ticket.
