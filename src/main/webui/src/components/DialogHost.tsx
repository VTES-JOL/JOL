import { resolveDialog, useDialogRequest } from './dialog';
import { Modal } from './Modal';

// Mounted once at the app root (see App.tsx) — renders whichever
// confirmDialog()/alertDialog() request is currently pending, styled
// consistently with every other modal in this app instead of the browser's
// native confirm()/alert() chrome.
export function DialogHost() {
  const request = useDialogRequest();

  if (!request) return null;

  return (
    <Modal onClose={() => resolveDialog(false)}>
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
    </Modal>
  );
}
