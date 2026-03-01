# CODE_REVIEW_GUIDELINES.md

This document defines how code reviews are performed in this repository.

Goals:
- Protect architecture
- Ensure functional stability
- Keep code understandable
- Prevent technical debt

---

## Review Principles

Reviews must be:
- Professional
- Constructive
- Objective
- Actionable
- Concise

Every critical suggestion should include:
1. What is wrong
2. Why it matters
3. Minimal correction direction

---

## Review Categories

### 1. Architecture
- Does code follow `UI -> ViewModel -> UseCase -> Repository`?
- Any Firebase/API usage in UI? (Blocker)
- Any domain logic inside Composables? (Blocker)
- Are feature boundaries respected?

### 2. State Management
- Is UiState explicit and stable?
- Is MutableStateFlow private?
- Are loading/error/empty states handled?
- Are one-time events handled safely?

### 3. Data & Firebase
- Is Firestore schema respected (`projects/{projectId}`)?
- Are reads/writes validated and safe?
- Any undocumented schema/rule changes? (Blocker)

### 4. Reliability & Error Handling
- Are failures caught in ViewModel/use-case boundaries?
- Are user-facing errors understandable?
- Are edge cases covered (empty, offline, missing permissions, unavailable data)?

### 5. Product Scope
- Does implementation match task acceptance criteria exactly?
- Any unapproved scope expansion? (Blocker)

---

## Severity Levels

### Blocker
Must be fixed before merge.
Examples:
- Architecture violation
- Firebase/API in UI
- Silent schema changes
- Broken acceptance criteria

### Strong Suggestion
Should be improved before merge when practical.
Examples:
- Weak state modeling
- Unclear error handling
- Hard-to-maintain structure

### Suggestion
Optional improvement.
Examples:
- Small readability refactor
- Naming or decomposition tweak

---

## Merge Readiness Output

Each review should end with:
- Risk Level: Low / Medium / High
- Merge Readiness: Approved / Needs Changes
