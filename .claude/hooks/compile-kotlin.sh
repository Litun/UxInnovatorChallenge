#!/bin/bash
# PostToolUse hook: compile Android debug after any .kt file is written/edited
# asyncRewake: exits 2 on failure to wake Claude

FILE=$(jq -r '.tool_input.file_path // empty')

if echo "$FILE" | grep -qE '\.kt$'; then
  cd /Users/litun/AndroidStudioProjects/UxInnovator
  OUTPUT=$(./gradlew :composeApp:compileDebugKotlinAndroid --quiet 2>&1)
  rc=$?
  if [ $rc -ne 0 ]; then
    echo "=== Kotlin compile errors ==="
    echo "$OUTPUT" | grep '^e: ' | awk '!seen[$0]++'
    echo ""
    echo "BUILD FAILED (:composeApp:compileDebugKotlinAndroid)"
    exit 2
  fi
fi

exit 0
