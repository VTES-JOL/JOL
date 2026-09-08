import { X } from 'lucide-react';
import { useIsMobile } from '../../hooks/useMediaQuery';

// Mirrors pick-target-modal.jsp/showTargetPicker() — a lightweight,
// non-blocking banner (no backdrop) shown while a play-card mode with a
// MINION_YOU_CONTROL/SELF/SOMETHING target is pending. Completing the pick
// happens by clicking an on-table card, handled by GamePage's
// onTableCardClick (mirrors cardOnTableClicked()'s dual role).
export function TargetPicker({
  cardName,
  prompt = 'Pick target.',
  onCancel,
}: {
  cardName: string;
  prompt?: string;
  onCancel: () => void;
}) {
  // <md: sit just above the bottom tab bar in the thumb zone (ActSheet.dc.html)
  // rather than pinned under the HUD at the top.
  const isMobile = useIsMobile();
  return (
    <div
      className={`fixed left-1/2 -translate-x-1/2 rounded border border-line-accent bg-panel text-ink shadow-xl ${
        isMobile ? 'bottom-20' : 'top-2'
      }`}
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
      <div className="p-2 text-sm">{prompt}</div>
    </div>
  );
}
