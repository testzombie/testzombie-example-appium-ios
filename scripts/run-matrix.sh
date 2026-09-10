#!/usr/bin/env bash
set -euo pipefail

APP="${1:-./TestZombieHybridIOS.app}"
DEVICE="${2:-${DEVICE_NAME:-iPhone 16}}"

mvn \
  -Dapp.ios="$APP" \
  -Ddevice.name="$DEVICE" \
  -Dtestzombie.healing=false \
  -Dtest=MutationLevelMatrixTest \
  test
