import type { ReactNode } from 'react';
import './Callout.css';

// A single visually-distinct aside for a tip/note in the middle of prose —
// replaces the old JSP's habit of nesting a whole extra Bootstrap `card`
// inside the page for this (see the Visual design direction in the Help
// route plan: content gets its own purpose-built treatment, not a reused
// generic container).
export function Callout({ title = 'Tip', children }: { title?: string; children: ReactNode }) {
  return (
    <div className="help-callout my-3">
      <i className="bi bi-lightbulb-fill help-callout-icon" />
      <div>
        <div className="help-callout-title">{title}</div>
        <div className="small">{children}</div>
      </div>
    </div>
  );
}
