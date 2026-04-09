#!/bin/zsh
#
# linear-create-ticket.sh
# Create a Linear ticket end-to-end
# Usage: ./workflow/linear-create-ticket.sh "Short description of ticket"
#

set -e

TICKET_DESC="${1:-}"

if [ -z "$TICKET_DESC" ]; then
  echo "Usage: ./workflow/linear-create-ticket.sh \"Ticket description\""
  echo ""
  echo "Example:"
  echo "  ./workflow/linear-create-ticket.sh \"Implement planning timeline progress screen\""
  exit 1
fi

echo "📋 Linear Ticket Creator"
echo "═══════════════════════════════════════════"
echo ""

# Step 1: Classify the ticket
echo "Step 1: Classify the ticket type"
echo ""
echo "Types:"
echo "  1) feature - New user-facing functionality"
echo "  2) bug     - Something broken or incorrect"
echo "  3) chore   - Technical work, no direct user change"
echo "  4) spike   - Time-boxed investigation"
echo ""
read -p "Select type (1-4): " TYPE_NUM

case "$TYPE_NUM" in
  1) TICKET_TYPE="feature" ;;
  2) TICKET_TYPE="bug" ;;
  3) TICKET_TYPE="chore" ;;
  4) TICKET_TYPE="spike" ;;
  *)
    echo "❌ Invalid selection"
    exit 1
    ;;
esac

echo "✅ Type: $TICKET_TYPE"

# Step 2: Determine next SB number
echo ""
echo "Step 2: Fetching Linear tickets to determine next SB number..."

# Note: This requires 'linear' CLI to be installed
# For now, we'll prompt the user
read -p "Enter next SB number (check Linear for highest current number): " SB_NUMBER

if ! [[ "$SB_NUMBER" =~ ^[0-9]+$ ]]; then
  echo "❌ Invalid SB number"
  exit 1
fi

TICKET_PREFIX="SB$SB_NUMBER"
echo "✅ Ticket ID: $TICKET_PREFIX"

# Step 3: Collect layer and milestone
echo ""
echo "Step 3: Specify layer and milestone"
echo ""
echo "Layer:"
echo "  1) frontend"
echo "  2) backend"
echo "  3) fullstack"
echo "  4) infra"
echo ""
read -p "Select layer (1-4): " LAYER_NUM

case "$LAYER_NUM" in
  1) LAYER="frontend" ;;
  2) LAYER="backend" ;;
  3) LAYER="fullstack" ;;
  4) LAYER="infra" ;;
  *)
    echo "❌ Invalid selection"
    exit 1
    ;;
esac

echo ""
echo "Milestone:"
echo "  1) MVP"
echo "  2) Post-MVP"
echo "  3) Beta"
echo ""
read -p "Select milestone (1-3, default 1): " MILESTONE_NUM
MILESTONE_NUM=${MILESTONE_NUM:-1}

case "$MILESTONE_NUM" in
  1) MILESTONE="MVP" ;;
  2) MILESTONE="Post-MVP" ;;
  3) MILESTONE="Beta" ;;
  *)
    echo "❌ Invalid selection"
    exit 1
    ;;
esac

# Step 4: Draft ticket fields
echo ""
echo "Step 4: Draft ticket details"
echo ""

# Title
if [ "$TICKET_TYPE" = "spike" ]; then
  TITLE="Spike: $TICKET_DESC"
else
  TITLE="$TICKET_PREFIX $TICKET_DESC"
fi

echo "📝 Title: $TITLE"

# Description template based on type
echo ""
echo "📄 Enter description (or leave empty for template):"

if [ "$TICKET_TYPE" = "feature" ]; then
  read -p "Context/Why (optional): " CONTEXT
  read -p "AC1 (optional): " AC1
  read -p "AC2 (optional): " AC2
  read -p "AC3 (optional): " AC3
  
  DESCRIPTION="### Context
${CONTEXT:-Why does this feature exist? What user problem does it solve?}

### Acceptance Criteria
- [ ] AC1: ${AC1:-Specific, testable outcome}
- [ ] AC2: ${AC2:-Specific, testable outcome}
- [ ] AC3: ${AC3:-Specific, testable outcome}

### Implementation Notes
- [ ] Update Firestore schema if needed (document in docs/firestore_schema.md)
- [ ] Add KDoc for business logic
- [ ] Follow Clean Architecture layers
- [ ] Support loading, error, and empty states"

elif [ "$TICKET_TYPE" = "bug" ]; then
  read -p "Steps to reproduce: " STEPS
  read -p "Expected behavior: " EXPECTED
  read -p "Actual behavior: " ACTUAL
  
  DESCRIPTION="### Steps to Reproduce
${STEPS:-1. Step 1
2. Step 2
3. Step 3}

### Expected Behavior
${EXPECTED:-What should happen}

### Actual Behavior
${ACTUAL:-What actually happens}

### Additional Info
- Device/OS: 
- Frequency: Always/Sometimes/Rare"

elif [ "$TICKET_TYPE" = "spike" ]; then
  read -p "Investigation goal: " GOAL
  read -p "Key questions: " QUESTIONS
  
  DESCRIPTION="### Goal
${GOAL:-What are we trying to determine?}

### Key Questions
${QUESTIONS:-What specific questions need answers?}

### Success Criteria
- [ ] Question 1 answered
- [ ] Question 2 answered
- [ ] Recommendation documented

### Suggested Approach
- Research
- Proof of concept
- Performance testing"

else  # chore
  read -p "What needs to be done: " WHAT
  
  DESCRIPTION="### What
${WHAT:-Technical task description}

### Why
- Improves maintainability
- Reduces technical debt
- Enables future work

### Success Criteria
- [ ] Task completed
- [ ] Tests updated (if applicable)
- [ ] Documentation updated (if applicable)"
fi

# Step 5: Summary and confirmation
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Summary"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Type:      $TICKET_TYPE"
echo "ID:        $TICKET_PREFIX"
echo "Title:     $TITLE"
echo "Layer:     $LAYER"
echo "Milestone: $MILESTONE"
echo ""
echo "Description preview:"
echo "$DESCRIPTION" | head -10
echo ""

read -p "Create ticket? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
  echo "📋 Creating ticket in Linear..."
  echo ""
  echo "To create this ticket, use Linear's web interface:"
  echo "  1. Go to https://linear.app/stugbygget"
  echo "  2. Click 'New issue'"
  echo "  3. Fill in:"
  echo "     Title: $TITLE"
  echo "     Type: $TICKET_TYPE"
  echo "     Team: StugBygget"
  echo "     Priority: (your choice)"
  echo "     Description: (paste below)"
  echo ""
  echo "────────────────────────────────────────"
  echo "$DESCRIPTION"
  echo "────────────────────────────────────────"
  echo ""
  echo "💡 Note: CLI automation coming soon when linear-cli is fully configured"
else
  echo "❌ Ticket creation cancelled"
  exit 1
fi
