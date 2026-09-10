#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROJECT="$ROOT/ios-app/TestZombieHybridIOS.xcodeproj"
DERIVED="$ROOT/target/DerivedData"
OUTPUT="$ROOT/TestZombieHybridIOS.app"
DEVICE="${1:-${DEVICE_NAME:-iPhone 16}}"

rm -rf "$DERIVED" "$OUTPUT"
mkdir -p "$ROOT/target"

xcodebuild \
  -project "$PROJECT" \
  -scheme TestZombieHybridIOS \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination "platform=iOS Simulator,name=$DEVICE" \
  -derivedDataPath "$DERIVED" \
  CODE_SIGNING_ALLOWED=NO \
  build

BUILT_APP="$DERIVED/Build/Products/Debug-iphonesimulator/TestZombieHybridIOS.app"
if [[ ! -d "$BUILT_APP" ]]; then
  echo "Gebautes .app-Bundle nicht gefunden: $BUILT_APP" >&2
  exit 1
fi

cp -R "$BUILT_APP" "$OUTPUT"
echo "Fertig: $OUTPUT"
