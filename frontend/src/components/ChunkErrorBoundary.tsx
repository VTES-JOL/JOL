import { Component, type ErrorInfo, type ReactNode } from 'react';

// Reactive backstop for a stale tab whose lazily-loaded chunk (e.g. a help
// section's MDX module — see HelpSection.tsx's lazy()) no longer exists on
// the server because a new deploy replaced it with a differently-hashed
// file. UpdateBanner (updateCheck.ts) is the proactive path for this same
// problem; this one covers the gap between a deploy landing and the next
// poll tick actually firing.
//
// React 19 still has no hook for componentDidCatch — a class component is
// the only way to implement an error boundary.
const CHUNK_LOAD_ERROR = /failed to fetch dynamically imported module|error loading dynamically imported module|importing a module script failed/i;

// Guards against a reload loop: if reloading once didn't fix it, the failure
// isn't a stale-bundle problem (e.g. the asset host is actually down), so
// stop retrying and show a manual fallback instead.
const RELOAD_GUARD_KEY = 'jol-chunk-error-reloaded';

interface State {
  error: Error | null;
}

export class ChunkErrorBoundary extends Component<{ children: ReactNode }, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error('[ChunkErrorBoundary]', error, info.componentStack);

    if (CHUNK_LOAD_ERROR.test(error.message) && !sessionStorage.getItem(RELOAD_GUARD_KEY)) {
      sessionStorage.setItem(RELOAD_GUARD_KEY, '1');
      location.reload();
    }
  }

  render() {
    if (this.state.error) {
      return (
        <div className="d-flex flex-column align-items-center justify-content-center text-center p-4 gap-2 flex-grow-1">
          <h4 className="mb-0">Something went wrong</h4>
          <p className="text-muted mb-0">{this.state.error.message}</p>
          <button className="btn btn-outline-secondary btn-sm mt-2" onClick={() => location.reload()}>
            Reload
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
