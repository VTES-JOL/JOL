// Shared "nothing selected yet" placeholder for detail/preview panels
// (Lobby/Tournament/Deck) — wrapped in the same `card shadow` every other
// panel uses, so an empty selection reads as an intentional, styled panel
// instead of a blank gap in the layout.
export function EmptyState({ icon, message }: { icon: string; message: string }) {
  return (
    <div className="card shadow flex-fill d-flex flex-column align-items-center justify-content-center text-muted min-h-0">
      <i className={`bi ${icon} fs-1 mb-2`} />
      <span>{message}</span>
    </div>
  );
}
