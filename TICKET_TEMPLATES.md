# StugBygget - Ticket Templates

Use these templates when creating tickets in Linear.

---

## Terminology Guide

| Linear Term | When to Use | Example |
|---|---|---|
| Feature | New user-facing functionality | Add AR room measurement save flow |
| Bug | Something broken or incorrect | Fix crash when photo upload fails |
| Chore | Technical work, no direct user-visible change | Set up Firebase security rules |
| Spike | Time-boxed investigation | Evaluate best source for live building material prices |

In conversation and PRs, use the word `ticket`.

---

## Feature Ticket Template

```text
Title: [Short, imperative description]
Example: "Implement planning timeline progress screen"

---

### Context
Why does this feature exist? What user problem does it solve?
Link to UX/design reference if available.

### Acceptance Criteria
- [ ] AC1: [Specific, testable outcome]
- [ ] AC2: [Specific, testable outcome]
- [ ] AC3: [Specific, testable outcome]

### Scope Boundary
In scope:
- [What is included]

Out of scope:
- [What is explicitly not included]

### UI/UX Reference
- Figma link or screenshot (if available)
- Relevant module: planning / todos / gallery / ai-chat / room-planner / ar-measure / materials / shopping / budget / logistics
- Key interaction decision(s)

### Technical Notes
- Layer: UI only / Full stack / Backend only
- Dependencies: [Ticket IDs]
- Data source: Mock data / Firebase / External API / Cloud Functions

### Edge Cases to Handle
- [ ] Loading state
- [ ] Empty state
- [ ] Error state
- [ ] Offline or permission-denied behavior (if relevant)
- [ ] Feature-specific edge case(s)

### Labels
Priority: Urgent / High / Medium / Low
Type: feature
Layer: frontend / backend / fullstack
Milestone: MVP / Post-MVP / Beta
```

---

## Bug Ticket Template

```text
Title: Fix [what's broken]
Example: "Fix duplicate todos when reopening timeline screen"

---

### What Happens
[Actual behavior]

### What Should Happen
[Expected behavior]

### Steps to Reproduce
1. [Step 1]
2. [Step 2]
3. [Step 3]

### Environment
- Device: [e.g. Pixel 8 emulator]
- Android version: [e.g. API 34]
- App branch/build: [e.g. codex/SOM-18-budget-overview]

### Stacktrace / Logs
[Paste relevant error output]

### Labels
Priority: Urgent / High / Medium / Low
Type: bug
Layer: frontend / backend / fullstack
```

---

## Chore Ticket Template

```text
Title: [Technical task description]
Example: "Configure Firestore indexes for shopping and price queries"

---

### Context
Why is this technical work needed? What does it enable?

### Done When
- [ ] [Concrete, verifiable outcome]
- [ ] [Concrete, verifiable outcome]

### Technical Notes
- [Relevant details, docs, constraints]

### Labels
Priority: High / Medium / Low
Type: chore
Layer: infra / frontend / backend
Milestone: MVP / Post-MVP
```

---

## Spike Ticket Template

```text
Title: Spike: [What are we investigating?]
Example: "Spike: Evaluate data source strategy for Swedish building material prices"

---

### Question to Answer
[Specific question this spike must resolve]

### Time Box
[Maximum time, e.g. 2 hours or half day]

### Output
- [ ] Written findings (comment on ticket)
- [ ] Recommendation with pros/cons
- [ ] Follow-up tickets created

### Labels
Priority: Medium / Low
Type: spike
```

---

## Quick Quality Check

Good ticket traits:
- Clear title and measurable ACs
- Explicit scope and out-of-scope
- Defined layer and data source
- Edge cases listed
- Dependencies linked

Bad ticket traits:
- Vague goals ("make it work")
- No scope boundaries
- No verification criteria
- Hidden assumptions

Senior test:
Could a developer new to the repo build it correctly and know when it is done?
