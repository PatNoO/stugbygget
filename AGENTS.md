# AGENTS.md - AI-First Workflow
SommarStugan (Android)

> This document defines how Codex should work in this repository.
> Human review is mandatory before merge.

---

# Project Overview

SommarStugan is an Android app for coordinating a Swedish summer cottage renovation (summer 2026).

Core modules:
- Planning / Timeline
- Todos
- Photo Gallery
- AI Assistant ("Stugan AI")
- Room Planner (2D)
- AR Measurement
- Smart Materials & Live Prices
- Shopping Lists & Budget
- Logistics Planning

---

# Tech Stack

- Language: Kotlin
- UI: Jetpack Compose (Material 3)
- Architecture: Clean Architecture + feature modules
- Async: Kotlin Coroutines + StateFlow
- DI: Hilt
- Backend: Firebase Auth + Firestore + Storage + Cloud Functions
- Integrations: Retrofit (Claude API), Google Maps SDK, ARCore, CameraX
- AI Agent: Codex (primary)

---

# Architecture Rules (STRICT)

## Layering

UI -> ViewModel -> UseCase -> Repository -> Data Source

### UI (Compose)
- No business logic.
- No Firebase/API calls.
- Only communicates with ViewModel.
- Must support loading, error, and empty states where relevant.

### ViewModel
- Owns UiState (StateFlow).
- Handles user events.
- Coordinates use-cases.
- Converts technical failures into user-friendly messages.

### Domain (UseCase + Models)
- Domain models must be framework-independent.
- Use-cases contain business rules (calculations, comparisons, planning logic).

### Data Layer
- Repositories are the only entry point for Firebase/API.
- Data sources can call Firestore, Storage, Cloud Functions, and external APIs.

---

# State Pattern

Each screen should define a UiState, for example:

```kotlin
data class ScreenUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

For one-time events (navigation/snackbar), use SharedFlow or an event wrapper.

---

# Firebase Conventions

Primary project path:
- projects/{projectId}/...

Typical collections:
- projects/{projectId}/phases
- projects/{projectId}/todos
- projects/{projectId}/photos
- projects/{projectId}/rooms
- projects/{projectId}/measurements
- projects/{projectId}/materials
- projects/{projectId}/prices
- projects/{projectId}/shopping_lists
- projects/{projectId}/budget
- projects/{projectId}/transport
- projects/{projectId}/chat

Rules:
- Do not change schema without documenting it in the PR.
- Validate input before write.
- Keep reads side-effect free.
- Keep project access team-scoped through Firebase Auth + security rules.

---

# Delivery Workflow

When implementing a task:
1. Read the task description and acceptance criteria.
2. Keep scope tight; do not add unrelated changes.
3. Confirm acceptance criteria coverage in the PR.
4. Use the ticket format in [TICKET_TEMPLATES.md](/Users/mrnoordh/AndroidStudioProjects/stugbygget/TICKET_TEMPLATES.md) when creating or refining tickets.

If a task is UI-only:
- Use mock/placeholder data.
- Add TODO markers for later backend wiring.

---

# Branch Naming

Format:

```text
codex/<TASK-ID>-short-description
```

Examples:
- codex/SOM-12-planning-timeline-progress
- codex/SOM-21-shopping-budget-sync

---

# Commit Rules

All commits must start with:

```text
[codex]
```

Examples:
- [codex] SOM-12 Implement planning timeline progress
- [codex] Add transport recommendation use case

Rules:
- Imperative tense.
- Specific scope.
- No vague commit messages.

---

# Pull Request Rules

## PR Title

```text
[codex] <TASK-ID> Title Case Summary
```

## PR Description Template (REQUIRED)

```markdown
## <TASK-ID> Implementation Complete

Task: <ticket URL or local task reference>

### Summary
Short explanation of what was implemented.

### What Was Done
1. Data Layer
2. Domain/ViewModel Layer
3. UI Layer

### Acceptance Criteria Coverage
- [ ] AC1
- [ ] AC2
- [ ] AC3

### Manual Test Steps
1.
2.
3.

### Notes / Tradeoffs
- ...
```

---

# AI Restrictions

Codex must NOT:
- Modify authentication/security behavior without explicit instruction.
- Change Firestore schema silently.
- Introduce new dependencies without documenting why in PR.
- Hardcode production secrets or API keys.

---

# Documentation Rules

Add KDoc for non-trivial business logic (calculations, budgeting, transport decisions, measurement transforms).

---

# Pre-Merge Checklist

- [ ] Layering respected (UI -> ViewModel -> UseCase -> Repository)
- [ ] No Firebase/API usage inside Composables
- [ ] Loading / Error / Empty states handled
- [ ] Acceptance criteria covered
- [ ] Schema/security-impacting changes documented
- [ ] Commits and PR format follow this document

---

# Long-Term Goal

Codebase should stay modular, testable, and safe to evolve as SommarStugan grows from prototype to production.
