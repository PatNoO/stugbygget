# ARCHITECTURE_GUARD.md

This document defines architectural invariants that must not be violated.

---

## Layering Rules (STRICT)

UI -> ViewModel -> UseCase -> Repository -> Data Source

- UI must never access Firebase, Retrofit, Maps, ARCore, or CameraX directly.
- UI must not contain business logic.
- ViewModel must not call SDKs directly; go through use-cases/repositories.
- Repositories are the only boundary for remote/local data operations.

---

## State Rules

- Each screen defines a UiState.
- MutableStateFlow remains private.
- Public state is immutable StateFlow.
- One-time events use SharedFlow or an event wrapper.
- State transitions must be explicit and predictable.

---

## Firebase Guardrails

- Keep all project data under `projects/{projectId}`.
- No schema changes without PR documentation and migration notes when needed.
- Validate write input.
- No silent data mutation on read.
- Respect team-based access constraints in security rules.

---

## Domain Rules

- Domain models must be independent of Android framework.
- Business logic belongs in use-cases (or ViewModel only if trivial).
- Calculation logic (materials, budget, logistics) must be deterministic and testable.

---

## Forbidden Patterns

- Firebase/API imports in Composables.
- try/catch-heavy UI logic branches.
- ViewModels with mixed concerns across unrelated modules.
- Direct SDK calls in domain layer.

---

## Scalability Principles

- Feature modules with clear boundaries.
- Minimal coupling across modules.
- Explicit error handling and recovery paths.
- Testable domain logic before UI polish.

---

## Architecture Review Trigger

Any PR that touches one of the following requires strict architecture review:
- Repository/data contracts
- Firestore schema or rules
- State model changes
- Dependency additions
- Core module boundaries

Architecture stability > feature speed.
