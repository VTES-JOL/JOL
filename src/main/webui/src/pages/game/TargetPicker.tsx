import { X } from 'lucide-react';

// Mirrors pick-target-modal.jsp/showTargetPicker() — a lightweight,
// non-blocking banner (no backdrop) shown while a play-card mode with a
// MINION_YOU_CONTROL/SELF/SOMETHING target is pending. Completing the pick
// happens by clicking an on-table card, handled by GamePage's
// onTableCardClick (mirrors cardOnTableClicked()'s dual role).
export function TargetPicker({ cardName, onCancel }: { cardName: string; onCancel: () => void }) {
  return (
    <div
      className="fixed top-2 left-1/2 -translate-x-1/2 rounded border border-line-accent bg-panel text-ink shadow-xl"
      style={{ zIndex: 1055, width: 'min(90vw, 400px)' }}
    >
      <div className="flex justify-between items-center p-2 border-b border-line">
        <span className="font-bold">{cardName}</span>
        <button
          type="button"
          onClick={onCancel}
          aria-label="Cancel"
          className="p-1 rounded hover:bg-hover text-ink-muted"
        >
          <X size={14} />
        </button>
      </div>
      <div className="p-2 text-sm">Pick target.</div>
    </div>
  );
}
