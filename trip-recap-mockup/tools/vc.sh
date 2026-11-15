#!/usr/bin/env bash
# Pleos Virtual Console(가상 차량) 터미널 조작 스크립트
#   ./tools/vc.sh gear Drive|Park|Reverse|Neutral
#   ./tools/vc.sh speed 60          # km/h
#   ./tools/vc.sh state             # 현재 기어·속도
# 콘솔은 브라우저 세션 heartbeat가 있어야 명령을 받으므로, 명령 전후로 잠깐 heartbeat를 보낸다.
set -euo pipefail

BASE="http://127.0.0.1:8088"
ADB="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
"$ADB" forward tcp:8088 tcp:8088 >/dev/null

heartbeat() {
  for _ in 1 2 3 4; do
    curl -s -X POST "$BASE/browser/session?client=vc-cli" >/dev/null || true
    sleep 1
  done
}

case "${1:-}" in
  gear)
    heartbeat &
    sleep 1.2
    curl -s -X PUT "$BASE/vehicle/gear?gear=${2:?Drive|Park|Reverse|Neutral}" >/dev/null
    wait
    ;;
  speed)
    heartbeat &
    sleep 1.2
    curl -s -X PUT "$BASE/vehicle/controls?speed=${2:?km/h}&accelerator=0&brake=0" >/dev/null
    wait
    ;;
  state) ;;
  *)
    echo "usage: $0 gear <Drive|Park|Reverse|Neutral> | speed <km/h> | state" >&2
    exit 1
    ;;
esac

curl -s "$BASE/vehicle/state" | python3 -c '
import json, sys
s = json.load(sys.stdin)
g = (s["gear"].get("actual") or {}).get("value", "-")
print("gear=%s  speed=%skm/h" % (g, s["controls"]["speed_kph"]))'
