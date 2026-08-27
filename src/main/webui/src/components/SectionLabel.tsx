import type { ReactNode } from 'react';

// Small muted heading above a sub-section inside a card body (e.g. "Players",
// "Invite Player") — extracted from repeated inline divs in GameDetail.tsx.
export function SectionLabel({ children }: { children: ReactNode }) {
  return <div className="fw-semibold small text-muted mb-2">{children}</div>;
}
