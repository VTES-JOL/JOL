import type { ReactNode } from 'react';

export interface TabDef<Id extends string> {
  id: Id;
  label: ReactNode;
  /** Rendered as a pill badge after the label when not null/undefined. */
  badge?: ReactNode;
}

interface TabBarProps<Id extends string> {
  tabs: readonly TabDef<Id>[];
  active: Id;
  onChange: (id: Id) => void;
  /** Extra classes on the `<ul.nav.nav-tabs>` (e.g. `card-header-tabs`, `bg-secondary-subtle`). */
  className?: string;
  /** Extra classes on every tab `<button.nav-link>` (e.g. `px-3 py-2`). */
  tabClassName?: string;
}

// The Bootstrap `nav nav-tabs` markup every tabbed view was hand-rolling
// (WatchPage, watch/StatsTab, stats/PersonalStats, main/GamesPanel) — same
// `<ul><li.nav-item><button.nav-link>` shell, same active toggle, same
// optional count badge. Bootstrap's JS isn't loaded here, so the button
// `onClick` drives everything; callers own the panel switching.
export function TabBar<Id extends string>({ tabs, active, onChange, className, tabClassName }: TabBarProps<Id>) {
  return (
    <ul className={`nav nav-tabs${className ? ` ${className}` : ''}`} role="tablist">
      {tabs.map((tab) => (
        <li key={tab.id} className="nav-item" role="presentation">
          <button
            type="button"
            role="tab"
            aria-selected={active === tab.id}
            className={`nav-link${tabClassName ? ` ${tabClassName}` : ''}${active === tab.id ? ' active' : ''}`}
            onClick={() => onChange(tab.id)}
          >
            {tab.label}
            {tab.badge != null && <span className="badge rounded-pill bg-secondary ms-1">{tab.badge}</span>}
          </button>
        </li>
      ))}
    </ul>
  );
}
