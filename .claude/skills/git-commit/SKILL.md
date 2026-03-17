---
name: git-commit
description: 'Commit message formatting for StugBygget. Use when: committing changes, writing a commit message, staging and committing files, first commit on a branch, subsequent commits, git commit -m.'
argument-hint: 'Optional: commit type (feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert) — omit for first commit on branch'
---

# Git Commit Format

All commits made with Claude Code assistance must start with the `[claude]` prefix. This is a strict project rule from CLAUDE.md.

## First Commit on a Branch

Mirror the branch ticket ID and use a Title Case description:

```
[claude] <TICKET-ID> <Title Case Description>
```

Example (branch `claude/NOO-179-add-unit-test-for-login-screen`):
```
[claude] NOO-179 Add Unit Test For Login Screen
```

## Subsequent Commits

```
[claude] [<type>] <imperative description>
```

Examples:
```
[claude] [feat] Add login ViewModel unit test
[claude] [fix] Fix coroutine scope in LoginViewModel
[claude] [chore] Add JUnit4 and Turbine test dependencies
```

## Commit Types

| Type | When |
|------|------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no logic change |
| `refactor` | Rename/restructure, no behaviour change |
| `test` | Tests only |
| `chore` | Non-src, non-test changes (deps, config) |
| `perf` | Performance improvement |
| `ci` | CI/CD pipeline changes |
| `build` | Build system or dependency changes |
| `revert` | Reverts a previous commit |

## Multi-Commit Strategy for Large Changesets

When changes span multiple unrelated concerns, split into focused commits instead of one large commit.

1. Run `git status` and `git diff --stat` to survey all changed files.
2. Group files by **feature cohesion** — files that changed for the same reason belong together.
3. Order logically: foundational changes first (config, deps), then features, then docs/cleanup.
4. Stage each group explicitly with `git add <files>` — never `git add .` when splitting.

**Split when** changes mix features, bug fixes, refactors, CI, or docs.
**Stay in one commit when** all files serve a single logical change.

In a multi-commit flow, the **first** commit still uses the ticket mirror format; remaining commits use the type prefix format.

## Rules

- Prefix is always `[claude]` — never omit it, never use a model name
- Use imperative mood: "Add feature" not "Added feature"
- Keep the subject line under 72 characters — no body, no bullet points
- First commit on a branch = ticket mirror (no type prefix); all subsequent = type prefix required
- Never skip hooks (`--no-verify`) unless explicitly instructed by the developer
- Stage specific files by name — avoid `git add -A` or `git add .` to prevent accidentally including secrets or binaries
