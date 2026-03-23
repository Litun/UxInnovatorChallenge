#!/bin/bash
# Stop hook: build Android, build iOS, run unit tests before Claude finishes
# Outputs JSON to block stopping if any check fails

cd /Users/litun/AndroidStudioProjects/UxInnovator

ERRORS=""

# 1. Build Android
echo "Building Android..." >&2
ANDROID_OUTPUT=$(./gradlew :composeApp:compileDebugKotlinAndroid --quiet 2>&1)
if [ $? -ne 0 ]; then
  ERRORS="Android build failed."
  ANDROID_ERRORS=$(echo "$ANDROID_OUTPUT" | grep '^e: ' | awk '!seen[$0]++')
  if [ -n "$ANDROID_ERRORS" ]; then
    echo "=== Android build errors ===" >&2
    echo "$ANDROID_ERRORS" >&2
  else
    echo "$ANDROID_OUTPUT" | tail -15 >&2
  fi
fi

# 2. Build iOS (compile + link framework)
echo "Building iOS..." >&2
IOS_OUTPUT=$(./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 --quiet 2>&1)
if [ $? -ne 0 ]; then
  ERRORS="${ERRORS} iOS build failed."
  IOS_ERRORS=$(echo "$IOS_OUTPUT" | grep '^e: ' | awk '!seen[$0]++')
  if [ -n "$IOS_ERRORS" ]; then
    echo "=== iOS build errors ===" >&2
    echo "$IOS_ERRORS" >&2
  else
    echo "$IOS_OUTPUT" | tail -15 >&2
  fi
fi

# 3. Run unit tests (commonTest + androidUnitTest, no integration tests)
echo "Running unit tests..." >&2
TEST_OUTPUT=$(./gradlew :composeApp:testDebugUnitTest --quiet 2>&1)
if [ $? -ne 0 ]; then
  ERRORS="${ERRORS} Tests failed."
  COMPILE_ERRORS=$(echo "$TEST_OUTPUT" | grep '^e: ' | awk '!seen[$0]++')
  if [ -n "$COMPILE_ERRORS" ]; then
    echo "=== Test compile errors ===" >&2
    echo "$COMPILE_ERRORS" >&2
  else
    echo "$TEST_OUTPUT" | tail -15 >&2
  fi
fi

if [ -n "$ERRORS" ]; then
  echo "{\"decision\": \"block\", \"reason\": \"Pre-stop checks failed: ${ERRORS} Fix issues before finishing.\"}"
  exit 2
fi

echo "{\"systemMessage\": \"All checks passed: Android built, iOS built, tests green.\"}"
exit 0
