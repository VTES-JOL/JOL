// Canonical VTES turn phases, in order (rules relay, game-flow.md:44). Rulebook
// names verbatim; GameSnapshot.phase / .phases ship exactly these strings
// ("Unlock", not the legacy "Untap"). No wake/react/combat/referendum node —
// those happen inside Minion.
export const PHASES = ['Unlock', 'Master', 'Minion', 'Influence', 'Discard'] as const;
export type Phase = (typeof PHASES)[number];
