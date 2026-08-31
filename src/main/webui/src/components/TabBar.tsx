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
  /** Extra classes on the tab strip `<ul>` (e.g. `mt-3`). */
  className?: string;
}

// Underlined tab strip — a token-styled counterpart of Bootstrap
// `nav nav-tabs`. Callers own the panel switching.
export function TabBar<Id extends string>({ tabs, active, onChange, className }: TabBarProps<Id>) {
  return (
    <ul className={`flex border-b border-line${className ? ` ${className}` : ''}`} role="tablist">
      {tabs.map((tab) => (
        <li key={tab.id} role="presentation">
          <button
            type="button"
            role="tab"
            aria-selected={active === tab.id}
            className={`px-3 py-2 text-sm border-b-2 -mb-px transition-colors ${
              active === tab.id
                ? 'border-accent text-ink font-semibold'
                : 'border-transparent text-ink-muted hover:text-ink'
            }`}
            onClick={() => onChange(tab.id)}
          >
            {tab.label}
            {tab.badge != null && (
              <span className="ml-1 inline-flex items-center rounded-full bg-hover text-ink-muted px-1.5 text-xs">
                {tab.badge}
              </span>
            )}
          </button>
        </li>
      ))}
    </ul>
  );
}
