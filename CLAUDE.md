# CLAUDE.md - AI-Assisted Development Workflow
StugBygget (Android)

> This document defines how Claude Code should work in this repository.
> Claude Code works interactively with the developer — changes are reviewed in real time before committing.

---

# Project Overview

StugBygget is an Android app for coordinating a Swedish summer cottage renovation (summer 2026).

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
- AI Assistant: Claude Code (interactive CLI)

---

# How Claude Code Works Here

Claude Code is an interactive tool — it reads, edits, and runs code **together with the developer** in a live conversation. This is different from batch-style agents that run unattended.

Guidelines for our collaboration:
- Claude reads existing code before suggesting or making changes.
- Changes are shown to the developer before committing — nothing is pushed silently.
- If a task is ambiguous, Claude asks for clarification rather than guessing.
- Scope stays tight: only implement what is discussed and agreed upon.
- If a task is UI-only, use mock/placeholder data and add `// TODO: wire to backend` markers.

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

# Branch Naming

Format:

```text
claude/<TASK-ID>-short-description
```

Examples:
- claude/SB12-planning-timeline-progress
- claude/SB21-shopping-budget-sync

---

# Commit Rules

All commits made with Claude Code assistance must start with:

```text
[claude]
```

Examples:
- [claude] SB12 Implement planning timeline progress
- [claude] Add transport recommendation use case

Rules:
- Imperative tense.
- Specific scope.
- No vague commit messages.

---

# Pull Request Rules

## PR Title

```text
[claude] <TASK-ID> Title Case Summary
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

# Claude Code Restrictions

Claude must NOT:
- Modify authentication/security behavior without explicit instruction and developer confirmation.
- Change Firestore schema silently — always discuss and document schema changes first.
- Introduce new dependencies without explaining why and getting approval.
- Hardcode production secrets or API keys.
- Push to remote or create PRs without explicit developer instruction.
- Take destructive actions (delete files, reset branches, force push) without confirmation.

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

Codebase should stay modular, testable, and safe to evolve as StugBygget grows from prototype to production.
