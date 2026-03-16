---
name: git-pushit
description: 'Combined stage + commit + push workflow for StugBygget. Use when: committing and pushing in one step, git pushit, ship changes, stage + commit + push.'
argument-hint: 'Optional: commit type (feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert) — omit for first commit on branch'
allowed-tools: Bash(git status), Bash(git diff --stat), Bash(git diff), Bash(git log --oneline -5), Bash(git add *), Bash(git commit -m *), Bash(git rev-parse --abbrev-ref HEAD), Bash(git rev-parse --abbrev-ref --symbolic-full-name @{u}), Bash(git push -u origin *), Bash(git push)
---

# Git Pushit — Stage + Commit + Push

Combined workflow for StugBygget. Runs the commit step and, only on success, the push step.

The optional argument is: `$ARGUMENTS`

---

## Step 1 — Follow the git-commit skill

Follow `.claude/skills/git-commit/SKILL.md` in full:

1. Run `git status` and `git diff --stat` to survey all changed files.
2. Group files by feature cohesion — stage specific files by name, never `git add .` or `git add -A`.
3. Draft the commit message following the StugBygget format:
   - **First commit on branch** → `[claude] <TICKET-ID> <Title Case Description>`
   - **Subsequent commit** → `[claude] [<type>] <imperative description>`
4. Commit with the message. Never skip hooks (`--no-verify`).

**If the commit step fails, stop. Do not proceed to push.**

---

## Step 2 — Follow the git-push skill

Only after the commit succeeds, follow `.claude/skills/git-push/SKILL.md` in full:

1. Verify the branch is not `main` or `dev` — stop and warn if so.
2. Show the last 5 commits (`git log --oneline -5`) so the user can confirm what will be pushed.
3. Check for an upstream remote:
   - No upstream → `git push -u origin <branch>`
   - Upstream exists → `git push`
4. On success, display push output and remind the user that PRs must target `dev`, not `main`.
5. On failure, diagnose the error and never force-push without explicit user confirmation.

---

## Completion checks

- [ ] Only specific files staged — no `git add .` or `git add -A`.
- [ ] Commit message starts with `[claude]` and follows the correct format for first/subsequent commits.
- [ ] No hooks skipped.
- [ ] Branch is not `main` or `dev`.
- [ ] Recent commits shown to user before push.
- [ ] Push succeeded and output displayed.
- [ ] User reminded that PR should target `dev`.
