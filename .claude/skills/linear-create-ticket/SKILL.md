---
name: linear-create-ticket
description: "Create a Linear ticket end-to-end: classify type, resolve team/project/labels, draft title and description from the canonical template, then post the ticket. Use when asked to create, open, or add a Linear ticket (e.g. 'Create a ticket for …')."
argument-hint: "Short description of what the ticket is for"
allowed-tools: mcp__linear__list_teams, mcp__linear__list_projects, mcp__linear__list_issue_labels, mcp__linear__list_issue_statuses, mcp__linear__save_issue, mcp__linear__list_users
disable-model-invocation: false
---

# Create Linear Ticket

Use this skill to create a well-structured Linear ticket following StugBygget conventions from `TICKET_TEMPLATES.md`.

Expect the request phrasing to be like: "Create a ticket for …", "Open a Linear ticket for …", "Add a ticket for …"

The request is: `$ARGUMENTS`

---

## Step 1 — Classify the ticket

Determine the ticket type from the user's request:

| Type | When to use | Example title |
|------|-------------|---------------|
| `feature` | New user-facing functionality | `Implement planning timeline progress screen` |
| `bug` | Something broken or incorrect | `Fix crash when photo upload fails` |
| `chore` | Technical work, no direct user-visible change | `Set up Firebase security rules` |
| `spike` | Time-boxed investigation | `Spike: Evaluate best source for live building material prices` |

In tickets, PRs, and conversation always use the word `ticket` (not "issue" or "task").

---

## Step 2 — Resolve team and project

Use `mcp__linear__list_teams` to get available teams.
Use `mcp__linear__list_projects` to look up the right project if the user mentions one.

Default team: **StugBygget** (or the first team found if the workspace only has one).
Default project: **StugBygget** — always assign every ticket to the StugBygget project unless the user explicitly specifies a different one.

---

## Step 3 — Resolve labels and statuses

Use `mcp__linear__list_issue_labels` to find the correct label IDs matching:
- **Type**: feature / bug / chore / spike
- **Layer**: frontend / backend / fullstack / infra (infer from context)
- **Milestone**: MVP / Post-MVP / Beta (default to MVP if not mentioned)

Use `mcp__linear__list_issue_statuses` to confirm the "Backlog" status ID for the team.

---

## Step 4 — Draft the ticket fields

### Title

Short, action-oriented, imperative.

- Feature: `<verb> <subject>` — e.g. `Implement planning timeline progress screen`
- Bug: `Fix <what's broken>` — e.g. `Fix duplicate todos when reopening timeline screen`
- Chore: `<Technical task description>` — e.g. `Configure Firestore indexes for shopping and price queries`
- Spike: `Spike: <What are we investigating?>` — e.g. `Spike: Evaluate data source strategy for Swedish building material prices`

### Description (Markdown)

Use the template matching the ticket type below. Fill in all sections from the user's request; use placeholder text only when a section is genuinely unknown. Do not remove sections.

---

#### Feature template

```markdown
### Context
<Why does this feature exist? What user problem does it solve?>
<Link to UX/design reference if available.>

### Acceptance Criteria
- [ ] AC1: <Specific, testable outcome>
- [ ] AC2: <Specific, testable outcome>
- [ ] AC3: <Specific, testable outcome>

### Scope Boundary
In scope:
- <What is included>

Out of scope:
- <What is explicitly not included>

### UI/UX Reference
- <Figma link or screenshot if available, otherwise omit line>
- Relevant module: <planning / todos / gallery / ai-chat / room-planner / ar-measure / materials / shopping / budget / logistics>
- Key interaction decision(s): <describe>

### Technical Notes
- Layer: <UI only / Full stack / Backend only>
- Dependencies: <Ticket IDs or "none">
- Data source: <Mock data / Firebase / External API / Cloud Functions>

### Edge Cases to Handle
- [ ] Loading state
- [ ] Empty state
- [ ] Error state
- [ ] Offline or permission-denied behavior (if relevant)
- [ ] <Feature-specific edge case(s)>
```

---

#### Bug template

```markdown
### What Happens
<Actual behavior>

### What Should Happen
<Expected behavior>

### Steps to Reproduce
1. <Step 1>
2. <Step 2>
3. <Step 3>

### Environment
- Device: <e.g. Pixel 8 emulator>
- Android version: <e.g. API 34>
- App branch/build: <e.g. claude/SB74-add-phase-progress-bar>

### Stacktrace / Logs
<Paste relevant error output, or "Not yet captured">
```

---

#### Chore template

```markdown
### Context
<Why is this technical work needed? What does it enable?>

### Done When
- [ ] <Concrete, verifiable outcome>
- [ ] <Concrete, verifiable outcome>

### Technical Notes
- <Relevant details, docs, constraints>
```

---

#### Spike template

```markdown
### Question to Answer
<Specific question this spike must resolve>

### Time Box
<Maximum time, e.g. 2 hours or half day>

### Output
- [ ] Written findings (comment on ticket)
- [ ] Recommendation with pros/cons
- [ ] Follow-up tickets created
```

---

### Priority

| User says | Priority |
|-----------|----------|
| critical / urgent / blocking | Urgent (1) |
| high / important | High (2) |
| normal / medium (default) | Medium (3) |
| low / nice-to-have / backlog | Low (4) |

---

## Step 5 — Create the ticket

Call `mcp__linear__save_issue` with the resolved fields:

```
title        → drafted title (step 4)
description  → drafted description (step 4)
team         → resolved team name or ID (step 2)
project      → resolved project name or ID (step 2, if applicable)
labels       → resolved label names or IDs (step 3)
state        → "Backlog" (step 3)
priority     → mapped priority number (step 4)
```

---

## Step 6 — Report back

After creating the ticket, reply with:

```
✅ Ticket created: <TICKET-ID> — <Title>
🔗 <Linear URL>

**Type:** <feature / bug / chore / spike>
**Priority:** <priority>
**Labels:** <labels>
**Team:** <team>

**Suggested branch name:** claude/<TICKET-ID-lowercase>-short-description
```

The suggested branch name must follow CLAUDE.md convention:
`claude/<TASK-ID>-short-description` — e.g. `claude/SB74-add-phase-progress-bar`

If the user wants the ticket assigned, use `mcp__linear__list_users` to resolve the user ID before calling `save_issue`.

---

## Branching logic

| Condition | Action |
|-----------|--------|
| Figma / design link provided | Add to **UI/UX Reference** section |
| UI-only scope | Set Layer to `UI only`; note mock data + `// TODO: wire to backend` |
| Bug report | Use bug template; fill Steps to Reproduce from user's description |
| Firestore schema change | Add note in Technical Notes: `Schema change — document in PR before merging` |
| User omits priority | Default to **Medium (3)** |
| User omits team | Default to **StugBygget** |
| User omits project | Default to **StugBygget** project |
| User provides related ticket ID | Add `Dependencies: SB-XXX` in Technical Notes |

---

## Completion checks

The workflow is complete only when all are true:

- [ ] Ticket type classified and correct type/layer/milestone labels resolved.
- [ ] Title matches the imperative format for the ticket type.
- [ ] Correct template used (feature / bug / chore / spike) with no sections removed.
- [ ] Bug tickets include Steps to Reproduce, Environment, and Stacktrace sections.
- [ ] UI-only tickets note mock data and TODO markers.
- [ ] Firestore schema changes flagged in Technical Notes.
- [ ] Ticket posted successfully and Linear URL reported back.
- [ ] Suggested branch name (following `claude/SB<ID>-...` convention) provided.
