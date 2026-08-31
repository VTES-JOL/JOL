import {
  useEffect,
  type CSSProperties,
  type MouseEvent,
  type ReactNode,
} from "react";

// Shared chrome for every hand-rolled modal in this app (this app doesn't
// load Bootstrap's JS bundle — see TopBar's dropdown comments — so modals
// are plain conditionally-rendered overlays, not bootstrap.Modal). Centralizes
// the backdrop/dialog/content wrapper plus Escape-to-close and
// click-outside-to-close, which used to be copy-pasted (inconsistently —
// only one of seven call sites had Escape handling) into every modal.
//
// `title` and `footer` render the standard `.modal-header` (title + close
// button) and `.modal-footer` so callers stop re-typing that boilerplate;
// pass neither and put your own structure in `children`. `children` is not
// auto-wrapped in `.modal-body` — supply that yourself when you want it.
export function Modal({
  onClose,
  size,
  title,
  footer,
  contentClassName,
  contentStyle,
  children,
}: {
  onClose?: () => void;
  size?: "lg";
  title?: ReactNode;
  footer?: ReactNode;
  contentClassName?: string;
  contentStyle?: CSSProperties;
  children: ReactNode;
}) {
  useEffect(() => {
    if (!onClose) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [onClose]);

  const onBackdropMouseDown = (e: MouseEvent<HTMLDivElement>) => {
    if (onClose && e.target === e.currentTarget) onClose();
  };

  return (
    <div
      className="modal d-block"
      tabIndex={-1}
      style={{ background: "rgba(0,0,0,0.5)" }}
      onMouseDown={onBackdropMouseDown}
    >
      <div
        className={`modal-dialog${size === "lg" ? " modal-lg" : ""}`}
        role="document"
      >
        <div
          className={`modal-content${contentClassName ? ` ${contentClassName}` : ""}`}
          style={contentStyle}
        >
          {title != null && (
            <div className="modal-header">
              <h5 className="modal-title">{title}</h5>
              {onClose && (
                <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
              )}
            </div>
          )}
          {children}
          {footer != null && <div className="modal-footer">{footer}</div>}
        </div>
      </div>
    </div>
  );
}
