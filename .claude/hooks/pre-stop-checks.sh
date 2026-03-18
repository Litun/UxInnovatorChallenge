#!/bin/bash
# Stop hook: run tests + compile iOS before Claude finishes a task
# Outputs JSON to block stopping if checks fail

cd /Users/litun/AndroidStudioProjects/UxInnovator

ERRORS=""

# Run Android unit tests (covers commonTest + androidTest, single variant)
echo "Running tests..." >&2
TEST_OUTPUT=$(./gradlew :composeApp:testDebugUnitTest --quiet 2>&1)
if [ $? -ne 0 ]; then
  ERRORS="Tests failed."
  COMPILER_ERRORS=$(echo "$TEST_OUTPUT" | grep '^e: ' | awk '!seen[$0]++')
  if [ -n "$COMPILER_ERRORS" ]; then
    echo "=== Test / compile errors ===" >&2
    echo "$COMPILER_ERRORS" >&2
  else
    echo "$TEST_OUTPUT" | tail -15 >&2
  fi
fi

# Compile iOS simulator target
echo "Compiling iOS..." >&2
IOS_OUTPUT=$(./gradlew :composeApp:compileKotlinIosSimulatorArm64 --quiet 2>&1)
if [ $? -ne 0 ]; then
  ERRORS="${ERRORS} iOS compilation failed."
  IOS_ERRORS=$(echo "$IOS_OUTPUT" | grep '^e: ' | awk '!seen[$0]++')
  if [ -n "$IOS_ERRORS" ]; then
    echo "=== iOS compile errors ===" >&2
    echo "$IOS_ERRORS" >&2
  else
    echo "$IOS_OUTPUT" | tail -10 >&2
  fi
fi

if [ -n "$ERRORS" ]; then
  echo "{\"decision\": \"block\", \"reason\": \"Pre-stop checks failed: ${ERRORS} Fix issues before finishing.\"}"
  exit 2
fi

echo "{\"systemMessage\": \"All checks passed: tests green, iOS compiled successfully.\"}"
exit 0
