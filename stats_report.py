import csv
import math
import statistics
from collections import defaultdict, Counter
from datetime import datetime
from pathlib import Path

CSV_PATH = Path("csv/data.csv")

DATE_FORMAT = "%d %b %Y %H:%M"


def parse_dt(value: str) -> datetime:
    return datetime.strptime(value.strip(), DATE_FORMAT)


def format_timedelta_hours(total_seconds: float) -> str:
    hours = total_seconds / 3600
    days = total_seconds / 86400
    if days >= 1:
        return f"{days:.1f} days ({hours:.1f} hours)"
    return f"{hours:.1f} hours"


def month_key(dt: datetime) -> str:
    return dt.strftime("%Y-%m")


def percentile(values, p):
    if not values:
        return None
    values = sorted(values)
    k = (len(values) - 1) * p
    f = math.floor(k)
    c = math.ceil(k)
    if f == c:
        return values[int(k)]
    return values[f] * (c - k) + values[c] * (k - f)


def iqr_outliers(values):
    if len(values) < 4:
        return set()

    q1 = percentile(values, 0.25)
    q3 = percentile(values, 0.75)
    iqr = q3 - q1
    lower = q1 - 1.5 * iqr
    upper = q3 + 1.5 * iqr
    return lower, upper


def main():
    if not CSV_PATH.exists():
        raise FileNotFoundError(f"Could not find {CSV_PATH.resolve()}")

    # Raw rows grouped by game
    games = defaultdict(list)

    # Per-player stats across all rows
    player_games = defaultdict(set)  # player -> set of game names
    player_vp_sum = defaultdict(float)
    player_vp_count = defaultdict(int)

    # Monthly stats
    games_started_per_month = Counter()
    vp_by_month = defaultdict(list)  # month -> list of vp values across player rows

    # Game-level stats
    game_lengths = {}  # game -> duration in seconds
    game_meta = {}     # game -> (started, ended, players)

    gw_rows = 0
    total_rows = 0

    with CSV_PATH.open(newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        for row in reader:
            total_rows += 1

            game = row["Game"].strip()
            started = parse_dt(row["Started"])
            ended = parse_dt(row["Ended"])
            player = row["Player"].strip()
            gw = row["GW"].strip()
            vp_raw = row["VP"].strip()

            try:
                vp = float(vp_raw) if vp_raw else 0.0
            except ValueError:
                vp = 0.0

            games[game].append(row)
            player_games[player].add(game)
            player_vp_sum[player] += vp
            player_vp_count[player] += 1

            games_started_per_month[month_key(started)] += 1
            vp_by_month[month_key(started)].append(vp)

            if gw.upper() == "GW":
                gw_rows += 1

    # Derive game-level stats from grouped rows
    for game, rows in games.items():
        started = parse_dt(rows[0]["Started"])
        ended = parse_dt(rows[0]["Ended"])
        duration = (ended - started).total_seconds()
        game_lengths[game] = duration
        game_meta[game] = (started, ended, [r["Player"].strip() for r in rows])

    unique_players = sorted(player_games.keys(), key=str.lower)
    total_games = len(games)
    total_unique_players = len(unique_players)

    lengths = list(game_lengths.values())
    avg_length_seconds = statistics.mean(lengths) if lengths else 0
    median_length_seconds = statistics.median(lengths) if lengths else 0

    # Top active players
    top_active_players = sorted(
        ((p, len(gs)) for p, gs in player_games.items()),
        key=lambda x: (-x[1], x[0].lower())
    )[:10]

    # Top average VP players
    top_avg_vp_players = sorted(
        (
            (p, player_vp_sum[p] / player_vp_count[p], player_vp_count[p])
            for p in player_vp_count
            if player_vp_count[p] > 0
        ),
        key=lambda x: (-x[1], -x[2], x[0].lower())
    )[:10]

    gw_rate = (gw_rows / total_rows * 100) if total_rows else 0

    # Longest / shortest games
    sorted_games = sorted(game_lengths.items(), key=lambda x: x[1])
    shortest_games = sorted_games[:10]
    longest_games = sorted(games.items(), key=lambda kv: (parse_dt(kv[1][0]["Ended"]) - parse_dt(kv[1][0]["Started"])).total_seconds(), reverse=True)[:10]

    # 5+ month duration games (approx using 150 days)
    five_month_plus = [
        (game, duration)
        for game, duration in game_lengths.items()
        if duration >= 150 * 86400
    ]

    # Outlier detection for VP values
    all_vps = []
    vp_records = []
    for game, rows in games.items():
        for row in rows:
            try:
                vp = float(row["VP"].strip()) if row["VP"].strip() else 0.0
            except ValueError:
                vp = 0.0
            all_vps.append(vp)
            vp_records.append((game, row["Player"].strip(), vp))

    bounds = iqr_outliers(all_vps)
    if bounds:
        lower, upper = bounds
        vp_outliers = [
            (game, player, vp)
            for game, player, vp in vp_records
            if vp < lower or vp > upper
        ]
    else:
        lower = upper = None
        vp_outliers = []

    # VP distribution by month
    vp_month_stats = []
    for month in sorted(vp_by_month.keys()):
        vals = vp_by_month[month]
        vp_month_stats.append((
            month,
            len(vals),
            min(vals) if vals else None,
            statistics.mean(vals) if vals else None,
            statistics.median(vals) if vals else None,
            max(vals) if vals else None
        ))

    # Print report
    print("# Game Stats Summary")
    print()

    print("## Overview")
    print()
    print("| Metric | Value |")
    print("|---|---:|")
    print(f"| Total unique players | {total_unique_players} |")
    print(f"| Total games | {total_games} |")
    print(f"| Total rows (player entries) | {total_rows} |")
    print(f"| Average game length | {format_timedelta_hours(avg_length_seconds)} |")
    print(f"| Median game length | {format_timedelta_hours(median_length_seconds)} |")
    print(f"| GW rate overall | {gw_rate:.2f}% |")
    print()

    print("## Active games per month")
    print()
    print("| Month | Active games started |")
    print("|---|---:|")
    for month in sorted(games_started_per_month.keys()):
        print(f"| {month} | {games_started_per_month[month]} |")
    print()

    print("## Top 10 most active players")
    print()
    print("| Player | Games played |")
    print("|---|---:|")
    for player, count in top_active_players:
        print(f"| {player} | {count} |")
    print()

    print("## Top 10 highest average VP players")
    print()
    print("| Player | Avg VP | Games counted |")
    print("|---|---:|---:|")
    for player, avg_vp, count in top_avg_vp_players:
        print(f"| {player} | {avg_vp:.2f} | {count} |")
    print()

    print("## VP distribution by month")
    print()
    print("| Month | Entries | Min VP | Avg VP | Median VP | Max VP |")
    print("|---|---:|---:|---:|---:|---:|")
    for month, entries, mn, avg, med, mx in vp_month_stats:
        print(f"| {month} | {entries} | {mn:.2f} | {avg:.2f} | {med:.2f} | {mx:.2f} |")
    print()

    print("## Longest games")
    print()
    print("| Game | Duration | Started | Ended |")
    print("|---|---:|---|---|")
    for game, rows in longest_games[:10]:
        started = parse_dt(rows[0]["Started"])
        ended = parse_dt(rows[0]["Ended"])
        duration = (ended - started).total_seconds()
        print(f"| {game} | {format_timedelta_hours(duration)} | {started:%Y-%m-%d %H:%M} | {ended:%Y-%m-%d %H:%M} |")
    print()

    print("## Shortest games")
    print()
    print("| Game | Duration | Started | Ended |")
    print("|---|---:|---|---|")
    for game, duration in shortest_games[:10]:
        rows = games[game]
        started = parse_dt(rows[0]["Started"])
        ended = parse_dt(rows[0]["Ended"])
        print(f"| {game} | {format_timedelta_hours(duration)} | {started:%Y-%m-%d %H:%M} | {ended:%Y-%m-%d %H:%M} |")
    print()

    print("## Games with 5+ month duration")
    print()
    print("| Game | Duration |")
    print("|---|---:|")
    if five_month_plus:
        for game, duration in sorted(five_month_plus, key=lambda x: x[1], reverse=True):
            print(f"| {game} | {format_timedelta_hours(duration)} |")
    else:
        print("| _None found_ |  |")
    print()

    print("## VP outlier detection")
    print()
    if lower is not None:
        print(f"- IQR bounds: **{lower:.2f}** to **{upper:.2f}**")
        print(f"- Outlier rows found: **{len(vp_outliers)}**")
        print()
        print("| Game | Player | VP |")
        print("|---|---|---:|")
        for game, player, vp in vp_outliers[:50]:
            print(f"| {game} | {player} | {vp:.2f} |")
        if len(vp_outliers) > 50:
            print()
            print(f"_Showing first 50 of {len(vp_outliers)} outliers._")
    else:
        print("_Not enough data for outlier detection._")


if __name__ == "__main__":
    main()