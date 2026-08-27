import type { ReactNode } from 'react';
import './SplitLayout.css';

/**
 * Shared list/detail split-column layout (a pinned-width left pane, a
 * flex-filling right pane, stacking vertically on narrow screens).
 *
 * `stackBelowLg`: false (default) stacks below 768px and narrows the left
 * pane from 33% to 25% at 992px+ (DeckPage's original breakpoints);
 * true stacks below 992px with a flat 33% left pane (TournamentAdminPage/
 * LobbyPage/TournamentPage's original breakpoints).
 */
export function SplitLayout({
  left,
  right,
  stackBelowLg = false,
  leftClassName = '',
  rightClassName = '',
}: {
  left: ReactNode;
  right: ReactNode;
  stackBelowLg?: boolean;
  leftClassName?: string;
  rightClassName?: string;
}) {
  return (
    <div className={`split-layout p-3${stackBelowLg ? ' split-layout--lg' : ''}`}>
      <div className={`split-layout-left ${leftClassName}`.trim()}>{left}</div>
      <div className={`split-layout-right ${rightClassName}`.trim()}>{right}</div>
    </div>
  );
}
