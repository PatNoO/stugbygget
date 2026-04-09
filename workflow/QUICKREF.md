# Workflow Scripts — Quick Reference

## One-Liners

```bash
# Git workflow — always pass <agent> <model> first
./workflow/git-commit.sh copilot GitHub-Copilot           # first commit on branch
./workflow/git-commit.sh copilot GitHub-Copilot feat      # subsequent commit
./workflow/git-push.sh                                     # push to remote (no agent needed)
./workflow/git-pushit.sh copilot GitHub-Copilot feat      # commit + push
./workflow/git-ship.sh copilot GitHub-Copilot SB-74       # sync + commit + push + PR

# With Claude
./workflow/git-commit.sh claude Claude-3.5-Sonnet feat
./workflow/git-ship.sh claude Claude-3.5-Sonnet SB-74

# Linear workflow
./workflow/linear-create-ticket.sh "Feature description"
./workflow/linear-implement-ticket.sh SB-74
```

## Commit Format

```
[agent](model) TICKET-ID Action
[agent](model) [type] description

Examples:
[claude](Claude-3.5-Sonnet) SB-74 Add Phase Progress Bar
[codex](Haiku-4.5) [feat] Add phase logic
[grok](Grok-3) [fix] Fix modal layout bug
```

## Common Workflows

### I just want to push my changes
```bash
./workflow/git-pushit.sh feat
# Prompts: Select agent (1-3), Enter model name
```

### I want to push with specific agent/model
```bash
./workflow/git-pushit.sh feat claude Claude-3.5-Sonnet
# No prompts, commits and pushes immediately
```

### I'm done with a feature, ship it
```bash
./workflow/git-ship.sh SB-74
# Prompts for agent/model if there are changes to commit
```

### I want to ship with specific agent/model
```bash
./workflow/git-ship.sh SB-74 codex Haiku-4.5
# Uses specified agent and model
```

### I need to create a new ticket
```bash
./workflow/linear-create-ticket.sh "My new feature"
```

### I'm starting work on a ticket
```bash
./workflow/linear-implement-ticket.sh SB-75
```

### Multiple commits, then ship
```bash
./workflow/git-commit.sh feat claude Claude-3.5-Sonnet  # First feature
./workflow/git-push.sh
# make more changes...
./workflow/git-commit.sh fix claude Claude-3.5-Sonnet   # Bug fix
./workflow/git-push.sh
# ready? sync and PR
./workflow/git-ship.sh SB-74
```

## Commit Types

| Type | Usage |
|------|-------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `style` | Formatting (no logic) |
| `refactor` | Restructure (no behavior change) |
| `test` | Tests only |
| `chore` | Dependencies, config |
| `perf` | Performance |
| `ci` | CI/CD pipeline |
| `build` | Build system |
| `revert` | Revert a commit |

## What Each Script Does

| Script | Purpose | Checks |
|--------|---------|--------|
| `git-commit.sh` | Format + stage + commit | `[claude]` prefix, ticket ID, type |
| `git-push.sh` | Push to remote | No `main`/`dev` pushes, shows commits |
| `git-pushit.sh` | Commit + push | Runs both above in sequence |
| `git-ship.sh` | Full release | Sync + commit + push + **PR** |
| `linear-create-ticket.sh` | Create Linear ticket | Type, SB#, layer, milestone, template |
| `linear-implement-ticket.sh` | Set up implementation | Branch creation, architecture guide |

## Safety Features

✅ Prompts before every destructive action  
✅ Prevents direct pushes to `main`/`dev`  
✅ Stops if any step fails (commit fails → no push)  
✅ Shows what will be pushed before pushing  
✅ Detects rebase conflicts before PR  
✅ Never force-pushes without user confirmation  
✅ Tracks which AI agent implemented each commit

## Tips

- Use `./workflow/git-ship.sh` to do everything at once
- Always stage specific files (script prompts for them)
- First commit on a branch auto-uses ticket format with agent/model
- Subsequent commits need a type (`feat`, `fix`, etc.)
- Can pass agent and model as arguments to avoid prompts
- Each commit is tagged with the AI model that implemented it
- Read the script output — it guides you at each step
