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
export function Modal({
  onClose,
  size,
  contentClassName,
  contentStyle,
  children,
}: {
  onClose?: () => void;
  size?: "lg";
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
          {children}
        </div>
      </div>
    </div>
  );
}
