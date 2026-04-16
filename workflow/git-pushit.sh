#!/bin/zsh
#
# git-pushit.sh
# Combined stage + commit + push workflow
#
# Usage: ./workflow/git-pushit.sh <agent> <model> [type]
#
# Examples:
#   ./workflow/git-pushit.sh copilot GitHub-Copilot          # first commit
#   ./workflow/git-pushit.sh copilot GitHub-Copilot feat     # subsequent
#   ./workflow/git-pushit.sh claude Claude-3.5-Sonnet fix
#

set -e

AGENT="${1:-}"
MODEL="${2:-}"
COMMIT_TYPE="${3:-}"

if [ -z "$AGENT" ] || [ -z "$MODEL" ]; then
  echo "❌ Usage: ./workflow/git-pushit.sh <agent> <model> [type]"
  echo "   Example: ./workflow/git-pushit.sh copilot GitHub-Copilot feat"
  echo "   See AGENTS.md for valid agents and models"
  exit 1
fi

echo "🔄 Git Pushit: Stage → Commit → Push"
echo ""

# Step 1: Commit
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Commit"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./workflow/git-commit.sh "$AGENT" "$MODEL" "$COMMIT_TYPE"

if [ $? -ne 0 ]; then
  echo "❌ Commit failed. Not proceeding to push."
  exit 1
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Push"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
./workflow/git-push.sh

if [ $? -ne 0 ]; then
  echo "❌ Push failed."
  exit 1
fi

echo ""
echo "✨ Stage → Commit → Push complete!"
