#!/usr/bin/env bash
# migrate-to-db.sh — Reinitialise PostgreSQL from JOL file-based data
#
# Usage:
#   ./migrate-to-db.sh [--player-data-only] [DATA_DIR]
#
# --player-data-only restricts the import to tables owned by a single player —
# player/player_role, player_activity, deck_info/deck_format/deck_content, and
# refresh_token. Everything else (games, registrations, tournaments, global
# chat, past-game history) is skipped entirely. Useful for a first test
# migration against a real data directory without dragging along live/in-
# progress game state.
#
# DATA_DIR defaults to ~/data. Override DB connection via env vars:
#   PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD
#
# Prerequisites: psql, jq, python3

set -euo pipefail

PLAYER_DATA_ONLY=false
for arg in "$@"; do
  case "$arg" in
    --player-data-only) PLAYER_DATA_ONLY=true ;;
    *) DATA="$arg" ;;
  esac
done
DATA="${DATA:-$HOME/data}"
export PGHOST="${PGHOST:-localhost}"
export PGPORT="${PGPORT:-5432}"
export PGDATABASE="${PGDATABASE:-jol}"
export PGUSER="${PGUSER:-jol}"
export PGPASSWORD="${PGPASSWORD:-jol}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATIONS="$SCRIPT_DIR/src/main/resources/db/migration"

# ── Helpers ──────────────────────────────────────────────────────────────────

log()     { echo; echo "▸ $*"; }
success() { echo "  ✓ $1"; }

# Run psql against a specific database (first arg), remaining args passed through
db() {
  local target="$1"; shift
  PGDATABASE="$target" psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 "$@"
}

# Pipe CSV from stdin into a table
copy_into() {
  local table="$1" cols="$2"
  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 \
    -c "\copy $table($cols) FROM STDIN WITH (FORMAT csv, NULL '')"
}

# Compute Flyway-compatible CRC32 (signed int32).
# Flyway reads the file line-by-line with readLine() (which strips the line terminator)
# and updates CRC32 with each line's UTF-8 bytes — no newlines included.
flyway_crc32() {
  python3 - "$1" <<'PYEOF'
import zlib, sys
crc = 0
with open(sys.argv[1], 'r', encoding='utf-8') as f:
    for line in f:
        crc = zlib.crc32(line.rstrip('\r\n').encode('utf-8'), crc) & 0xffffffff
print(crc if crc <= 0x7fffffff else crc - 0x100000000)
PYEOF
}

# ── Preflight checks ─────────────────────────────────────────────────────────

for cmd in psql jq python3; do
  command -v "$cmd" >/dev/null || { echo "ERROR: $cmd not found"; exit 1; }
done

[[ -d "$DATA" ]] || { echo "ERROR: DATA_DIR '$DATA' does not exist"; exit 1; }

REQUIRED_FILES=(players.json decks.json player-timestamps.json)
if [[ "$PLAYER_DATA_ONLY" == false ]]; then
  REQUIRED_FILES+=(games.json registrations.json tournaments.json game-timestamps.json chats.json)
fi
for f in "${REQUIRED_FILES[@]}"; do
  [[ -f "$DATA/$f" ]] || { echo "ERROR: $DATA/$f not found"; exit 1; }
done

echo "Migrating data from: $DATA"
echo "Target database:     $PGUSER@$PGHOST:$PGPORT/$PGDATABASE"
[[ "$PLAYER_DATA_ONLY" == true ]] && echo "Mode:                 --player-data-only (games/registrations/tournaments/global chat/game history skipped)"
echo

# ── Temp files ────────────────────────────────────────────────────────────────
DEDUPED_DECKS_FILE=$(mktemp)
REG_CSV=$(mktemp)
ACTIVITY_CSV=$(mktemp)
PLAYER_ACTIVITY_CSV=$(mktemp)
TOURNAMENT_REG_CSV=$(mktemp)
GAMES_CSV=$(mktemp)
DECK_INFO_CSV=$(mktemp)
GLOBAL_CHAT_CSV=$(mktemp)
SUBSCRIPTION_CSV=$(mktemp)
SITE_NOTES_CSV=$(mktemp)
trap 'rm -f "$DEDUPED_DECKS_FILE" "$REG_CSV" "$ACTIVITY_CSV" "$PLAYER_ACTIVITY_CSV" "$TOURNAMENT_REG_CSV" "$GAMES_CSV" "$DECK_INFO_CSV" "$GLOBAL_CHAT_CSV" "$SUBSCRIPTION_CSV" "$SITE_NOTES_CSV"' EXIT

# ── 1. Reset database ────────────────────────────────────────────────────────
log "Resetting database..."
db postgres -c "DROP DATABASE IF EXISTS \"$PGDATABASE\";"
db postgres -c "CREATE DATABASE \"$PGDATABASE\" WITH OWNER \"$PGUSER\";"
success "Database recreated"

# ── 2. Apply Flyway migrations ───────────────────────────────────────────────
log "Applying migrations..."

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<'SQL'
CREATE TABLE flyway_schema_history (
    installed_rank INTEGER       NOT NULL,
    version        VARCHAR(50),
    description    VARCHAR(200)  NOT NULL,
    type           VARCHAR(20)   NOT NULL,
    script         VARCHAR(1000) NOT NULL,
    checksum       INTEGER,
    installed_by   VARCHAR(100)  NOT NULL,
    installed_on   TIMESTAMP     NOT NULL DEFAULT now(),
    execution_time INTEGER       NOT NULL,
    success        BOOLEAN       NOT NULL,
    CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
);
SQL

