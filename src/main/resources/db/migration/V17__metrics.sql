-- V17: Activity metrics fact table.
--
-- Replaces the net.deckserver.metrics log4j2 CSV appender (one row per game
-- submit that carried a command and/or chat). Write-through-ish: MetricsService
-- batches rows in memory and flushes on a schedule. Read side is ad-hoc
-- aggregation straight off this table (no in-memory cache) — see
-- MetricEventRepository / MetricsResource.
--
-- Historical rows are backfilled from the legacy metrics/*.log files by
-- migrate-to-db.sh (section 18); those only ever recorded UTC and only to the
-- hour, so their occurred_at is always HH:00:00Z. Rows written by the running
-- app carry full precision.

CREATE TABLE metric_event (
    id            BIGSERIAL   NOT NULL,
    occurred_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    player_name   TEXT        NOT NULL,
    game_name     TEXT        NOT NULL,
    did_command   BOOLEAN     NOT NULL DEFAULT FALSE,
    did_chat      BOOLEAN     NOT NULL DEFAULT FALSE,
    is_tournament BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_metric_event PRIMARY KEY (id),
    CONSTRAINT ck_metric_event_activity CHECK (did_command OR did_chat)
);

-- occurred_at is strictly append-ordered and time-correlated — BRIN is kilobytes
-- where a btree would be hundreds of MB, and range scans are all the dashboards do.
CREATE INDEX idx_metric_event_occurred_at        ON metric_event USING brin (occurred_at);
-- Composite btrees for the per-player / per-game drill-down endpoints.
CREATE INDEX idx_metric_event_player_occurred_at ON metric_event (player_name, occurred_at);
CREATE INDEX idx_metric_event_game_occurred_at   ON metric_event (game_name, occurred_at);
