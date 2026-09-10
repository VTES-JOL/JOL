import type { BoardDensity } from './boardDensity';

// The dock's board-layout switch: tiles ⇄ text rows. The label shows the mode
// it switches TO (matching the rest of the dock's toggle affordances).
export function BoardDensityToggle({
  density,
  onToggle,
}: {
  density: BoardDensity;
  onToggle: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onToggle}
      className="rounded border border-line-accent px-1.5 py-0.5 text-[0.65rem] font-semibold uppercase tracking-wide text-ink-muted hover:border-ink hover:text-ink"
      title="Toggle board layout (tiles / text rows)"
    >
      {density === 'text' ? '▦ Tiles' : '▤ Text'}
    </button>
  );
}
