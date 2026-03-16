---
name: git-ship
description: "Ship a completed feature branch end-to-end: sync with dev, commit, push, and open a PR. Use when: shipping a finished feature, sending code for review, committing and pushing a completed change, open a PR."
argument-hint: "Optional: PR description notes or Linear ticket ID (e.g. SB-74)"
allowed-tools: Bash(git status), Bash(git diff --stat), Bash(git diff), Bash(git log --oneline *), Bash(git add *), Bash(git commit -m *), Bash(git rev-parse --abbrev-ref HEAD), Bash(git rev-parse --abbrev-ref --symbolic-full-name @{u}), Bash(git fetch origin), Bash(git rebase origin/dev), Bash(git push -u origin *), Bash(git push), Bash(gh pr create *), Bash(gh pr view *)
---

# Git Ship — Sync + Commit + Push + PR

Ships the current `claude/SB<ID>-...` feature branch to remote and opens a PR targeting `dev`.
Run each step sequentially — do not skip steps.

The optional argument is: `$ARGUMENTS`

---

## Step 1 — Verify the branch

```bash
git rev-parse --abbrev-ref HEAD
```

- If the branch is `main` or `dev`, **stop and warn the user** — direct pushes to protected branches are not allowed.
- Confirm the branch follows the `claude/SB<ID>-...` convention. If it doesn't, note it to the user but still proceed.
- Extract the ticket ID from the branch name (e.g. `SB74` from `claude/SB74-add-phase-progress-bar`) — you will need it for the PR.

---

## Step 2 — Sync with dev

Fetch and rebase onto `origin/dev` to pull in any new changes before committing or pushing. This prevents conflicts in the PR.

```bash
git fetch origin
git rebase origin/dev
```

If the rebase has conflicts, **stop and report the conflicting files to the user**. Do not auto-resolve conflicts. Ask the user to resolve them before continuing.

---

## Step 3 — Commit all changes

Follow `.claude/skills/git-commit/SKILL.md` in full:

1. Run `git status` and `git diff --stat` to survey all changed files.
2. If the working tree is **already clean** (nothing to commit), skip to Step 4.
3. Group files by feature cohesion — stage specific files by name, never `git add .` or `git add -A`.
4. Draft and apply the correct commit message:
   - **First commit on branch** → `[claude] <TICKET-ID> <Title Case Description>`
   - **Subsequent commit** → `[claude] [<type>] <imperative description>`
5. Never skip hooks (`--no-verify`).

**If the commit fails, stop. Do not proceed.**

---

## Step 4 — Push the branch

Follow `.claude/skills/git-push/SKILL.md` in full:

1. Show the last 5 commits (`git log --oneline -5`) so the user can confirm what will be pushed.
2. Verify all commits follow the `[claude]` prefix — note any that don't.
3. Push:
   - No upstream set → `git push -u origin <branch>`
   - Upstream already set → `git push`
4. On failure, diagnose the error and **never force-push without explicit user confirmation**.

**If the push fails, stop. Do not proceed to the PR step.**

---

## Step 5 — Open a PR to dev

Create the PR using `gh pr create` with the CLAUDE.md template. Target branch is always `dev`.

Derive the PR title and body from:
- The ticket ID extracted in Step 1 (or from `$ARGUMENTS` if provided)
- The branch commits (`git log --oneline origin/dev..HEAD`)
- What was implemented

### PR Title format

```
[claude] <TICKET-ID> <Title Case Summary>
```

Example: `[claude] SB74 Add Phase Progress Bar`

### PR Body template

```markdown
## <TICKET-ID> Implementation Complete

Task: <Linear ticket URL if known, otherwise omit>

### Summary
<One to two sentences describing what was implemented and why.>

### What Was Done
1. **Data Layer**
   - <item or "Not touched">

2. **Domain / ViewModel Layer**
   - <item or "Not touched">

3. **UI Layer**
   - <item or "Not touched">

### Acceptance Criteria Coverage
- [ ] <AC1 description>
- [ ] <AC2 description>

### Manual Test Steps
1. <Step 1>
2. <Step 2>

### Notes / Tradeoffs
- <Any relevant notes, UI-only placeholders, deferred work, or schema changes>
```

Omit layers that were not touched. Fill in as much detail as possible from the commit history and changed files.

Run:

```bash
gh pr create --base dev --title "[claude] <TICKET-ID> <Title>" --body "<body>"
```

After the PR is created, display the PR URL to the user.

---

## Completion checks

- [ ] Branch is not `main` or `dev`.
- [ ] Branch synced with `origin/dev` via rebase — no conflicts.
- [ ] Only specific files staged — no `git add .` or `git add -A`.
- [ ] Commit message starts with `[claude]` and follows the correct format.
- [ ] No hooks skipped.
- [ ] Push succeeded and output displayed.
- [ ] PR created targeting `dev` with the CLAUDE.md template.
- [ ] PR URL displayed to user.
