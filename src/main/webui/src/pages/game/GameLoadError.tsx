import { Button } from '../../components/ui/Button';

// The "this game couldn't be loaded" state — shown when the game id is missing
// or GET /game/{id}/view errored with no cached snapshot to fall back on.
// `canRetry` is false when there is no game id to retry against.
export function GameLoadError({
  canRetry,
  onRetry,
  onBack,
}: {
  canRetry: boolean;
  onRetry: () => void;
  onBack: () => void;
}) {
  return (
    <div className="flex flex-1 min-h-0 flex-col items-center justify-center gap-3 p-8 text-center">
      <p className="text-sm text-ink">This game couldn’t be loaded.</p>
      <p className="text-xs text-ink-muted">
        It may have been closed, or you don’t have access to it.
      </p>
      <div className="mt-1 flex gap-2">
        {canRetry && (
          <Button variant="secondary" size="sm" onClick={onRetry}>
            Try again
          </Button>
        )}
        <Button variant="primary" size="sm" onClick={onBack}>
          Back to lobby
        </Button>
      </div>
    </div>
  );
}
