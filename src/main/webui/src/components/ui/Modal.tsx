import { useEffect, type MouseEvent, type ReactNode } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';

/**
 * Tailwind modal — the `jt:` -prefixed counterpart of `components/Modal.tsx`.
 * The overlay/panel markup is the one that grew inside
 * `pages/deck/DeckImportModal.tsx`; this pulls it out and adds what it lacked:
 * Escape-to-close, backdrop-click-to-close, and a portal to `document.body`
 * with its own `.jt-scope` root (portals render outside the page tree, so the
 * form-control reset in styles/tailwind.css has to be re-applied here).
 *
 * `title` renders the standard header (title + close button); `footer` renders
 * the footer row. `children` is the body, wrapped in `bodyClassName` (default:
 * padded, scrollable, grows).
 */
const SIZE_CLASS: Record<'sm' | 'md' | 'lg', string> = {
  sm: 'jt:max-w-sm',
  md: 'jt:max-w-lg',
  lg: 'jt:max-w-2xl',
};

export function Modal({
  onClose,
  title,
  footer,
  size = 'md',
  bodyClassName = 'jt:flex jt:flex-col jt:gap-3 jt:p-4 jt:min-h-0 jt:overflow-y-auto jt:flex-1',
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
      className="jt-scope jt:fixed jt:inset-0 jt:z-50 jt:flex jt:items-center jt:justify-center jt:p-4 jt:bg-black/60 jt:backdrop-blur-sm"
      onMouseDown={onBackdropMouseDown}
    >
      <div
        className={`jt:relative jt:flex jt:flex-col jt:w-full ${SIZE_CLASS[size]} jt:max-h-[85dvh] jt:rounded-lg jt:border jt:border-line-accent jt:bg-surface jt:shadow-xl`}
        role="dialog"
        aria-modal="true"
      >
        {title != null && (
          <div className="jt:flex jt:items-center jt:justify-between jt:px-4 jt:py-3 jt:border-b jt:border-line/75 jt:bg-panel/45">
            <h2 className="jt:text-sm jt:font-medium jt:text-ink jt:tracking-wide">{title}</h2>
            {onClose && (
              <button
                type="button"
                onClick={onClose}
                aria-label="Close"
                className="jt:p-1 jt:rounded jt:hover:bg-hover jt:transition-colors jt:cursor-pointer"
              >
                <X className="jt:w-4 jt:h-4 jt:text-ink-muted" />
              </button>
            )}
          </div>
        )}

        <div className={bodyClassName}>{children}</div>

        {footer != null && (
          <div className="jt:flex jt:items-center jt:justify-end jt:gap-2 jt:px-4 jt:py-3 jt:border-t jt:border-line/75">
            {footer}
          </div>
        )}
      </div>
    </div>,
    document.body,
  );
}
