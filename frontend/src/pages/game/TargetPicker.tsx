// Mirrors pick-target-modal.jsp/showTargetPicker() — a lightweight,
// non-blocking banner (no backdrop) shown while a play-card mode with a
// MINION_YOU_CONTROL/SELF/SOMETHING target is pending. Completing the pick
// happens by clicking an on-table card, handled by GamePage's
// onTableCardClick (mirrors cardOnTableClicked()'s dual role).
export function TargetPicker({ cardName, onCancel }: { cardName: string; onCancel: () => void }) {
  return (
    <div
      className="position-fixed top-0 start-50 translate-middle-x mt-2 shadow border-2 border-dark bg-secondary-subtle rounded"
      style={{ zIndex: 1055, width: 'min(90vw, 400px)' }}
    >
      <div className="d-flex justify-content-between align-items-center p-2 border-bottom">
        <span className="fw-bold">{cardName}</span>
        <button type="button" className="btn-close" onClick={onCancel} aria-label="Cancel" />
      </div>
      <div className="p-2">Pick target.</div>
    </div>
  );
}
