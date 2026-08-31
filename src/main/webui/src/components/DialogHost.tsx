import { resolveDialog, useDialogRequest } from '../stores/dialog';
import { Modal } from './ui/Modal';
import { Button } from './ui/Button';

// Mounted once at the app root (see App.tsx) — renders whichever
// confirmDialog()/alertDialog() request is currently pending, styled
// consistently with every other modal in this app instead of the browser's
// native confirm()/alert() chrome.
export function DialogHost() {
  const request = useDialogRequest();

  if (!request) return null;

  return (
    <Modal
      size="sm"
      onClose={() => resolveDialog(false)}
      title={request.title}
      footer={
        <>
          {request.mode === 'confirm' && (
            <Button variant="secondary" size="sm" onClick={() => resolveDialog(false)}>
              {request.cancelLabel ?? 'Cancel'}
            </Button>
          )}
          <Button
            autoFocus
            variant={request.danger ? 'danger' : 'primary'}
            size="sm"
            onClick={() => resolveDialog(true)}
          >
            {request.confirmLabel ?? (request.mode === 'alert' ? 'OK' : 'Confirm')}
          </Button>
        </>
      }
    >
      <p className="text-sm text-ink">{request.message}</p>
    </Modal>
  );
}
