import { useUpdateAvailable } from '../updateCheck';

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
      className="position-fixed top-0 start-0 w-100 d-flex align-items-center justify-content-center gap-3 py-2 text-bg-info"
      style={{ zIndex: 1090 }}
    >
      <span>A new version is available.</span>
      <button className="btn btn-sm btn-light" onClick={() => location.reload()}>
        Reload
      </button>
    </div>
  );
}
