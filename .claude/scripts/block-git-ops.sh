#!/bin/bash
# Claude Code PreToolUse hook: block git commit/push and gh pr create
# unless explicitly instructed by the user.

set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

if [ "$TOOL_NAME" != "Bash" ]; then
  exit 0
fi

if echo "$COMMAND" | grep -qE '(^|&&|\|[|]|;)\s*git\s+commit\b'; then
  echo '{"decision": "block", "reason": "git commit はユーザーが明示的に指示した場合のみ実行できます。"}'
  exit 0
fi

if echo "$COMMAND" | grep -qE '(^|&&|\|[|]|;)\s*git\s+push\b'; then
  echo '{"decision": "block", "reason": "git push はユーザーが明示的に指示した場合のみ実行できます。"}'
  exit 0
fi

if echo "$COMMAND" | grep -qE '(^|&&|\|[|]|;)\s*gh\s+pr\s+create\b'; then
  echo '{"decision": "block", "reason": "gh pr create はユーザーが明示的に指示した場合のみ実行できます。"}'
  exit 0
fi

exit 0
