#!/usr/bin/env bash
set -euo pipefail

APP="${1:-./TestZombieHybridIOS.app}"
DEVICE="${2:-${DEVICE_NAME:-iPhone 16}}"

: "${TESTZOMBIE_API_KEY:?Bitte TESTZOMBIE_API_KEY setzen}"
: "${TESTZOMBIE_EMAIL:?Bitte TESTZOMBIE_EMAIL setzen}"

mvn \
  -Dapp.ios="$APP" \
  -Ddevice.name="$DEVICE" \
  -Dtestzombie.healing=true \
  -Dtest=BaselineHealingDemoTest \
  test
