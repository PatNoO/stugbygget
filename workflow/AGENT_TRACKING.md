# Agent & Model Tracking Implementation

## Overview

Your workflow scripts now automatically track **which AI agent and model** implemented each commit. Instead of just `[claude]`, commits now show:

```
[claude](Claude-3.5-Sonnet) SB-74 Add Feature
[codex](GPT-5.2) [feat] Implement logic
[grok](Grok-3) [fix] Fix bug
```

## Updated Scripts

All six workflow scripts have been updated to support agent/model tracking:

### Git Workflow Scripts
- ✅ `git-commit.sh` — Prompts for agent/model or accepts them as arguments
- ✅ `git-push.sh` — Unchanged (only pushes, doesn't commit)
- ✅ `git-pushit.sh` — Passes agent/model to `git-commit.sh`
- ✅ `git-ship.sh` — Passes agent/model to `git-commit.sh`

### Linear Workflow Scripts
- ✅ `linear-create-ticket.sh` — Unchanged (Linear tracking is separate)
- ✅ `linear-implement-ticket.sh` — Unchanged (sets up branch)

## Usage

### Option 1: Automatic Prompting (Interactive)
```bash
./workflow/git-commit.sh feat
# Will prompt:
# 🤖 Which agent implemented this?
#   1) claude
#   2) codex
#   3) grok
# Select agent (1-3): _
# Enter model name (e.g., Claude-3.5-Sonnet, Haiku-4.5): _
```

### Option 2: Specify Agent & Model Upfront (Non-Interactive)
```bash
./workflow/git-commit.sh feat claude Claude-3.5-Sonnet
./workflow/git-pushit.sh fix codex Haiku-4.5
./workflow/git-ship.sh SB-74 grok Grok-3
```

## Commit Message Format

### First Commit on Branch
```
[agent](model) TICKET-ID Message

Example:
[claude](Claude-3.5-Sonnet) SB-74 Add Phase Progress Bar
```

### Subsequent Commits
```
[agent](model) [type] Message

Examples:
[claude](Claude-3.5-Sonnet) [feat] Add phase logic
[codex](Haiku-4.5) [fix] Fix modal layout
[grok](Grok-3) [docs] Update README
```

## For AI Agents

When using these scripts from Claude, Codex, or Grok:

1. **Identify yourself**: Determine which agent you are (claude/codex/grok)
2. **Know your model**: Know which model version you are (e.g., Claude-3.5-Sonnet)
3. **Pass it explicitly**: Always pass agent and model to avoid prompts
   ```bash
   ./workflow/git-commit.sh feat claude Claude-3.5-Sonnet
   ./workflow/git-ship.sh SB-74 claude Claude-3.5-Sonnet
   ```

## Files Modified

- `workflow/git-commit.sh` — Added agent/model parameter parsing and prompting
- `workflow/git-pushit.sh` — Updated to pass agent/model to git-commit
- `workflow/git-ship.sh` — Updated to pass agent/model to git-commit
- `workflow/README.md` — Updated documentation with new format
- `workflow/QUICKREF.md` — Updated quick reference with examples

## Backward Compatibility

✅ Scripts still work without agent/model (they prompt)  
✅ No breaking changes to existing workflows  
✅ Optional parameters for flexibility  

## Example Workflow

```bash
# As Claude, implementing SB-74
./workflow/linear-implement-ticket.sh SB-74

# Make changes in VS Code...

# Commit with auto-identification
./workflow/git-commit.sh feat claude Claude-3.5-Sonnet

# More changes...

# Fix something
./workflow/git-commit.sh fix claude Claude-3.5-Sonnet

# Ship everything with sync + push + PR
./workflow/git-ship.sh SB-74 claude Claude-3.5-Sonnet
```

This results in a commit history like:
```
[claude](Claude-3.5-Sonnet) SB-74 Add Phase Progress Bar
[claude](Claude-3.5-Sonnet) [feat] Add phase calculation logic
[claude](Claude-3.5-Sonnet) [fix] Fix modal styling issue
```

Perfect for tracking which AI implemented what!
