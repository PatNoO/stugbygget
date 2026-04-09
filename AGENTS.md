# AGENTS.md — AI Agents in StugBygget

This document defines which AI agents are used in this project, what they are responsible for, and how they should behave. All agents follow the same architecture rules defined in `CLAUDE.md` and `ARCHITECTURE_GUARD.md`.

---

## Active Agents

### Claude (Anthropic)
- **Tag:** `[claude]`
- **Models used:** Claude-Sonnet-4.6
- **Primary role:** Feature implementation, architecture, code reviews, Linear ticket management
- **Works via:** Claude Code CLI (interactive), VS Code Copilot Chat
- **Skills:** `.claude/skills/` — git-commit, git-push, git-pushit, git-ship, linear-create-ticket, linear-implement-ticket

### Copilot (GitHub / Microsoft)
- **Tag:** `[copilot]`
- **Models used:** GitHub-Copilot, Copilot-Chat *(exact model not exposed)*
- **Primary role:** Code generation, refactoring, workflow scripts, in-editor assistance
- **Works via:** VS Code GitHub Copilot Chat

### Grok (xAI)
- **Tag:** `[grok]`
- **Models used:** Grok-3 *(update as needed)*
- **Primary role:** *(update as needed)*
- **Works via:** *(update as needed)*

---

## Commit Tagging Convention

Every AI-assisted commit must be tagged with the agent and exact model that implemented it.

### Format

```
[agent](Model-Version) TICKET-ID Title Case Description    ← first commit on branch
[agent](Model-Version) [type] imperative description       ← subsequent commits
```

### Examples

```
[claude](Claude-Sonnet-4.6) SB-74 Add Phase Progress Bar
[claude](Claude-Sonnet-4.6) [feat] Add phase calculation logic
[copilot](GitHub-Copilot) [fix] Fix modal layout on small screens
[grok](Grok-3) [refactor] Simplify budget use case
```

### Commit Types

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

---

## Rules All Agents Must Follow

These rules apply to **every** agent, regardless of which tool they are invoked from.

### Architecture (STRICT)
- Follow `UI -> ViewModel -> UseCase -> Repository -> Data Source`
- No Firebase/API imports inside Composables — ever
- No business logic in Composables
- ViewModel must not call SDKs directly — go through UseCases or Repositories
- Domain models must be framework-independent

### Scope
- Only implement what has been discussed and agreed upon with the developer
- If a task is ambiguous, ask for clarification — do not guess
- UI-only features: use mock/placeholder data and add `// TODO: wire to backend` markers

### Firebase & Schema
- Do not change Firestore schema without discussing and documenting it in `docs/firestore_schema.md` first
- Validate input before every write
- Keep reads side-effect free

### Security
- Do not modify authentication or security rules without explicit developer confirmation
- Never hardcode production secrets or API keys

### Git & PRs
- Never push to `main` or `dev` directly — always use a `claude/SB<ID>-...` feature branch
- Never force-push without explicit developer confirmation
- Never skip commit hooks (`--no-verify`)
- PRs always target `dev`, never `main`
- PR title format: `[agent] TICKET-ID Title Case Summary` *(no model in PR title)*

### Transparency
- Show changes to the developer before committing — nothing silently committed
- Do not take destructive actions (delete files, reset branches, force push) without confirmation

---

## Workflow Scripts

All agents use the same workflow scripts in `workflow/`:

```bash
./workflow/git-commit.sh [type] [agent] [model]    # Commit with tagging
./workflow/git-push.sh                              # Push to remote
./workflow/git-pushit.sh [type] [agent] [model]    # Commit + push
./workflow/git-ship.sh [ticket-id] [agent] [model] # Sync + commit + push + PR
./workflow/linear-create-ticket.sh "description"   # Create Linear ticket
./workflow/linear-implement-ticket.sh SB-74         # Set up implementation branch
```

See `workflow/README.md` for full documentation and `workflow/QUICKREF.md` for quick reference.

---

## Adding a New Agent

When a new AI agent is introduced to this project:

1. Add it to the **Active Agents** section above with tag, models, role, and tool
2. Update `workflow/git-commit.sh` to include the new agent name in the selection list
3. Make sure the agent is aware of `CLAUDE.md`, `ARCHITECTURE_GUARD.md`, and this file before starting work
4. Test a commit to verify the tagging format is correct

---

## Related Documents

| Document | Purpose |
|----------|---------|
| `CLAUDE.md` | Detailed workflow rules for Claude Code |
| `ARCHITECTURE_GUARD.md` | Architectural invariants all agents must respect |
| `CODE_REVIEW_GUIDELINES.md` | How code reviews are performed |
| `workflow/README.md` | Workflow scripts documentation |
| `workflow/QUICKREF.md` | Quick command reference |
| `docs/firestore_schema.md` | Firestore schema documentation |
