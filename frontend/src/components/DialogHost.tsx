import { useEffect } from 'react';
import { resolveDialog, useDialogRequest } from './dialog';

// Mounted once at the app root (see App.tsx) — renders whichever
// confirmDialog()/alertDialog() request is currently pending, styled
// consistently with every other modal in this app (`modal d-block` +
// rgba backdrop, same as ImportTablesModal etc.) instead of the browser's
// native confirm()/alert() chrome.
export function DialogHost() {
  const request = useDialogRequest();

  useEffect(() => {
    if (!request) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') resolveDialog(false);
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [request]);

  if (!request) return null;

  return (
    <div className="modal d-block" tabIndex={-1} style={{ background: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog" role="document">
        <div className="modal-content">
          {request.title && (
            <div className="modal-header">
              <h5 className="modal-title">{request.title}</h5>
            </div>
          )}
          <div className="modal-body">
            <p className="mb-0">{request.message}</p>
          </div>
          <div className="modal-footer">
            {request.mode === 'confirm' && (
              <button type="button" className="btn btn-secondary" onClick={() => resolveDialog(false)}>
                {request.cancelLabel ?? 'Cancel'}
              </button>
            )}
            <button
              type="button"
              autoFocus
              className={`btn ${request.danger ? 'btn-danger' : 'btn-primary'}`}
              onClick={() => resolveDialog(true)}
            >
              {request.confirmLabel ?? (request.mode === 'alert' ? 'OK' : 'Confirm')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
