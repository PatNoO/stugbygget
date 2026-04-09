#!/bin/zsh
#
# git-commit.sh
# Commit message formatting for StugBygget
#
# Usage:
#   ./workflow/git-commit.sh <agent> <model> [type]
#
# Arguments:
#   agent  — Who implemented this: claude | copilot | grok (required)
#   model  — Which model: Claude-3.5-Sonnet | GitHub-Copilot | Grok-3 (required)
#   type   — Commit type, required for subsequent commits on a branch:
#            feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert
#
# Examples:
#   ./workflow/git-commit.sh copilot GitHub-Copilot          # first commit
#   ./workflow/git-commit.sh copilot GitHub-Copilot feat     # subsequent
#   ./workflow/git-commit.sh claude Claude-3.5-Sonnet fix
#

set -e

AGENT="${1:-}"
MODEL="${2:-}"
COMMIT_TYPE="${3:-}"

# Validate required args
if [ -z "$AGENT" ] || [ -z "$MODEL" ]; then
  echo "❌ Usage: ./workflow/git-commit.sh <agent> <model> [type]"
  echo "   Example: ./workflow/git-commit.sh copilot GitHub-Copilot feat"
  echo "   See AGENTS.md for valid agents and models"
  exit 1
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)

# Extract ticket ID from branch name (e.g., SB74 from claude/SB74-add-feature)
TICKET_ID=$(echo "$BRANCH" | grep -oE 'SB[0-9]+' | head -1)

# Show current changes
echo "📊 Current changes:"
git status
echo ""
git diff --stat
echo ""

# Determine if this is the first commit on the branch
COMMITS_ON_BRANCH=$(git rev-list --count @{u}..HEAD 2>/dev/null || echo "0")

if [ "$COMMITS_ON_BRANCH" -eq 0 ]; then
  # First commit on branch — use ticket mirror format
  if [ -z "$TICKET_ID" ]; then
    echo "⚠️  No ticket ID found in branch name"
    echo "Please rename your branch to follow: claude/SB<ID>-short-description"
    exit 1
  fi

  echo "📝 First commit on branch"
  echo "Format: [$AGENT]($MODEL) $TICKET_ID <Title Case Description>"
  echo ""
  read -p "Enter commit message (Title Case): " MESSAGE

  if [ -z "$MESSAGE" ]; then
    echo "❌ Commit message cannot be empty"
    exit 1
  fi

  FULL_MESSAGE="[$AGENT]($MODEL) $TICKET_ID $MESSAGE"

else
  # Subsequent commits — type is required
  if [ -z "$COMMIT_TYPE" ]; then
    echo "❌ Subsequent commits require a type as the 3rd argument"
    echo "   Usage: ./workflow/git-commit.sh $AGENT $MODEL <type>"
    echo "   Types: feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert"
    exit 1
  fi

  # Validate type
  case "$COMMIT_TYPE" in
    feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert) ;;
    *)
      echo "❌ Invalid type: $COMMIT_TYPE"
      echo "   Valid types: feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert"
      exit 1
      ;;
  esac

  echo "📝 Subsequent commit"
  echo "Format: [$AGENT]($MODEL) [$COMMIT_TYPE] <imperative description>"
  echo ""
  read -p "Enter commit message (imperative): " MESSAGE

  if [ -z "$MESSAGE" ]; then
    echo "❌ Commit message cannot be empty"
    exit 1
  fi

  FULL_MESSAGE="[$AGENT]($MODEL) [$COMMIT_TYPE] $MESSAGE"
fi

echo ""
echo "📌 Stage files"
echo "Which files to stage? Leave empty to stage all changed tracked files."
git status --short
echo ""
read -p "File paths (comma-separated) or Enter for all: " FILES_INPUT

if [ -z "$FILES_INPUT" ]; then
  echo "🔄 Staging all changed files..."
  git add -A
else
  IFS=',' read -ra FILES <<< "$FILES_INPUT"
  for file in "${FILES[@]}"; do
    file=$(echo "$file" | xargs)
    if [ -n "$file" ]; then
      echo "📎 Staging: $file"
      git add "$file"
    fi
  done
fi

echo ""
echo "✅ Commit message:"
echo "   $FULL_MESSAGE"
echo ""
read -p "Proceed with commit? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
  git commit -m "$FULL_MESSAGE"
  echo "✨ Commit successful!"
else
  echo "❌ Commit cancelled"
  git reset
  exit 1
fi


# Extract ticket ID from branch name (e.g., SB74 from claude/SB74-add-feature)
TICKET_ID=$(echo "$BRANCH" | grep -oE 'SB[0-9]+' | head -1)

# Show current changes
echo "📊 Current changes:"
git status
echo ""
git diff --stat
echo ""

