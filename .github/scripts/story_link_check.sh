#!/bin/bash
set -e

PR_BODY="$1"

JIRA_KEY_REGEX='[A-Z][A-Z0-9]+-[0-9]+'

echo "🔍 Verifying Jira reference..."

# 1. Check PR body
if [[ "$PR_BODY" =~ $JIRA_KEY_REGEX ]]; then
  echo "✅ Jira key found in PR description"
  exit 0
fi

# 2. Check PR title
if [[ "$GITHUB_PR_TITLE" =~ $JIRA_KEY_REGEX ]]; then
  echo "✅ Jira key found in PR title"
  exit 0
fi

# 3. Check branch name
if [[ "$GITHUB_HEAD_REF" =~ $JIRA_KEY_REGEX ]]; then
  echo "✅ Jira key found in branch name"
  exit 0
fi

echo "❌ No Jira issue key found."
echo "Expected something like ENG-1234 in branch name, PR title, PR description."
exit 1
