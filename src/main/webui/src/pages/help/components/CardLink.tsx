import type { ReactNode } from 'react';

// An inline card-name mention in prose (e.g. "burn Sébastien Goulet"), with
// the same hover-preview tooltip as chat/deck card links — HelpSection wires
// up useCardTooltips on the whole rendered section, so any of these just
// need the same markup those places use.
export function CardLink({ id, children }: { id: string; children: ReactNode }) {
  return (
    <a className="card-name" data-card-id={id}>
      {children}
    </a>
  );
}
