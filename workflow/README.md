# Workflow Scripts — StugBygget

Local shell scripts that replicate the Claude Chat skill workflow, available from your terminal.

## Scripts

### Git Workflow

#### `git-commit.sh`
Commit changes following StugBygget conventions (`[agent](model)` prefix, ticket ID, type).

**Usage:**
```bash
./workflow/git-commit.sh                                    # First commit (prompts for agent/model)
./workflow/git-commit.sh feat                              # Subsequent with type
./workflow/git-commit.sh feat claude Claude-3.5-Sonnet     # With agent and model specified
./workflow/git-commit.sh fix codex Haiku-4.5
./workflow/git-commit.sh docs grok Grok-3
```

**Commit Format:**
```
First commit:      [claude](Claude-3.5-Sonnet) SB-74 Add Phase Progress Bar
Subsequent:        [claude](Claude-3.5-Sonnet) [feat] Add phase logic
```

**What it does:**
1. Shows current changes
2. Prompts for agent (claude/codex/grok) and model if not provided
3. Prompts for commit message
4. Stages specific files (not `git add .`)
5. Confirms before committing
6. Enforces `[agent](model)` format

---

#### `git-push.sh`
Push the current branch to remote with safety checks.

**Usage:**
```bash
./workflow/git-push.sh                 # Normal push
./workflow/git-push.sh --force-with-lease  # Force push (with prompt)
```

**What it does:**
1. ✅ Verifies branch is not `main` or `dev`
2. Shows last 5 commits for confirmation
3. Checks for upstream remote
4. Pushes with `-u origin <branch>` (first time) or `git push` (subsequent)
5. Reminds you PRs target `dev`

---

#### `git-pushit.sh`
Combined workflow: stage → commit → push.

**Usage:**
```bash
./workflow/git-pushit.sh                                    # First commit
./workflow/git-pushit.sh feat                              # Subsequent with type
./workflow/git-pushit.sh feat claude Claude-3.5-Sonnet     # With agent and model
```

**What it does:**
1. Runs `git-commit.sh` (with agent/model if provided)
2. Runs `git-push.sh` (only if commit succeeds)

---

#### `git-ship.sh`
Full release workflow: sync → commit → push → open PR.

**Usage:**
```bash
./workflow/git-ship.sh                                      # Auto-detects ticket ID from branch
./workflow/git-ship.sh SB-74                               # Explicit ticket ID
./workflow/git-ship.sh SB-74 claude Claude-3.5-Sonnet      # With agent and model
```

**What it does:**
1. ✅ Verifies branch is not `main`/`dev`
2. Fetches and rebases onto `origin/dev`
3. Commits any changes (prompts for agent/model if needed)
4. Pushes to remote
5. Opens a PR to `dev` (uses `gh` CLI if available)

---

### Linear Workflow

#### `linear-create-ticket.sh`
Create a new Linear ticket with proper structure.

**Usage:**
```bash
./workflow/linear-create-ticket.sh "Implement payment processing"
./workflow/linear-create-ticket.sh "Fix crash when photo uploads fail"
```

**What it does:**
1. Classifies ticket type (feature/bug/chore/spike)
2. Determines next SB number
3. Collects layer and milestone
4. Uses appropriate template (Context + Acceptance Criteria, etc.)
5. Shows you the ticket structure and copies to clipboard

---

#### `linear-implement-ticket.sh`
Set up a feature branch and implementation guide for a ticket.

**Usage:**
```bash
./workflow/linear-implement-ticket.sh SB-74
```

**What it does:**
1. Syncs with `dev` branch
2. Creates feature branch: `claude/SB-74-your-description`
3. Shows architecture layers and strict rules
4. Optionally opens VS Code
5. Ready for you to start coding

---

## Quick Start

### First-time setup

```bash
cd /Users/mrnoordh/AndroidStudioProjects/stugbygget

# Make scripts executable (already done)
chmod +x workflow/*.sh

# Test a script
./workflow/git-push.sh  # Will show your current branch status
```

### Typical Feature Implementation Workflow

```bash
# 1. Create a Linear ticket
./workflow/linear-create-ticket.sh "My new feature"

# 2. Implement the feature
./workflow/linear-implement-ticket.sh SB-75

# 3. Make changes in VS Code, then commit
./workflow/git-commit.sh feat

# 4. Ship it (sync, push, open PR)
./workflow/git-ship.sh SB-75
```

### Typical Daily Workflow

```bash
# Start with a branch
git checkout -b claude/SB-74-feature

# Make changes...

# Commit and push
./workflow/git-pushit.sh feat

# More changes...

# Another commit
./workflow/git-commit.sh fix
./workflow/git-push.sh

# Ready to ship? Final sync + PR
./workflow/git-ship.sh
```

---

## Requirements

- **zsh** (macOS default) ✅
- **git** ✅
- **`gh` CLI** (optional, for automatic PR creation)
  - Install: `brew install gh`
  - Authenticate: `gh auth login`

---

## Troubleshooting

### "Permission denied" when running a script
```bash
chmod +x workflow/*.sh
```

### Rebase conflict in `git-ship.sh`
The script will stop and tell you to resolve manually:
```bash
# Fix the conflicts in your editor
git rebase --continue
./workflow/git-ship.sh SB-74  # Re-run
```

### PR not creating automatically
The script tries to use `gh` CLI. If not installed, it opens GitHub in your browser so you can create manually.

### Script not found
Make sure you're in the repo root:
```bash
cd /Users/mrnoordh/AndroidStudioProjects/stugbygget
```

---

## Architecture Enforced by Scripts

- ✅ All commits include agent and model: `[agent](model)`
- ✅ First commits include ticket ID (SB-74)
- ✅ Subsequent commits include type (feat/fix/docs/etc.)
- ✅ No direct pushes to `main` or `dev`
- ✅ PRs always target `dev`
- ✅ Specific file staging (never `git add .`)
- ✅ Rebasing on `dev` before PR

---

## Notes

- These scripts follow **CLAUDE.md** and **ARCHITECTURE_GUARD.md** exactly
- They're safe — they prompt for confirmation at critical steps
- They're interactive — they ask what you need, not silent automation
- You can always cancel and recover (nothing destructive without confirmation)
