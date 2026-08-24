// Shared first-render loading state for top-level pages that otherwise
// `return null` while their initial fetch is in flight — that produced a
// blank white flash on every navigation to Lobby/Deck/Game/etc.
export function PageLoading() {
  return (
    <div className="flex-fill d-flex align-items-center justify-content-center text-muted">
      <div className="spinner-border me-2" role="status" style={{ width: '1.5rem', height: '1.5rem' }}>
        <span className="visually-hidden">Loading...</span>
      </div>
      Loading...
    </div>
  );
}
