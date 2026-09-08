-- Per-player UI theme preference. Flat named list (no light/dark axis) —
-- 'light' plus the four dark variants amethyst / candlelit / oxblood /
-- nightshade defined in src/main/webui/src/styles/tailwind.css. Every
-- existing player defaults to 'light'; the value is the server-side source
-- of truth, surfaced on NavBean and set via PUT /jol/api/profile/theme.
ALTER TABLE player ADD COLUMN theme VARCHAR(32) NOT NULL DEFAULT 'light';
