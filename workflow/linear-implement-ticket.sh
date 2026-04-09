#!/bin/zsh
#
# linear-implement-ticket.sh
# Implement a Linear ticket end-to-end
# Usage: ./workflow/linear-implement-ticket.sh SB-74
#

set -e

TICKET_ID="${1:-}"

if [ -z "$TICKET_ID" ]; then
  echo "Usage: ./workflow/linear-implement-ticket.sh SB-74"
  exit 1
fi

echo "🎯 Linear Ticket Implementation"
echo "═══════════════════════════════════════════"
echo "Ticket: $TICKET_ID"
echo ""

# Step 1: Create the feature branch
echo "Step 1: Create feature branch"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "📡 Updating dev branch..."
git checkout dev
git pull origin dev

echo ""
read -p "Enter branch description (short slug, e.g. 'add-phase-progress-bar'): " BRANCH_DESC

if [ -z "$BRANCH_DESC" ]; then
  echo "❌ Branch description required"
  exit 1
fi

BRANCH_NAME="claude/$TICKET_ID-$BRANCH_DESC"
echo "📌 Creating branch: $BRANCH_NAME"

git checkout -b "$BRANCH_NAME"

CURRENT_BRANCH=$(git branch --show-current)
echo "✅ Active branch: $CURRENT_BRANCH"

# Step 2: Implementation guide
echo ""
echo "Step 2: Implementation Guide"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Architecture Layers (Clean Architecture):"
echo ""
echo "  UI Layer"
echo "    └─ feature/<module>/ui/<Name>Screen.kt"
echo "    └─ feature/<module>/ui/<Name>UiState.kt"
echo ""
echo "  ViewModel Layer"
echo "    └─ feature/<module>/ui/<Name>ViewModel.kt"
echo "    └─ feature/<module>/ui/<Name>ViewModelFactory.kt"
echo ""
echo "  Domain Layer"
echo "    └─ domain/usecase/<Name>UseCase.kt"
echo "    └─ domain/model/<Name>.kt"
echo ""
echo "  Data Layer"
echo "    └─ domain/repository/<Name>Repository.kt"
echo "    └─ data/firebase/firestore/<Name>FirestoreDataSource.kt"
echo ""
echo "  DI Layer"
echo "    └─ di/<Module>Module.kt"
echo ""

echo "📋 Strict Rules:"
echo "  ✓ No Firebase/API imports in Composables"
echo "  ✓ No business logic in Composables"
echo "  ✓ ViewModel communicates through Repository"
echo "  ✓ Every screen needs UiState (loading, error, content)"
echo "  ✓ UI-only features: use // TODO: wire to backend comments"
echo ""

read -p "Press Enter when ready to start implementation: "

echo ""
echo "💡 Next Steps:"
echo "  1. Implement the feature following the layers above"
echo "  2. Follow CLAUDE.md and ARCHITECTURE_GUARD.md"
echo "  3. Add KDoc for non-trivial business logic"
echo "  4. Test thoroughly"
echo "  5. Run: ./workflow/git-ship.sh $TICKET_ID"
echo ""

echo "✨ Branch ready for implementation!"
echo ""
echo "Reminders:"
echo "  • VS Code: Open and start coding"
echo "  • Commit: ./workflow/git-commit.sh [type]"
echo "  • Push & PR: ./workflow/git-ship.sh $TICKET_ID"
echo ""

# Optional: Open project in VS Code
read -p "Open VS Code in this project? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
  code .
fi
