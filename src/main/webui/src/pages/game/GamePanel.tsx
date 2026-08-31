import type { ReactNode } from 'react';

// Shared chrome for the four toggling side panels (History/Notes/GameChat/Deck)
// on GamePage — each is a framed card with a header title + a single
// pill-button that swaps to a sibling panel, and a body. Only covers this
// exact shape; panels elsewhere in the app use ui/Panel.
export function GamePanel({
  id,
  className,
  bodyClassName,
  title,
  headerExtra,
  toggle,
  children,
}: {
  id?: string;
  className?: string;
  bodyClassName?: string;
  title: ReactNode;
  headerExtra?: ReactNode;
  toggle?: { icon: ReactNode; label: string; onClick: () => void };
  children: ReactNode;
}) {
  return (
    <div
      className={`flex flex-col min-h-0 rounded-lg border border-line-accent bg-surface/85 shadow-lg overflow-hidden${
        className ? ` ${className}` : ''
      }`}
      id={id}
    >
      <div className="flex items-center justify-between px-3 py-1.5 border-b border-line bg-panel/60 shrink-0">
        <span className="text-sm font-semibold text-ink">{title}</span>
        <span className="flex items-center gap-2">
          {headerExtra}
          {toggle && (
            <button
              type="button"
              className="inline-flex items-center gap-1.5 rounded-full border border-line-accent bg-surface px-2.5 py-1 text-xs text-ink-secondary hover:bg-hover shadow-sm"
              onClick={toggle.onClick}
            >
              {toggle.icon}
              {toggle.label}
            </button>
          )}
        </span>
      </div>
      <div className={`flex-1 min-h-0${bodyClassName ? ` ${bodyClassName}` : ''}`}>{children}</div>
    </div>
  );
}
