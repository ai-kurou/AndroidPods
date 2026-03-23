#!/bin/bash
# Claude Code PreToolUse hook: scan for secrets before git commit/push
# Reads tool input from stdin (JSON), checks for secret patterns in diffs

set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# Only intercept Bash tool calls
if [ "$TOOL_NAME" != "Bash" ]; then
  exit 0
fi

# Determine diff command based on git operation
DIFF_CMD=""
if echo "$COMMAND" | grep -qE '^git\s+commit\b'; then
  DIFF_CMD="git diff --cached"
elif echo "$COMMAND" | grep -qE '^git\s+push\b'; then
  BRANCH=$(git branch --show-current 2>/dev/null || echo "main")
  if git rev-parse "origin/$BRANCH" >/dev/null 2>&1; then
    DIFF_CMD="git diff origin/$BRANCH..HEAD"
  else
    # No remote tracking branch yet; scan all commits
    DIFF_CMD="git diff --cached"
  fi
else
  exit 0
fi

# Secret detection patterns
PATTERNS=(
  'AIza[0-9A-Za-z_-]{35}'
  'sk-[0-9a-zA-Z]{20,}'
  'ghp_[0-9a-zA-Z]{36}'
  'gho_[0-9a-zA-Z]{36}'
  'github_pat_[0-9a-zA-Z_]{22,}'
  '-----BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----'
  'AKIA[0-9A-Z]{16}'
  'password\s*=\s*["\x27][^"\x27]{4,}["\x27]'
  'secret\s*=\s*["\x27][^"\x27]{4,}["\x27]'
  'token\s*=\s*["\x27][^"\x27]{4,}["\x27]'
)

# Build combined pattern
COMBINED=$(IFS='|'; echo "${PATTERNS[*]}")

# Run diff and scan
DIFF_OUTPUT=$($DIFF_CMD 2>/dev/null || true)

if [ -z "$DIFF_OUTPUT" ]; then
  exit 0
fi

# Only scan added lines (lines starting with +, excluding +++ header)
MATCHES=$(echo "$DIFF_OUTPUT" | grep -E '^\+[^+]' | grep -oEi "$COMBINED" || true)

if [ -n "$MATCHES" ]; then
  REASON="Secret pattern detected in diff. Matched patterns:\n$MATCHES\n\nPlease remove secrets before committing."
  echo "{\"decision\": \"block\", \"reason\": \"$REASON\"}"
  exit 0
fi

exit 0
