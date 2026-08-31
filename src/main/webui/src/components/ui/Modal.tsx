import { useEffect, type MouseEvent, type ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

/**
 * Tailwind modal. The overlay/panel markup is the one that grew inside
 * `pages/deck/DeckImportModal.tsx`; this pulls it out and adds what it lacked:
 * Escape-to-close, backdrop-click-to-close, and a portal to `document.body`.
 *
 * `title` renders the standard header (title + close button); `footer` renders
 * the footer row. `children` is the body, wrapped in `bodyClassName` (default:
 * padded, scrollable, grows).
 */
const SIZE_CLASS: Record<'sm' | 'md' | 'lg', string> = {
  sm: 'max-w-sm',
  md: 'max-w-lg',
  lg: 'max-w-2xl',
};

export function Modal({
  onClose,
  title,
  footer,
  size = 'md',
  bodyClassName = 'flex flex-col gap-3 p-4 min-h-0 overflow-y-auto flex-1',
  children,
}: {
  onClose?: () => void;
  title?: ReactNode;
  footer?: ReactNode;
  size?: 'sm' | 'md' | 'lg';
  bodyClassName?: string;
  children: ReactNode;
}) {
  useEffect(() => {
    if (!onClose) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [onClose]);

  const onBackdropMouseDown = (e: MouseEvent<HTMLDivElement>) => {
    if (onClose && e.target === e.currentTarget) onClose();
  };

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
      onMouseDown={onBackdropMouseDown}
    >
      <div
        className={`relative flex flex-col w-full ${SIZE_CLASS[size]} max-h-[85dvh] rounded-lg border border-line-accent bg-surface shadow-xl`}
        role="dialog"
        aria-modal="true"
      >
        {title != null && (
          <div className="flex items-center justify-between px-4 py-3 border-b border-line/75 bg-panel/45">
            <h2 className="text-sm font-medium text-ink tracking-wide">{title}</h2>
            {onClose && (
              <button
                type="button"
                onClick={onClose}
                aria-label="Close"
                className="p-1 rounded hover:bg-hover transition-colors cursor-pointer"
              >
                <X className="w-4 h-4 text-ink-muted" />
              </button>
            )}
          </div>
        )}

        <div className={bodyClassName}>{children}</div>

        {footer != null && (
          <div className="flex items-center justify-end gap-2 px-4 py-3 border-t border-line/75">
            {footer}
          </div>
        )}
      </div>
    </div>,
    document.body,
  );
}
