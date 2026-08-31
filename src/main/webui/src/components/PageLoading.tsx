import { Spinner } from './ui/Spinner';

// Shared first-render loading state for top-level pages that otherwise
// `return null` while their initial fetch is in flight — that produced a
// blank white flash on every navigation to Lobby/Deck/Game/etc.
export function PageLoading() {
  return (
    <div className="flex flex-1 items-center justify-center bg-base">
      <Spinner />
    </div>
  );
}
