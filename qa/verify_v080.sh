#!/usr/bin/env bash
# Compile core + desktop and run all three standalone regression verifiers.
# Usage (from anywhere): /lib/apk/Omatydom/qa/verify_v080.sh
set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT" || exit 1

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=/data/data/com.foxdebug.acode/cache/omatydom-sdk
export ANDROID_SDK_ROOT=/data/data/com.foxdebug.acode/cache/omatydom-sdk

JAVA="$JAVA_HOME/bin/java"
GRADLE="$(command -v gradle || true)"

if [[ ! -x "$JAVA" ]]; then
    echo "FAIL compile: missing Java at $JAVA"
    exit 1
fi
if [[ -z "$GRADLE" ]]; then
    echo "FAIL compile: gradle not found on PATH"
    exit 1
fi

CP="$ROOT/core/build/classes/java/main:$ROOT/desktop/build/classes/java/main"

echo "== Compile core + desktop =="
if ! "$GRADLE" :core:compileJava :desktop:compileJava --console=plain; then
    echo "FAIL compile (VerifyV080 needs Worker 4's TradeRoute API merged)"
    exit 1
fi
echo "PASS compile"

STATUS=0
for VERIFIER in VerifyV060 VerifyV070 VerifyV080; do
    echo "== Run $VERIFIER =="
    if "$JAVA" -cp "$CP" "com.dotee313.omatydom.desktop.$VERIFIER"; then
        echo "PASS $VERIFIER"
    else
        echo "FAIL $VERIFIER"
        STATUS=1
    fi
done

if [[ "$STATUS" -ne 0 ]]; then
    echo "OVERALL FAIL"
    exit 1
fi
echo "OVERALL PASS"