# Determine if this is the first commit on the branch
COMMITS_ON_BRANCH=$(git rev-list --count @{u}..HEAD 2>/dev/null || echo "0")

if [ "$COMMITS_ON_BRANCH" -eq 0 ]; then
  # First commit on branch - use ticket mirror format
  if [ -z "$TICKET_ID" ]; then
    echo "⚠️  No ticket ID found in branch name"
    echo "Please rename your branch to follow: claude/SB<ID>-short-description"
    exit 1
  fi
  
  # If agent not provided, prompt for it
  if [ -z "$AGENT" ]; then
    echo "🤖 Which agent implemented this? (see AGENTS.md for full list)"
    echo "  1) claude"
    echo "  2) copilot"
    echo "  3) grok"
    read -p "Select agent (1-3): " AGENT_NUM
    case "$AGENT_NUM" in
      1) AGENT="claude" ;;
      2) AGENT="copilot" ;;
      3) AGENT="grok" ;;
      *)
        echo "❌ Invalid selection"
        exit 1
        ;;
    esac
  fi
  
  # If model not provided, prompt for it
  if [ -z "$MODEL" ]; then
    read -p "Enter model name (e.g., Claude-3.5-Sonnet, GitHub-Copilot — see AGENTS.md): " MODEL
    if [ -z "$MODEL" ]; then
      echo "❌ Model name required"
      exit 1
    fi
  fi
  
  echo "📝 First commit on branch detected"
  echo "Format: [$AGENT]($MODEL) $TICKET_ID <Title Case Description>"
  echo ""
  read -p "Enter commit message (Title Case): " MESSAGE
  
  if [ -z "$MESSAGE" ]; then
    echo "❌ Commit message cannot be empty"
    exit 1
  fi
  
  FULL_MESSAGE="[$AGENT]($MODEL) $TICKET_ID $MESSAGE"
else
  # Subsequent commits - use type prefix format
  if [ -z "$COMMIT_TYPE" ]; then
    echo "⚠️  Subsequent commit requires a type"
    echo "Usage: ./workflow/git-commit.sh [type] [agent] [model]"
    echo "Types: feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert"
    exit 1
  fi
  
  # Validate type
  case "$COMMIT_TYPE" in
    feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert) ;;
    *)
      echo "❌ Invalid type: $COMMIT_TYPE"
      echo "Valid types: feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert"
      exit 1
      ;;
  esac
  
  # If agent not provided, prompt for it
  if [ -z "$AGENT" ]; then
    echo "🤖 Which agent implemented this? (see AGENTS.md for full list)"
    echo "  1) claude"
    echo "  2) copilot"
    echo "  3) grok"
    read -p "Select agent (1-3): " AGENT_NUM
    case "$AGENT_NUM" in
      1) AGENT="claude" ;;
      2) AGENT="copilot" ;;
      3) AGENT="grok" ;;
      *)
        echo "❌ Invalid selection"
        exit 1
        ;;
    esac
  fi
  
  # If model not provided, prompt for it
  if [ -z "$MODEL" ]; then
    read -p "Enter model name (e.g., Claude-3.5-Sonnet, GitHub-Copilot — see AGENTS.md): " MODEL
    if [ -z "$MODEL" ]; then
      echo "❌ Model name required"
      exit 1
    fi
  fi
  
  echo "📝 Subsequent commit detected"
  echo "Format: [$AGENT]($MODEL) [$COMMIT_TYPE] <imperative description>"
  echo ""
  read -p "Enter commit message (imperative): " MESSAGE
  
  if [ -z "$MESSAGE" ]; then
    echo "❌ Commit message cannot be empty"
    exit 1
  fi
  
  FULL_MESSAGE="[$AGENT]($MODEL) [$COMMIT_TYPE] $MESSAGE"
fi

echo ""
echo "📌 Staging specific files (not git add .)"
echo "Which files should be staged? (leave empty to auto-stage all changed)"
git status --short

echo ""
read -p "Enter file paths or leave empty for all (comma-separated or just Enter): " FILES_INPUT

if [ -z "$FILES_INPUT" ]; then
  echo "🔄 Staging all changed files..."
  git add -A
else
  # Parse comma-separated files and stage them
  IFS=',' read -ra FILES <<< "$FILES_INPUT"
  for file in "${FILES[@]}"; do
    file=$(echo "$file" | xargs)  # trim whitespace
    if [ -n "$file" ]; then
      echo "📎 Staging: $file"
      git add "$file"
    fi
  done
fi

echo ""
echo "✅ Commit message:"
echo "   $FULL_MESSAGE"
echo ""
read -p "Proceed with commit? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
  git commit -m "$FULL_MESSAGE"
  echo "✨ Commit successful!"
else
  echo "❌ Commit cancelled"
  git reset
  exit 1
fi