rank=1
while IFS= read -r migration_file; do
  script_name=$(basename "$migration_file")
  version="${script_name%%__*}"; version="${version#V}"
  description="${script_name#*__}"; description="${description%.sql}"; description="${description//_/ }"
  checksum=$(flyway_crc32 "$migration_file")

  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 -f "$migration_file"
  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 -c \
    "INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success)
     VALUES ($rank,'$version','$description','SQL','$script_name',$checksum,current_user,0,true);"

  echo "  ✓ V$version: $description"
  rank=$((rank + 1))
done < <(printf '%s\n' "$MIGRATIONS"/V*.sql | sort -V)

# ── 3. Players ───────────────────────────────────────────────────────────────
log "Loading players..."
jq -r 'to_entries[] | [
  .value.name,
  .value.id,
  (.value.email // ""),
  (.value.hash // ""),
  (.value.discordId // ""),
  (.value.veknId // ""),
  (.value.countryCode // ""),
  (if .value.showImages then "true" else "false" end),
  (.value.edgeColor // "#FFFFFF")
] | @csv' "$DATA/players.json" \
| copy_into player "player_name,player_id,email,password_hash,discord_id,vekn_id,country_code,show_images,edge_color"

# player_id is available directly in players.json — emit it without a staging table
jq -r 'to_entries[] | .value.id as $id | .value.roles[] | [$id, .] | @csv' "$DATA/players.json" \
| copy_into player_role "player_id,role"

PLAYER_COUNT=$(jq 'length' "$DATA/players.json")
success "$PLAYER_COUNT players"

# ── 4. Player activity ───────────────────────────────────────────────────────
log "Loading player activity..."
# player-timestamps.json is keyed by player_name; join to player to get player_id.
jq -r 'to_entries[] | [.key, .value] | @csv' "$DATA/player-timestamps.json" > "$PLAYER_ACTIVITY_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE player_activity_staging (
  player_name VARCHAR(255),
  last_seen   TIMESTAMP WITH TIME ZONE
);
\copy player_activity_staging(player_name,last_seen) FROM '$PLAYER_ACTIVITY_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO player_activity (player_id, last_seen)
SELECT p.player_id, s.last_seen
FROM player_activity_staging s
JOIN player p USING (player_name);
SQL

success "$(jq 'length' "$DATA/player-timestamps.json") player timestamps"

# ── 5. Tournaments ───────────────────────────────────────────────────────────
if [[ "$PLAYER_DATA_ONLY" == true ]]; then
  echo
  echo "  ⚠ --player-data-only: skipping tournaments"
else
log "Loading tournaments..."
jq -r '.[] | [
  .id,
  .name,
  .registrationStart,
  .registrationEnd,
  .playStarts,
  .playEnds,
  .format,
  ((.deckFormat // "") | ascii_upcase),
  (.numberOfRounds // 0),
  (if .finalEnabled then "true" else "false" end),
  (if .requiresId then "true" else "false" end),
  (.status // "EDIT"),
  ((.rules // []) | tojson),
  ((.specialRules // {}) | tojson),
  ((.rounds // {}) | tojson),
  ((.finals // {}) | tojson)
] | @csv' "$DATA/tournaments.json" \
| copy_into tournament \
  "tournament_id,name,registration_start,registration_end,play_starts,play_ends,format,deck_format,number_of_rounds,final_enabled,requires_id,status,rules,special_rules,rounds,finals"

# tournament registrations reference player_name; stage and join to resolve player_id.
# vekn is sometimes stored as "#1610008" or a full player-registry URL instead of a bare
# ID — extract the trailing digit run so only the numeric VEKN ID is imported.
jq -r '.[] | .id as $tid | .registrations[] | [
  $tid,
  .player,
  ((.vekn // "") | [scan("[0-9]+")] | last // ""),
  (.deck // "")
] | @csv' "$DATA/tournaments.json" > "$TOURNAMENT_REG_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE tournament_reg_staging (
  tournament_id VARCHAR(36),
  player_name   VARCHAR(255),
  vekn          VARCHAR(50),
  deck_id       VARCHAR(36)
);
\copy tournament_reg_staging(tournament_id,player_name,vekn,deck_id) FROM '$TOURNAMENT_REG_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO tournament_registration (tournament_id, player_id, vekn, deck_id)
SELECT s.tournament_id, p.player_id, NULLIF(s.vekn,''), NULLIF(s.deck_id,'')
FROM tournament_reg_staging s
JOIN player p USING (player_name);
SQL

# Populate deck_content from tournaments/{tournament_id}/{deck_id}.json snapshots
TOURN_DECK_CSV=$(mktemp)
python3 - "$DATA/tournaments" <<'PYEOF' > "$TOURN_DECK_CSV"
import sys, csv, os

tournaments_dir = sys.argv[1]
writer = csv.writer(sys.stdout)

if not os.path.isdir(tournaments_dir):
    sys.exit(0)

for tourn_entry in os.scandir(tournaments_dir):
    if not tourn_entry.is_dir():
        continue
    tournament_id = tourn_entry.name
    for f in os.scandir(tourn_entry.path):
        if not f.name.endswith('.json'):
            continue
        deck_id = f.name[:-5]
        try:
            content = open(f.path, encoding='utf-8', errors='replace').read()
            writer.writerow([tournament_id, deck_id, content])
        except Exception as e:
            print(f'WARN: skipping {f.path}: {e}', file=sys.stderr)
PYEOF
psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE tourn_deck_staging (
  tournament_id VARCHAR(36),
  deck_id       VARCHAR(36),
  deck_content  TEXT
);
\copy tourn_deck_staging(tournament_id,deck_id,deck_content) FROM '$TOURN_DECK_CSV' WITH (FORMAT csv, NULL '');
UPDATE tournament_registration r
SET deck_content = s.deck_content
FROM tourn_deck_staging s
WHERE r.tournament_id = s.tournament_id AND r.deck_id = s.deck_id;
SQL
rm -f "$TOURN_DECK_CSV"

success "$(jq 'length' "$DATA/tournaments.json") tournaments"
fi

# ── 6. Games ─────────────────────────────────────────────────────────────────
if [[ "$PLAYER_DATA_ONLY" == true ]]; then
  echo
  echo "  ⚠ --player-data-only: skipping games"
else
log "Loading games..."
# Write games CSV with owner_name; resolve to owner_id via LEFT JOIN so SYSTEM games get null owner.
jq -r 'to_entries[] | [
  .value.name,
  .value.id,
  (.value.owner // ""),
  .value.visibility,
  .value.status,
  (.value.gameFormat | ascii_upcase),
  (.value.created // "2000-01-01T00:00:00Z"),
  (if .value.version == "INITIAL" then "0" elif .value.version == "GAME_STATE" then "1" else "2" end),
  (.value.tournamentName // "")
] | @csv' "$DATA/games.json" > "$GAMES_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE games_staging (
  game_name       VARCHAR(255),
  game_id         VARCHAR(36),
  owner_name      VARCHAR(255),
  visibility      VARCHAR(50),
  status          VARCHAR(50),
  game_format     VARCHAR(50),
  created_at      TIMESTAMP WITH TIME ZONE,
  version         INTEGER,
  tournament_name VARCHAR(255)
);
\copy games_staging(game_name,game_id,owner_name,visibility,status,game_format,created_at,version,tournament_name) FROM '$GAMES_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO game (game_name, game_id, owner_id, visibility, status, game_format, created_at, version, tournament_name)
SELECT s.game_name, s.game_id, p.player_id, s.visibility, s.status, s.game_format, s.created_at, s.version, NULLIF(s.tournament_name,'')
FROM games_staging s
LEFT JOIN player p ON p.player_name = s.owner_name AND s.owner_name <> '';
SQL

GAME_COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc "SELECT COUNT(*) FROM game;")
success "$GAME_COUNT games loaded"
fi

# ── 7. Registrations ─────────────────────────────────────────────────────────
if [[ "$PLAYER_DATA_ONLY" == true ]]; then
  echo
  echo "  ⚠ --player-data-only: skipping registrations"
else
log "Loading registrations..."
# registrations.json is keyed by game_name; stage to resolve both game_id and player_id.
jq -r 'to_entries[] | .key as $game | .value | to_entries[] | [
  $game,
  .key,
  .value.deckId,
  .value.deckName,
  (if .value.valid then "true" else "false" end),
  .value.summary,
  .value.timestamp
] | @csv' "$DATA/registrations.json" > "$REG_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE reg_staging (
  game_name     VARCHAR(255),
  player_name   VARCHAR(255),
  deck_id       VARCHAR(36),
  deck_name     VARCHAR(255),
  valid         BOOLEAN,
  summary       TEXT,
  registered_at TIMESTAMP WITH TIME ZONE
);
\copy reg_staging(game_name,player_name,deck_id,deck_name,valid,summary,registered_at) FROM '$REG_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO registration (game_id, player_id, deck_id, deck_name, valid, summary, registered_at)
SELECT g.game_id, p.player_id, NULLIF(s.deck_id,''), NULLIF(s.deck_name,''), s.valid, NULLIF(s.summary,''), s.registered_at
FROM reg_staging s
JOIN game   g ON g.game_name   = s.game_name
JOIN player p ON p.player_name = s.player_name;
SQL

# Populate deck_content from games/{game_id}/{deck_id}.json snapshots
GAME_DECK_CSV=$(mktemp)
python3 - "$DATA/games" <<'PYEOF' > "$GAME_DECK_CSV"
import sys, csv, os

games_dir = sys.argv[1]
writer = csv.writer(sys.stdout)

if not os.path.isdir(games_dir):
    sys.exit(0)

for game_entry in os.scandir(games_dir):
    if not game_entry.is_dir():
        continue
    game_id = game_entry.name
    for f in os.scandir(game_entry.path):
        name = f.name
        # skip known non-deck files: game state, turn snapshots, chat history, XML
        if not name.endswith('.json'):
            continue
        if name in ('game.json', 'history.json') or name.startswith('game-'):
            continue
        deck_id = name[:-5]
        try:
            content = open(f.path, encoding='utf-8', errors='replace').read()
            writer.writerow([game_id, deck_id, content])
        except Exception as e:
            print(f'WARN: skipping {f.path}: {e}', file=sys.stderr)
PYEOF
psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE game_deck_staging (
  game_id      VARCHAR(36),
  deck_id      VARCHAR(36),
  deck_content TEXT
);
\copy game_deck_staging(game_id,deck_id,deck_content) FROM '$GAME_DECK_CSV' WITH (FORMAT csv, NULL '');
UPDATE registration r
SET deck_content = s.deck_content
FROM game_deck_staging s
WHERE r.game_id = s.game_id AND r.deck_id = s.deck_id;
SQL
rm -f "$GAME_DECK_CSV"

success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM registration;') registrations"
fi

# ── 8. Decks ──────────────────────────────────────────────────────────────────
log "Loading deck info..."
# Flatten all decks into a temp file, deduplicated by deck_id
# (same deck_id can appear under multiple player/name combos for renamed decks)
jq '[
  to_entries[] | .key as $player | .value | to_entries[] | {
    player: $player,
    name:   .key,
    deckId: .value.deckId,
    format: .value.format,
    gameFormats: (.value.gameFormats // [])
  }
] | unique_by(.deckId)' "$DATA/decks.json" > "$DEDUPED_DECKS_FILE"

# Stage by player_name; JOIN player to resolve player_id.
jq -r '.[] | [.player, .name, .deckId, .format] | @csv' "$DEDUPED_DECKS_FILE" > "$DECK_INFO_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE deck_info_staging (
  player_name VARCHAR(255),
  deck_name   VARCHAR(255),
  deck_id     VARCHAR(36),
  format      VARCHAR(20)
);
\copy deck_info_staging(player_name,deck_name,deck_id,format) FROM '$DECK_INFO_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO deck_info (player_id, deck_name, deck_id, format)
SELECT p.player_id, s.deck_name, s.deck_id, s.format
FROM deck_info_staging s
JOIN player p USING (player_name);
SQL

DECK_FORMAT_CSV=$(mktemp)
jq -r '.[] | .deckId as $id | .gameFormats[] | [$id, .] | @csv' "$DEDUPED_DECKS_FILE" > "$DECK_FORMAT_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE deck_format_staging (
  deck_id    VARCHAR(36),
  format_tag VARCHAR(50)
);
\copy deck_format_staging(deck_id,format_tag) FROM '$DECK_FORMAT_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO deck_format (deck_id, format_tag)
SELECT s.deck_id, s.format_tag
FROM deck_format_staging s
WHERE EXISTS (SELECT 1 FROM deck_info d WHERE d.deck_id = s.deck_id);
SQL
rm -f "$DECK_FORMAT_CSV"

log "Loading deck content..."
LOADED_DECK_IDS=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc "SELECT deck_id FROM deck_info;")
DECK_CONTENT_CSV=$(mktemp)
python3 - "$DATA/decks" <<PYEOF > "$DECK_CONTENT_CSV"
import json, csv, os, sys

deduped    = json.load(open("$DEDUPED_DECKS_FILE"))
deck_dir   = sys.argv[1]
loaded_ids = set("""$LOADED_DECK_IDS""".split())
writer     = csv.writer(sys.stdout)

for deck in deduped:
    deck_id = deck.get("deckId", "")
    if not deck_id or deck_id not in loaded_ids:
        continue
    for ext in ("json", "txt"):
        path = os.path.join(deck_dir, f"{deck_id}.{ext}")
        if os.path.exists(path):
            try:
                content = open(path, encoding="utf-8", errors="replace").read()
                writer.writerow([deck_id, content])
            except Exception as e:
                print(f"WARN: skipping {path}: {e}", file=sys.stderr)
            break
PYEOF
psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 \
  -c "\copy deck_content(deck_id,content) FROM '$DECK_CONTENT_CSV' WITH (FORMAT csv, NULL '')"
rm -f "$DECK_CONTENT_CSV"

DECK_COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc "SELECT COUNT(*) FROM deck_info;")
CONTENT_COUNT=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc "SELECT COUNT(*) FROM deck_content;")
success "$DECK_COUNT decks ($CONTENT_COUNT with content)"

# ── 9-13. Game states, game chat, game snapshots, game activity, global chat, game history ──
if [[ "$PLAYER_DATA_ONLY" == true ]]; then
  echo
  echo "  ⚠ --player-data-only: skipping game states/chat/snapshots/activity, global chat, and game history"
else
# ── 9. Game states ────────────────────────────────────────────────────────────
log "Loading game states..."
# Only load states for game_ids that were actually inserted
LOADED_GAME_IDS=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc "SELECT game_id FROM game;")
python3 - "$DATA/games" <<PYEOF | copy_into game_state \
  "game_id,state,current_player,turn,phase,player_count,updated_at"
import sys, json, csv, os, datetime

games_dir  = sys.argv[1]

# Only process game_ids that were actually inserted into game
loaded_ids = set("""$LOADED_GAME_IDS""".split())

writer  = csv.writer(sys.stdout)
now     = datetime.datetime.now(datetime.timezone.utc).isoformat()
loaded  = 0

for entry in os.scandir(games_dir):
    if not entry.is_dir():
        continue
    game_id   = entry.name
    game_json = os.path.join(entry.path, "game.json")

    if game_id not in loaded_ids or not os.path.exists(game_json):
        continue
    try:
        state = json.load(open(game_json))
        cp = state.get("currentPlayer") or ""
        writer.writerow([
            game_id,
            json.dumps(state),
            cp if isinstance(cp, str) else "",
            state.get("turn", ""),
            state.get("phase") or "",
            len(state.get("playerOrder", [])),
            now,
        ])
        loaded += 1
    except Exception as e:
        print(f"WARN: skipping {game_json}: {e}", file=sys.stderr)

print(f"  loaded {loaded} game states", file=sys.stderr)
PYEOF
success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_state;') game states"

# ── 10. Game chat ─────────────────────────────────────────────────────────────
# Row-per-line model (game_chat_message, V18) is the only game-chat store the app
# reads or writes, so it's the only one loaded here. The legacy game_chat blob
# table is left empty on a fresh import; on an already-migrated database V18's
# own backfill copies any existing blob into game_chat_message, and V19 drops
# the blob table.
#
# invocation / invocation_by are backfilled here from the per-game raw-command
# logs ($DATA/commands/<game name>.log) when present: the log and history.json
# are the same event stream in the same order, so an order-preserving walk
# anchored on (minute, issuer) — absorbing SYSTEM side-effect lines and the
# `draw` line that play/discard append — reassociates each raw command with the
# chat line(s) it produced. Best-effort: anything not confidently matched keeps
# a NULL invocation. Games with no log file are simply skipped.
#
# FORCE_NOT_NULL (message): some prod chat lines have an empty/null message;
# in CSV an unquoted empty field is read as NULL, which violates message's
# NOT NULL. This keeps it an empty string instead (matching how the Java
# paths and V18's COALESCE handle the same case).
log "Loading game chat messages..."
python3 - "$DATA" <<PYEOF | psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 \
  -c "\copy game_chat_message(game_id,turn_seq,chat_seq,turn_id,turn_player,turn_label,posted_at,display_ts,source,message,command,invocation,invocation_by) FROM STDIN WITH (FORMAT csv, FORCE_NOT_NULL (message))"
import sys, json, csv, os, datetime, re
from datetime import timedelta

data_dir   = sys.argv[1]
games_dir  = os.path.join(data_dir, "games")
cmds_dir   = os.path.join(data_dir, "commands")
loaded_ids = set("""$LOADED_GAME_IDS""".split())

# game_id -> display name (raw-command logs are named by display name)
id_to_name = {}
try:
    gj = json.load(open(os.path.join(data_dir, "games.json")))
    for g in (gj.values() if isinstance(gj, dict) else gj):
        if g.get("id") and g.get("name"):
            id_to_name[g["id"]] = g["name"]
except Exception as e:
    print(f"WARN: games.json unreadable; command backfill disabled: {e}", file=sys.stderr)

LOG_RE = re.compile(r'^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})[,.]\d+\s+(INFO|ERROR)\s+\[([^\]]+)\]\s+(.*)\$')

def load_commands(game_name):
    if not game_name:
        return []
    path = os.path.join(cmds_dir, game_name + ".log")
    if not os.path.exists(path):
        return []
    out = []
    with open(path, encoding="utf-8", errors="replace") as fh:
        for line in fh:
            m = LOG_RE.match(line.rstrip("\r\n"))
            if not m:
                continue
            ts, level, issuer, raw = m.groups()
            if level != "INFO":          # ERROR lines produced no chat
                continue
            try:
                dt = datetime.datetime.strptime(ts, "%Y-%m-%dT%H:%M:%S")
            except ValueError:
                continue
            out.append((dt, issuer.strip(), raw.strip()))
    return out

def hist_dt(ts, ref):
    """Full datetime for a year-less 'd-MMM HH:mm' history stamp: history and log
    are the same event, so pick the year (ref-1 / ref / ref+1) that places the
    stamp closest to the log command time -- handles games spanning New Year."""
    ts = (ts or "").strip().replace("Sept ", "Sep ")   # a stray long form seen in old data
    try:
        t = datetime.datetime.strptime("2000 " + ts, "%Y %d-%b %H:%M")  # 2000: leap-safe
    except ValueError:
        return None
    cands = []
    for y in (ref.year - 1, ref.year, ref.year + 1):
        try:
            cands.append(datetime.datetime(y, t.month, t.day, t.hour, t.minute))
        except ValueError:
            pass
    return min(cands, key=lambda d: abs((d - ref).total_seconds())) if cands else None

# raw verb -> synthetic verbs a single command may emit *after* its primary
# line (every other side effect arrives as source="SYSTEM").
FOLLOW_UPS = {"play": {"draw"}, "discard": {"draw"}}
# raw verb -> synthetic verb(s) the same command can carry on its primary line
VERB_ALIASES = {
    "blood":  {"counter", "pool"},
    "unlock": {"lock", "untap"},
    "vp":     {"vp", "withdraw"},
    "choose": {"choice"},
}

def _norm(tok):
    return tok.lstrip("+").lower()

def issuer_matches(chat, issuer):
    if (chat.get("source") or "") == issuer:
        return True
    return issuer in (chat.get("command") or "").split()

def starts_block(chat, issuer, raw):
    """Whether this chat line can be the primary line a raw command produced:
    a genuine command line (synthetic command present) by the right actor,
    with the verb or trailing argument corroborating the pairing. This is what
    stops phase headers and plain chat lines from absorbing a command."""
    syn = (chat.get("command") or "").strip()
    if not syn or not issuer_matches(chat, issuer):
        return False
    rparts, sparts = raw.split(), syn.split()
    if not rparts or not sparts:
        return False
    rverb, sverb = rparts[0].lower(), sparts[0].lower()
    if sverb == rverb or sverb in VERB_ALIASES.get(rverb, set()):
        return True
    return _norm(rparts[-1]) == _norm(sparts[-1]) and _norm(rparts[-1]) != rverb

def align(entries, commands):
    """Order-preserving assignment of each raw command to the run of chat
    entries it produced. Mutates entries in place; returns the match count."""
    li = ei = matched = 0
    n = len(entries)
    while li < len(commands) and ei < n:
        ldt, issuer, raw = commands[li]
        lmin  = ldt.replace(second=0, microsecond=0)
        rverb = raw.split(" ", 1)[0].lower()

        while ei < n:                                   # drop entries before this minute
            hd = hist_dt(entries[ei].get("timestamp"), ldt)
            if hd is None or hd < lmin:
                ei += 1
            else:
                break
        if ei >= n:
            break

        hd = hist_dt(entries[ei].get("timestamp"), ldt)
        if hd is None or hd > lmin + timedelta(minutes=1):
            li += 1                                     # command produced nothing visible
            continue
        if not starts_block(entries[ei], issuer, raw):
            ei += 1                                     # not a corroborated primary line
            continue

        # If the next command is the same actor in the same minute, keep this
        # block to just the primary line (+ SYSTEM / declared follow-ups).
        greedy = not (li + 1 < len(commands)
                      and commands[li + 1][0].replace(second=0, microsecond=0) <= lmin + timedelta(minutes=1)
                      and commands[li + 1][1] == issuer)

        start = ei
        ei += 1
        while ei < n:
            hd2 = hist_dt(entries[ei].get("timestamp"), ldt)
            if hd2 is None or hd2 > lmin + timedelta(minutes=1):
                break
            src   = entries[ei].get("source") or ""
            cverb = (entries[ei].get("command") or "").split(" ", 1)[0].lower()
            if src == "SYSTEM":
                ei += 1
            elif cverb and cverb in FOLLOW_UPS.get(rverb, set()):
                ei += 1
            elif greedy and cverb and issuer_matches(entries[ei], issuer):
                ei += 1                                 # extra command line, same actor, no rival command
            else:
                break

        for j in range(start, ei):
            entries[j]["_inv"]    = raw
            entries[j]["_inv_by"] = issuer
            matched += 1
        li += 1
    return matched

writer = csv.writer(sys.stdout)
now    = datetime.datetime.now(datetime.timezone.utc).isoformat()
rows = matched_total = games_with_log = 0

for entry in os.scandir(games_dir):
    if not entry.is_dir():
        continue
    game_id      = entry.name
    history_json = os.path.join(entry.path, "history.json")
    game_json    = os.path.join(entry.path, "game.json")

    if game_id not in loaded_ids:
        continue
    if not os.path.exists(game_json) or not os.path.exists(history_json):
        continue
    try:
        turns = json.load(open(history_json))
    except Exception as e:
        print(f"WARN: skipping {history_json}: {e}", file=sys.stderr)
        continue

    valid_turns = [t for t in turns
                   if t.get("turnId") is not None and t.get("player") is not None]
    entries = [c for t in valid_turns for c in t.get("chats", [])]

    commands = load_commands(id_to_name.get(game_id))
    if commands:
        games_with_log += 1
        matched_total  += align(entries, commands)

    for turn_seq, turn in enumerate(valid_turns):
        turn_label = f"{turn['player']} {turn['turnId']}"
        for chat_seq, chat in enumerate(turn.get("chats", [])):
            writer.writerow([
                game_id, turn_seq, chat_seq, turn["turnId"], turn["player"], turn_label, now,
                chat.get("timestamp") or "",
                chat.get("source") or "",
                chat.get("message") or "",
                chat.get("command") or "",
                chat.get("_inv") or "",
                chat.get("_inv_by") or "",
            ])
            rows += 1

print(f"  generated {rows} chat message rows; backfilled the raw command for "
      f"{matched_total} of them from {games_with_log} game logs", file=sys.stderr)
PYEOF
success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_chat_message;') game chat messages "\
"($(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_chat_message WHERE invocation IS NOT NULL;') with a backfilled command)"

# ── 10a. Failed command attempts (judge-only) ────────────────────────────────
# The ERROR lines in the per-game command log are mistypes that produced no
# chat. They land in game_command_error (V19), assigned to the turn whose time
# span contains them, so a judge can see an attempt was made during a misplay.
log "Loading failed command attempts..."
python3 - "$DATA" <<PYEOF | psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 \
  -c "\copy game_command_error(game_id,turn_label,occurred_at,display_ts,player,raw_command,error_text) FROM STDIN WITH (FORMAT csv)"
import sys, json, csv, os, datetime, re

data_dir   = sys.argv[1]
games_dir  = os.path.join(data_dir, "games")
cmds_dir   = os.path.join(data_dir, "commands")
loaded_ids = set("""$LOADED_GAME_IDS""".split())

id_to_name = {}
try:
    gj = json.load(open(os.path.join(data_dir, "games.json")))
    for g in (gj.values() if isinstance(gj, dict) else gj):
        if g.get("id") and g.get("name"):
            id_to_name[g["id"]] = g["name"]
except Exception:
    pass

LOG_RE = re.compile(r'^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})[,.]\d+\s+(INFO|ERROR)\s+\[([^\]]+)\]\s+(.*)\$')

def hist_dt(ts, ref):
    ts = (ts or "").strip().replace("Sept ", "Sep ")
    try:
        t = datetime.datetime.strptime("2000 " + ts, "%Y %d-%b %H:%M")
    except ValueError:
        return None
    cands = []
    for y in (ref.year - 1, ref.year, ref.year + 1):
        try:
            cands.append(datetime.datetime(y, t.month, t.day, t.hour, t.minute))
        except ValueError:
            pass
    return min(cands, key=lambda d: abs((d - ref).total_seconds())) if cands else None

writer = csv.writer(sys.stdout)
rows = games_seen = 0

for entry in os.scandir(games_dir):
    if not entry.is_dir() or entry.name not in loaded_ids:
        continue
    name = id_to_name.get(entry.name)
    log_path = os.path.join(cmds_dir, (name or "") + ".log")
    hist_path = os.path.join(entry.path, "history.json")
    if not name or not os.path.exists(log_path) or not os.path.exists(hist_path):
        continue

    try:
        turns = json.load(open(hist_path))
    except Exception:
        continue
    # (turn_label, first-entry timestamp string) in order
    turn_marks = []
    for t in turns:
        if t.get("turnId") is None or t.get("player") is None:
            continue
        chats = t.get("chats", [])
        first_ts = chats[0].get("timestamp") if chats else None
        turn_marks.append((f"{t['player']} {t['turnId']}", first_ts))
    if not turn_marks:
        continue

    errs = []
    with open(log_path, encoding="utf-8", errors="replace") as fh:
        for line in fh:
            m = LOG_RE.match(line.rstrip("\r\n"))
            if not m or m.group(2) != "ERROR":
                continue
            ts, _lvl, issuer, raw = m.groups()
            try:
                dt = datetime.datetime.strptime(ts, "%Y-%m-%dT%H:%M:%S")
            except ValueError:
                continue
            errs.append((dt, issuer.strip(), raw.strip()))
    if not errs:
        continue
    games_seen += 1

    for dt, issuer, raw in errs:
        # turn whose start is the latest that is still <= this error's time
        label = turn_marks[0][0]
        for lbl, first_ts in turn_marks:
            hd = hist_dt(first_ts, dt)
            if hd is not None and hd <= dt + datetime.timedelta(minutes=1):
                label = lbl
            else:
                break
        writer.writerow([
            entry.name, label,
            dt.replace(tzinfo=datetime.timezone.utc).isoformat(),
            dt.strftime("%-d-%b %H:%M "),
            issuer, raw, "",
        ])
        rows += 1

print(f"  {rows} failed attempts from {games_seen} game logs", file=sys.stderr)
PYEOF
success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_command_error;') failed command attempts"

# ── 10b. Game turn snapshots (rollback) ───────────────────────────────────────
log "Loading game turn snapshots..."
python3 - "$DATA/games" <<PYEOF | copy_into game_snapshot "game_id,turn,state,created_at"
import sys, csv, os, datetime

games_dir  = sys.argv[1]
loaded_ids = set("""$LOADED_GAME_IDS""".split())

writer = csv.writer(sys.stdout)
now    = datetime.datetime.now(datetime.timezone.utc).isoformat()

for entry in os.scandir(games_dir):
    if not entry.is_dir() or entry.name not in loaded_ids:
        continue
    for f in os.scandir(entry.path):
        # snapshot files are game-<turn>.json (turn label with dots normalized to dashes)
        if not (f.name.startswith('game-') and f.name.endswith('.json')):
            continue
        turn = f.name[5:-5]
        try:
            state = open(f.path, encoding='utf-8', errors='replace').read()
            writer.writerow([entry.name, turn, state, now])
        except Exception as e:
            print(f'WARN: skipping {f.path}: {e}', file=sys.stderr)
PYEOF
success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_snapshot;') game snapshots"

# ── 11. Game activity ─────────────────────────────────────────────────────────
log "Loading game activity..."
# game-timestamps.json is keyed by game_name; stage through a temp table to resolve game_id.
jq -r 'to_entries[] | [
  .key,
  .value.timestamp,
  (.value.playerTimestamps | tojson),
  (.value.playerPings | tojson)
] | @csv' "$DATA/game-timestamps.json" > "$ACTIVITY_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE activity_staging (
  game_name         VARCHAR(255),
  last_updated      TIMESTAMP WITH TIME ZONE,
  player_timestamps TEXT,
  player_pings      TEXT
);
\copy activity_staging(game_name,last_updated,player_timestamps,player_pings) FROM '$ACTIVITY_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO game_activity (game_id, last_updated, player_timestamps, player_pings)
SELECT g.game_id, s.last_updated, COALESCE(s.player_timestamps,'{}'), COALESCE(s.player_pings,'{}')
FROM activity_staging s
JOIN game g USING (game_name);
SQL

success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_activity;') game activity entries"

# ── 12. Global chat ───────────────────────────────────────────────────────────
log "Loading global chat (last 1000 entries)..."
# chats.json has player_name; join to player to resolve player_id.
# Chat from unknown/deleted players is silently dropped.
jq -r '.[-1000:] | .[] | [.player, .message, .timestamp] | @csv' "$DATA/chats.json" > "$GLOBAL_CHAT_CSV"

psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE global_chat_staging (
  player_name VARCHAR(255),
  message     TEXT,
  posted_at   TIMESTAMP WITH TIME ZONE
);
\copy global_chat_staging(player_name,message,posted_at) FROM '$GLOBAL_CHAT_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO global_chat (player_id, message, posted_at)
SELECT p.player_id, s.message, s.posted_at
FROM global_chat_staging s
JOIN player p USING (player_name);
SQL

success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM global_chat;') chat entries"

# ── 13. Game history ──────────────────────────────────────────────────────────
log "Loading game history..."
if [[ -f "$DATA/pastGames.json" ]]; then
  jq -r 'to_entries[] | [
    .key,
    .value.name,
    (.value.started // ""),
    (.value.ended // ""),
    ((.value.results // []) | tojson)
  ] | @csv' "$DATA/pastGames.json" \
  | copy_into game_history "recorded_at,game_name,started,ended,results"
  success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_history;') game histories"
else
  echo "  ⚠ no pastGames.json found — skipping game history"
fi
fi

# ── 14. Refresh tokens ────────────────────────────────────────────────────────
log "Loading refresh tokens..."
if [[ -f "$DATA/refreshTokens.json" ]]; then
  REFRESH_TOKEN_CSV=$(mktemp)
  jq -r 'to_entries[] | .value[] | [
    .id, .playerName, .secretHash, (.deviceLabel // ""), .createdAt, .lastUsedAt, .expiresAt, .remember
  ] | @csv' "$DATA/refreshTokens.json" > "$REFRESH_TOKEN_CSV"

  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE refresh_token_staging (
  id           VARCHAR(36),
  player_name  VARCHAR(255),
  secret_hash  VARCHAR(64),
  device_label VARCHAR(255),
  created_at   BIGINT,
  last_used_at BIGINT,
  expires_at   BIGINT,
  remember     BOOLEAN
);
\copy refresh_token_staging FROM '$REFRESH_TOKEN_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO refresh_token (id, player_id, secret_hash, device_label, created_at, last_used_at, expires_at, remember)
SELECT s.id, p.player_id, s.secret_hash, s.device_label, s.created_at, s.last_used_at, s.expires_at, s.remember
FROM refresh_token_staging s
JOIN player p ON p.player_name = s.player_name;
SQL
  success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM refresh_token;') refresh tokens"
else
  echo "  ⚠ no refreshTokens.json found — skipping refresh tokens"
fi

# ── 15. Site notes ───────────────────────────────────────────────────────────
log "Loading site notes..."
if [[ -f "$DATA/site-notes.md" ]]; then
  # site-notes.md is raw markdown; slurp it into a single CSV field so embedded
  # newlines/quotes survive the \copy. The table is a singleton (id = 1).
  jq -Rrs '[.] | @csv' "$DATA/site-notes.md" > "$SITE_NOTES_CSV"

  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE site_notes_staging (notes TEXT);
\copy site_notes_staging(notes) FROM '$SITE_NOTES_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO site_notes (id, notes)
SELECT 1, COALESCE(notes, '') FROM site_notes_staging
ON CONFLICT (id) DO UPDATE SET notes = EXCLUDED.notes;
SQL
  success "site notes loaded"
else
  echo "  ⚠ no site-notes.md found — skipping site notes"
fi

# ── 16. Web push subscriptions ───────────────────────────────────────────────
log "Loading web push subscriptions..."
if [[ -f "$DATA/subscriptions.json" ]]; then
  # subscriptions.json is keyed by player_name -> [ {auth,key,endpoint,failureCount}, ... ].
  # Stage by player_name and JOIN player to resolve player_id; subscriptions for
  # unknown/deleted players are silently dropped.
  jq -r 'to_entries[] | .key as $player | .value[] | [
    $player,
    .endpoint,
    .auth,
    .key,
    (.failureCount // 0)
  ] | @csv' "$DATA/subscriptions.json" > "$SUBSCRIPTION_CSV"

  psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -v ON_ERROR_STOP=1 <<SQL
CREATE TEMP TABLE subscription_staging (
  player_name   VARCHAR(255),
  endpoint      VARCHAR(1024),
  auth_key      VARCHAR(255),
  p256dh_key    VARCHAR(255),
  failure_count INT
);
\copy subscription_staging(player_name,endpoint,auth_key,p256dh_key,failure_count) FROM '$SUBSCRIPTION_CSV' WITH (FORMAT csv, NULL '');
INSERT INTO subscription (player_id, endpoint, auth_key, p256dh_key, failure_count)
SELECT p.player_id, s.endpoint, s.auth_key, s.p256dh_key, COALESCE(s.failure_count, 0)
FROM subscription_staging s
JOIN player p USING (player_name)
WHERE s.endpoint <> '' AND s.auth_key <> '' AND s.p256dh_key <> ''
ON CONFLICT (player_id, endpoint) DO NOTHING;
SQL
  success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM subscription;') web push subscriptions"
else
  echo "  ⚠ no subscriptions.json found — skipping web push subscriptions"
fi

# ── 17. Outstanding JWT access tokens ───────────────────────────────────────
# Swapping the whole DB out from under a running dev server leaves the browser
# holding a still-valid access-token cookie for a player that no longer exists
# (classic: logged in as Player1, then loaded real data). Two things already
# cover this without touching any key material here:
#   * SecurityFilter / the WS handshake reject a token whose subject is gone
#     with a clean 401 (which bounces the SPA to /login), and
#   * this script has just truncated the refresh_token table, so the silent
#     refresh can't re-mint one either.
# The RS256 signing keypair is now a committed dev key on the classpath (or a
# JWT_PRIVATE_KEY_FILE / JWT_PUBLIC_KEY_FILE pair) — nothing to delete/rotate.
echo "  ✓ stale access tokens handled by the subject-not-found 401 guard + refresh_token reset above"

# ── 18. Activity metrics ─────────────────────────────────────────────────────
# Backfill metric_event from the legacy metrics/*.log files (the old
# net.deckserver.metrics log4j2 CSV appender). Site-wide activity, not
# player-scoped, so it rides the full import only.
if [[ "$PLAYER_DATA_ONLY" == true ]]; then
  echo
  echo "  ⚠ --player-data-only: skipping activity metrics"
else
log "Loading activity metrics..."
if [[ -d "$DATA/metrics" ]]; then
  python3 - "$DATA/metrics" <<'PYEOF' \
  | copy_into metric_event "occurred_at,player_name,game_name,did_command,did_chat,is_tournament"
import sys, os, csv, glob, re

metrics_dir = sys.argv[1]
writer = csv.writer(sys.stdout)
# Mirror net.deckserver.game.model.GameNames.isTournament(String) — keep in sync.
TABLE = re.compile(r"Round\s+\d+\s*-\s*Table\s+\d+")

rows = skipped = 0
for path in sorted(glob.glob(os.path.join(metrics_dir, "*", "*.log"))):
    # newline="" + csv.reader handles quoted commas in game names and CRLF endings
    with open(path, encoding="utf-8", errors="replace", newline="") as f:
        for r in csv.reader(f):
            if len(r) != 8:
                skipped += 1
                continue
            y, mo, d, h, player, game, did_cmd, did_chat = r
            if not player.strip() or not game.strip():
                skipped += 1
                continue
            try:
                occurred_at = "%04d-%02d-%02dT%02d:00:00Z" % (int(y), int(mo), int(d), int(h))
            except ValueError:
                skipped += 1
                continue
            dc = did_cmd.strip().lower()
            ch = did_chat.strip().lower()
            if dc not in ("true", "false") or ch not in ("true", "false"):
                skipped += 1
                continue
            if dc == "false" and ch == "false":
                skipped += 1  # violates ck_metric_event_activity
                continue
            is_tourn = ("Final Table" in game) or bool(TABLE.search(game))
            writer.writerow([occurred_at, player, game, dc, ch, "true" if is_tourn else "false"])
            rows += 1
print(f"  parsed {rows} metric rows ({skipped} skipped)", file=sys.stderr)
PYEOF
  success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM metric_event;') metric events"
else
  echo "  ⚠ no metrics/ dir found — skipping activity metrics"
fi
fi

# ── Summary ───────────────────────────────────────────────────────────────────
echo
echo "════════════════════════════════════════"
echo " Migration complete"
echo "════════════════════════════════════════"
for tbl in player player_role player_activity \
            game registration \
            deck_info deck_format deck_content \
            tournament tournament_registration \
            game_state game_chat_message game_command_error game_snapshot \
            game_activity global_chat game_history \
            refresh_token site_notes subscription metric_event; do
  n=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc "SELECT COUNT(*) FROM $tbl;")
  printf "  %-35s %s rows\n" "$tbl" "$n"
done
