import { dismissToast, useToasts } from './toast';

// Mounted once at the app root (see App.tsx), fixed to the bottom-right —
// showError()/showSuccess() push into this from anywhere, no prop drilling.
export function ToastHost() {
  const toasts = useToasts();
  if (toasts.length === 0) return null;

  return (
    <div className="position-fixed bottom-0 end-0 p-3" style={{ zIndex: 1080 }}>
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`toast show align-items-center text-bg-${toast.kind === 'error' ? 'danger' : 'success'} border-0 mb-2`}
          role="alert"
        >
          <div className="d-flex">
            <div className="toast-body">{toast.message}</div>
            <button
              type="button"
              className="btn-close btn-close-white me-2 m-auto"
              aria-label="Close"
              onClick={() => dismissToast(toast.id)}
            />
          </div>
        </div>
      ))}
    </div>
  );
}
