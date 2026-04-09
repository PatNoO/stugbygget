#!/bin/zsh
#
# git-ship.sh
# Full workflow: sync with dev → commit → push → open PR
#
# Usage: ./workflow/git-ship.sh <agent> <model> [ticket-id]
#
# Examples:
#   ./workflow/git-ship.sh copilot GitHub-Copilot
#   ./workflow/git-ship.sh copilot GitHub-Copilot SB-74
#   ./workflow/git-ship.sh claude Claude-3.5-Sonnet SB-74
#

set -e

AGENT="${1:-}"
MODEL="${2:-}"
TICKET_ID="${3:-}"

if [ -z "$AGENT" ] || [ -z "$MODEL" ]; then
  echo "❌ Usage: ./workflow/git-ship.sh <agent> <model> [ticket-id]"
  echo "   Example: ./workflow/git-ship.sh copilot GitHub-Copilot SB-74"
  echo "   See AGENTS.md for valid agents and models"
  exit 1
fi

echo "🚀 Git Ship: Sync → Commit → Push → PR"
echo ""

# Step 1: Verify branch
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Verify Branch"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch: $CURRENT_BRANCH"

if [ "$CURRENT_BRANCH" = "main" ] || [ "$CURRENT_BRANCH" = "dev" ]; then
  echo "❌ ERROR: Cannot ship from main or dev"
  exit 1
fi

# Extract ticket ID from branch if not provided
if [ -z "$TICKET_ID" ]; then
  TICKET_ID=$(echo "$CURRENT_BRANCH" | grep -oE 'SB[0-9]+' | head -1)
fi

if [ -z "$TICKET_ID" ]; then
  echo "⚠️  No ticket ID found in branch or arguments"
  read -p "Enter ticket ID (e.g., SB74): " TICKET_ID
fi

echo "✅ Ticket ID: $TICKET_ID"

# Step 2: Sync with dev
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Sync with dev"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "📡 Fetching latest changes..."
git fetch origin

echo "🔄 Rebasing onto origin/dev..."
if ! git rebase origin/dev; then
  echo "❌ Rebase conflict detected!"
  echo "Please resolve conflicts manually, then run:"
  echo "  git rebase --continue"
  echo "  ./workflow/git-ship.sh $TICKET_ID"
  exit 1
fi

echo "✅ Successfully synced with dev"

# Step 3: Commit (if there are changes)
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Commit Changes"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -z "$(git status --porcelain)" ]; then
  echo "✅ Working tree clean, nothing to commit"
else
  ./workflow/git-commit.sh "$AGENT" "$MODEL"
  if [ $? -ne 0 ]; then
    echo "❌ Commit failed. Not proceeding to push."
    exit 1
  fi
fi

# Step 4: Push
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 4: Push Branch"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "📋 Recent commits:"
git log --oneline -5
echo ""

./workflow/git-push.sh
if [ $? -ne 0 ]; then
  echo "❌ Push failed. Not proceeding to PR."
  exit 1
fi

# Step 5: Open PR
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 5: Create Pull Request"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Get the last commit message for PR title
LAST_COMMIT=$(git log -1 --pretty=%B)

# Extract a clean title (first line)
PR_TITLE="[claude] $TICKET_ID $(echo "$LAST_COMMIT" | head -1 | sed 's/\[claude\] //' | sed "s/$TICKET_ID //")"

echo "📝 PR Title: $PR_TITLE"
echo ""
echo "Opening PR to dev..."

# Create PR using gh CLI
if command -v gh &> /dev/null; then
  gh pr create \
    --base dev \
    --title "$PR_TITLE" \
    --body "## $TICKET_ID Implementation Complete

### Summary
Implementation of ticket $TICKET_ID

### Acceptance Criteria Coverage
- [ ] Review acceptance criteria in Linear ticket

### Manual Test Steps
1. Review the changes
2. Test according to ticket requirements

### Notes
- Check the Linear ticket for full details
"
  echo ""
  echo "✨ PR created successfully!"
else
  echo "⚠️  'gh' CLI not found. Opening GitHub in browser..."
  echo "Please create the PR manually with title:"
  echo "   $PR_TITLE"
  open "https://github.com/PatNoO/stugbygget/compare/dev...$CURRENT_BRANCH"
fi

echo ""
echo "🎉 Ship complete!"
