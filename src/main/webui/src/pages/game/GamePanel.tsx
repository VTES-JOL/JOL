import type { ReactNode } from 'react';

// Shared chrome for the four toggling side panels (History/Notes/GameChat/Deck)
// on GamePage — each is a `card shadow` with a `card-header` title + a single
// pill-button that swaps to a sibling panel, and a `card-body`. Only covers
// this exact shape; panels elsewhere in the app (lobby/deck/tournament lists)
// have enough header variation (tabs, badges, multiple buttons) that forcing
// them through the same wrapper would cost more props than it saves.
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
  toggle?: { icon: string; label: string; onClick: () => void };
  children: ReactNode;
}) {
  return (
    <div className={`card shadow${className ? ` ${className}` : ''}`} id={id}>
      <div className="card-header bg-body-secondary justify-content-between d-flex align-items-center">
        <span>{title}</span>
        <span className="d-flex align-items-center">
          {headerExtra}
          {toggle && (
            <button className="border-0 shadow rounded-pill bg-light" onClick={toggle.onClick}>
              <i className={`bi ${toggle.icon} me-2`} />
              {toggle.label}
            </button>
          )}
        </span>
      </div>
      <div className={`card-body${bodyClassName ? ` ${bodyClassName}` : ''}`}>{children}</div>
    </div>
  );
}
