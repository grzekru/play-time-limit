#!/usr/bin/env bash
set -euo pipefail

exec /usr/bin/java \
  -ea \
  -Drunelite.userhome="$HOME/.local/share/bolt-launcher/.runelite" \
  -jar "/home/gk/Software/rune/dist/play-time-limit-all.jar" \
  --developer-mode \
  --debug \
  "$@"
