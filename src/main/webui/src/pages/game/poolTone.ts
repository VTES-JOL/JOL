// N3 — the pool pill carries magnitude, not just sign. Every pool > 0 used to
// render the same red, so a seat on 2 looked identical to one on 26. A seat
// about to be ousted should read as urgent from across the table.
//
//   0        neutral ink   (ousted / on the brink)
//   1–4      blood red     (one good bleed from out)
//   5–9      gold          (pressured)
//   ≥10      calm surface  (healthy)
//   <0       gold          (a transient over-pay mid-command)
export function poolTone(pool: number): string {
  if (pool === 0) return 'bg-ink text-base';
  if (pool < 0) return 'bg-gold text-surface';
  if (pool <= 4) return 'bg-blood text-surface';
  if (pool <= 9) return 'bg-gold text-surface';
  return 'bg-hover text-ink';
}
