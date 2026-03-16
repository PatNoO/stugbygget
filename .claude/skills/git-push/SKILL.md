---
name: git-push
description: 'Use when: pushing the current branch to remote, publishing local commits, or asked to push code. Checks for an upstream remote and sets one if missing, then pushes with clear success/failure output. Never force-pushes without explicit confirmation.'
argument-hint: 'Optional: --force-with-lease (only with explicit user confirmation)'
allowed-tools: Bash(git rev-parse --abbrev-ref HEAD), Bash(git rev-parse --abbrev-ref --symbolic-full-name @{u}), Bash(git push -u origin *), Bash(git push), Bash(git log --oneline -5), Bash(git status)
---

# Git Push — StugBygget

Push the current `claude/SB*` branch to `origin`, following StugBygget branch conventions.

The optional argument is: `$ARGUMENTS`

---

## Step 1 — Verify the current branch

```bash
git rev-parse --abbrev-ref HEAD
```

- If the branch is `main` or `dev`, **stop and warn the user** — direct pushes to `main`/`dev` are not allowed. All work must be on a `claude/SB<ID>-...` feature branch.
- If the branch follows the `claude/SB<ID>-...` convention, proceed.
- If the branch name does not follow the convention, note it to the user but still proceed (it may be a one-off branch).

---

## Step 2 — Show recent commits being pushed

```bash
git log --oneline -5
```

Display these to the user so they can confirm what is about to be pushed. Verify that commits follow the `[claude]` prefix convention required by CLAUDE.md. If any commits are missing the `[claude]` prefix, note this to the user before pushing.

---

## Step 3 — Check for an upstream remote

```bash
git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null
```

- **Output is empty** → no upstream is set → use Step 4a.
- **Output shows a remote** (e.g. `origin/claude/SB74-...`) → skip to Step 4b.

---

## Step 4a — Push and set upstream (first push)

```bash
git push -u origin $(git rev-parse --abbrev-ref HEAD)
```

---

## Step 4b — Push (upstream already set)

```bash
git push
```

---

## Step 5 — Confirm or report failure

On **success**: show the push output (remote URL, branch name, commit range pushed) and remind the user that PRs should target the `dev` branch (not `main`).

On **failure**, diagnose the error:

| Error | Likely cause | Suggested fix |
|-------|-------------|---------------|
| `rejected … non-fast-forward` | Remote has commits not present locally | Run `git pull --rebase origin dev` first, then push again |
| `remote: Permission denied` | SSH key or token not configured | Check SSH keys or GitHub token scopes |
| `error: failed to push some refs` | Force-push needed after a rebase | Ask user for explicit confirmation before using `--force-with-lease` |
| `Updates were rejected` | Branch protection on `dev`/`main` | Confirm you are not pushing directly to a protected branch |

> ⚠️ **Never force-push without explicit user confirmation.** Present the error and ask before using `--force` or `--force-with-lease`. Never force-push to `main` or `dev`.

---

## Completion checks

- [ ] Branch is not `main` or `dev` — push only from feature branches.
- [ ] Recent commits shown to user before push.
- [ ] Commits follow `[claude] SB<ID> ...` convention (note any that don't).
- [ ] Push succeeded and output displayed.
- [ ] User reminded that the PR should target `dev`.
