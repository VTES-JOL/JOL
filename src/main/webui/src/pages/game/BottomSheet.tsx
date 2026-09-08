import { useEffect, useRef, type ReactNode } from 'react';
import { createPortal } from 'react-dom';

// Shared bottom-sheet chrome for the <md shell (Mobile.dc.html / ActSheet.dc.html):
// a backdrop, a rounded top panel that rises from the bottom edge, a drag-handle
// pill, and a threshold swipe-down-to-dismiss on the handle/header zone (no
// finger-tracked height — D29 scope).
//
// Stays MOUNTED when closed (toggled with `hidden`), so a child's scroll
// position / loaded state survives a close-reopen — that's why the chat sheet
// uses it (D22 open item).
export function BottomSheet({
  open,
  onClose,
  header,
  children,
  maxHeightClass = 'max-h-[85vh]',
  label,
}: {
  open: boolean;
  onClose: () => void;
  // Rendered next to the drag handle; also the swipe-down grab zone.
  header?: ReactNode;
  children: ReactNode;
  maxHeightClass?: string;
  label?: string;
}) {
  const startY = useRef<number | null>(null);
  const startX = useRef<number | null>(null);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open, onClose]);

  const onTouchStart = (e: React.TouchEvent) => {
    startY.current = e.touches[0].clientY;
    startX.current = e.touches[0].clientX;
  };
  const onTouchEnd = (e: React.TouchEvent) => {
    if (startY.current === null || startX.current === null) return;
    const dy = e.changedTouches[0].clientY - startY.current;
    const dx = Math.abs(e.changedTouches[0].clientX - startX.current);
    startY.current = null;
    startX.current = null;
    if (dy > 60 && dy > dx) onClose();
  };

  return createPortal(
    <div
      // pb-16 = MobileTabBar height (h-14) + #table-col's p-2 bottom padding:
      // the sheet rises to just above the bar, which stays visible and
      // interactive on top (its own z-[60]).
      className="fixed inset-0 z-50 flex flex-col justify-end bg-black/40 pb-16"
      hidden={!open}
      onClick={onClose}
      aria-hidden={!open}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-label={label}
        className={`flex ${maxHeightClass} flex-col rounded-t-2xl border-t border-line-accent bg-base`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="shrink-0 cursor-grab" onTouchStart={onTouchStart} onTouchEnd={onTouchEnd}>
          <div className="flex items-center justify-center py-2">
            <span className="h-1 w-10 rounded-full bg-line-accent" />
          </div>
          <div className="flex items-center gap-2 px-4 pb-2 text-sm">
            {header}
            <button
              type="button"
              onClick={onClose}
              className="ml-auto shrink-0 rounded px-2 py-1 text-xs text-ink-muted hover:bg-hover hover:text-ink"
            >
              Close
            </button>
          </div>
        </div>
        <div className="flex min-h-0 flex-1 flex-col overflow-y-auto">{children}</div>
      </div>
    </div>,
    document.body,
  );
}
