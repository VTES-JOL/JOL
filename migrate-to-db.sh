#!/usr/bin/env bash
# migrate-to-db.sh — Reinitialise PostgreSQL from JOL file-based data
#
# Usage:
#   ./migrate-to-db.sh [DATA_DIR]
#
# DATA_DIR defaults to ~/data. Override DB connection via env vars:
#   PGHOST PGPORT PGDATABASE PGUSER PGPASSWORD
#
# Prerequisites: psql, jq, python3

set -euo pipefail

DATA="${1:-$HOME/data}"
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

for f in players.json games.json registrations.json decks.json tournaments.json \
          player-timestamps.json game-timestamps.json chats.json; do
  [[ -f "$DATA/$f" ]] || { echo "ERROR: $DATA/$f not found"; exit 1; }
done

echo "Migrating data from: $DATA"
echo "Target database:     $PGUSER@$PGHOST:$PGPORT/$PGDATABASE"
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
trap 'rm -f "$DEDUPED_DECKS_FILE" "$REG_CSV" "$ACTIVITY_CSV" "$PLAYER_ACTIVITY_CSV" "$TOURNAMENT_REG_CSV" "$GAMES_CSV" "$DECK_INFO_CSV" "$GLOBAL_CHAT_CSV"' EXIT

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

# ── 6. Games ─────────────────────────────────────────────────────────────────
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

# ── 7. Registrations ─────────────────────────────────────────────────────────
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
log "Loading game chat..."
python3 - "$DATA/games" <<PYEOF | copy_into game_chat "game_id,history,updated_at"
import sys, json, csv, os, datetime

games_dir  = sys.argv[1]
loaded_ids = set("""$LOADED_GAME_IDS""".split())

writer = csv.writer(sys.stdout)
now    = datetime.datetime.now(datetime.timezone.utc).isoformat()

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
        writer.writerow([game_id, json.dumps(turns), now])
    except Exception as e:
        print(f"WARN: skipping {history_json}: {e}", file=sys.stderr)
PYEOF
success "$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc 'SELECT COUNT(*) FROM game_chat;') game chat histories"

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

# ── Summary ───────────────────────────────────────────────────────────────────
echo
echo "════════════════════════════════════════"
echo " Migration complete"
echo "════════════════════════════════════════"
for tbl in player player_role player_activity \
            game registration \
            deck_info deck_format deck_content \
            tournament tournament_registration \
            game_state game_chat game_snapshot \
            game_activity global_chat game_history; do
  n=$(psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -Atc "SELECT COUNT(*) FROM $tbl;")
  printf "  %-35s %s rows\n" "$tbl" "$n"
done
