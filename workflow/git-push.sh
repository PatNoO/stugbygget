#!/bin/zsh
#
# git-push.sh
# Push the current branch to remote following StugBygget conventions
# Usage: ./workflow/git-push.sh [--force-with-lease]
#

set -e

FORCE_FLAG=""
if [ "$1" = "--force-with-lease" ]; then
  FORCE_FLAG="--force-with-lease"
fi

echo "🔍 Step 1: Verify current branch"
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch: $CURRENT_BRANCH"

if [ "$CURRENT_BRANCH" = "main" ] || [ "$CURRENT_BRANCH" = "dev" ]; then
  echo "❌ ERROR: Direct pushes to main/dev are not allowed"
  echo "All work must be on a claude/SB<ID>-... feature branch"
  exit 1
fi

echo ""
echo "✅ Branch is valid for pushing"

echo ""
echo "📋 Step 2: Show recent commits being pushed"
echo ""
git log --oneline -5
echo ""

echo "🔍 Step 3: Check for upstream remote"
UPSTREAM=$(git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null || true)

if [ -z "$UPSTREAM" ]; then
  echo "No upstream set. Setting up..."
  echo ""
  echo "🚀 Step 4a: Push and set upstream (first push)"
  if [ -n "$FORCE_FLAG" ]; then
    git push -u origin "$CURRENT_BRANCH" "$FORCE_FLAG"
  else
    git push -u origin "$CURRENT_BRANCH"
  fi
else
  echo "Upstream already set: $UPSTREAM"
  echo ""
  echo "🚀 Step 4b: Push to upstream"
  if [ -n "$FORCE_FLAG" ]; then
    git push "$FORCE_FLAG"
  else
    git push
  fi
fi

echo ""
echo "✅ Push successful!"
echo ""
echo "💡 Reminder: PRs should target the 'dev' branch, not 'main'"
