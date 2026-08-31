import { checkNow } from '../stores/connectivity';
import { Spinner } from './ui/Spinner';

// An overlay, not a full-page replacement: sits on top of the still-mounted
// page so its state (chat scroll, tab, in-progress input) survives a blip.
export function ReconnectingOverlay({ everConnected }: { everConnected: boolean }) {
  return (
    <div
      className="absolute inset-0 flex flex-col items-center justify-center text-center p-4 gap-2 bg-base text-ink"
      style={{ zIndex: 1050 }}
    >
      <Spinner message="" />
      {/* Same underlying "offline" state either way, but a cold load against an
          already-dead backend was never a connection to "lose" in the first place. */}
      <h4 className="text-lg font-semibold mt-2">{everConnected ? 'Connection lost' : 'Unable to connect'}</h4>
      <p className="text-sm text-ink-muted">Trying to reconnect to the server…</p>
      <button
        className="mt-2 rounded border border-line-accent px-3 py-1.5 text-sm text-ink-secondary hover:bg-hover"
        onClick={checkNow}
      >
        Retry now
      </button>
    </div>
  );
}
