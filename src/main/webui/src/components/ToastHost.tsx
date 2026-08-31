import { X } from 'lucide-react';
import { dismissToast, useToasts } from '../stores/toast';

// Mounted once at the app root (see App.tsx), fixed to the bottom-right —
// showError()/showSuccess() push into this from anywhere, no prop drilling.
export function ToastHost() {
  const toasts = useToasts();
  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-0 right-0 p-3 flex flex-col gap-2" style={{ zIndex: 1080 }}>
      {toasts.map((toast) => (
        <div
          key={toast.id}
          role="alert"
          className={`flex items-center gap-2 rounded px-3 py-2 text-sm text-surface shadow-lg ${
            toast.kind === 'error' ? 'bg-blood' : 'bg-online'
          }`}
        >
          <span>{toast.message}</span>
          <button type="button" aria-label="Close" className="ml-2 opacity-80 hover:opacity-100" onClick={() => dismissToast(toast.id)}>
            <X size={14} />
          </button>
        </div>
      ))}
    </div>
  );
}
