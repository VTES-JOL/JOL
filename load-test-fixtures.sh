#!/usr/bin/env bash
# load-test-fixtures.sh — Reset the local Postgres DB to known fixture data
# (Player1-5, sample games/decks/tournaments) from src/test/resources/data.
#
# Requires local-docker-compose.yml's `db` service to be running:
#   docker compose -f local-docker-compose.yml up -d db
#
# Usage:
#   ./load-test-fixtures.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

exec "$SCRIPT_DIR/migrate-to-db.sh" "$SCRIPT_DIR/src/test/resources/data"
