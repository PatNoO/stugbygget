---
name: feedback_commit_messages
description: Keep commit messages short — one line summary only, no body
type: feedback
---

Keep commit messages short. One imperative-tense subject line only.

**Why:** User explicitly said commit messages are too long — the PR description explains the details.

**How to apply:** Never add a body paragraph to git commits. Just the `[claude] SB## Short summary` subject line, then the `Co-Authored-By` trailer on the next line.
