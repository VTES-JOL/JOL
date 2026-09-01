import { reloadForUpdate, useUpdateAvailable } from '../updateCheck';

// A slim persistent bar, not a Toast (auto-dismisses after 6s — wrong for
// something the user should be able to act on whenever they notice it) and
// not a full ReconnectingOverlay (this isn't blocking — the stale bundle
// still works until the user navigates into a chunk that's gone, see
// ChunkErrorBoundary). Sits above everything else so it's visible regardless
// of which page/dialog is open.
// `visible` overrides the real hook value when passed — updateCheck.ts's
// updateAvailable flag only ever flips via a real version.json poll
// (startUpdateCheck(), a no-op in dev), so this is the seam Storybook uses
// to render the "update available" state without faking that poll.
export function UpdateBanner({ visible }: { visible?: boolean }) {
  const updateAvailable = useUpdateAvailable();
  if (!(visible ?? updateAvailable)) return null;

  return (
    <div
      className="fixed top-0 left-0 right-0 flex items-center justify-center gap-3 py-2 bg-accent text-surface text-sm"
      style={{ zIndex: 1090 }}
    >
      <span>A new version is available.</span>
      <button
        className="rounded bg-surface text-ink px-2 py-0.5 text-xs hover:bg-hover"
        onClick={() => void reloadForUpdate()}
      >
        Reload
      </button>
    </div>
  );
}
