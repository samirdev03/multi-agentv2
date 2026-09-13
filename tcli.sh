#!/usr/bin/env bash
set -euo pipefail

# Telegram-Connector-CLI-Wrapper (Linux-Server, im Repo-Ordner)
#
#   ./tcli.sh add-channel --channelId 6440172519 --name "Mein Bot" --botToken TOKEN
#   ./tcli.sh list-channels
#
# Ohne Argumente wird list-channels ausgefuehrt.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_DIR="${TELECONN_COMPOSE_DIR:-/root/platform}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker nicht gefunden." >&2
  exit 1
fi

if [[ ! -f "$COMPOSE_DIR/docker-compose.yml" ]]; then
  echo "docker-compose.yml nicht in $COMPOSE_DIR gefunden." >&2
  exit 1
fi

[[ $# -eq 0 ]] && set -- list-channels

cd "$COMPOSE_DIR"
exec docker compose run --rm -T telegram-connector "$@"