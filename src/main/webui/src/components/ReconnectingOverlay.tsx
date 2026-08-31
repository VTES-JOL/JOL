import { checkNow } from '../stores/connectivity';

// An overlay, not a full-page replacement: the previous version swapped out
// <Routes> entirely while offline, which unmounted MainPage and everything
// under it — GlobalChat lost scroll position, GamesPanel reset to its
// default tab, in-progress chat input vanished, all re-fetched from scratch
// on reconnect instead of resuming. This sits on top of the still-mounted
// page instead, so that state survives a blip.
export function ReconnectingOverlay({ everConnected }: { everConnected: boolean }) {
  return (
    <div
      className="position-absolute top-0 start-0 w-100 h-100 d-flex flex-column align-items-center justify-content-center text-center p-4 gap-2"
      style={{ background: 'var(--bs-body-bg)', zIndex: 1050 }}
    >
      <div className="spinner-border text-secondary" role="status">
        <span className="visually-hidden">Reconnecting…</span>
      </div>
      {/* Same underlying "offline" state either way, but a cold load against an
          already-dead backend was never a connection to "lose" in the first place. */}
      <h4 className="mt-2 mb-0">{everConnected ? 'Connection lost' : 'Unable to connect'}</h4>
      <p className="text-muted mb-0">Trying to reconnect to the server…</p>
      <button className="btn btn-outline-secondary btn-sm mt-2" onClick={checkNow}>
        Retry now
      </button>
    </div>
  );
}
