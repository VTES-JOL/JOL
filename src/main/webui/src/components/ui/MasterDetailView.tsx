import { type ReactNode, useEffect, useState } from 'react';
import { ChevronDown } from 'lucide-react';

/**
 * Master/detail layout, ported from the jol-quarkus rewrite. Tailwind-based
 * (all classes Tailwind-based — see styles/tailwind.css).
 *
 * Desktop (>= breakpoint): every panel shown side-by-side in a CSS grid
 * whose track sizes come from `columns`.
 * Mobile (< breakpoint): the panel list collapses into a dropdown selector;
 * only the selected panel renders.
 *
 * This is the Tailwind replacement for the Bootstrap-era SplitLayout — new
 * pages should use this; SplitLayout stays until its callers are migrated.
 */
export interface PanelConfig {
  key: string;
  label: string;
  content: ReactNode;
}

interface MasterDetailViewProps {
  panels: [PanelConfig, PanelConfig, ...PanelConfig[]]; // at least two
  columns?: string; // grid-template-columns, e.g. "300px 1fr" or "280px 1fr 280px"
  breakpoint?: 'md' | 'lg' | 'xl';
  /**
   * Which panel is shown in the collapsed (mobile) view. Uncontrolled by
   * default — `activeKey` only nudges the initial/changed value. Pass
   * `onActiveKeyChange` too to make it fully controlled, so the parent can
   * force-focus a panel (e.g. jump to the detail pane when a list row is
   * picked) even when the key value itself hasn't changed.
   */
  activeKey?: string;
  onActiveKeyChange?: (key: string) => void;
}

export function MasterDetailView({
  panels,
  columns = '1fr 3fr',
  breakpoint = 'md',
  activeKey,
  onActiveKeyChange,
}: MasterDetailViewProps) {
  const controlled = onActiveKeyChange != null;
  const [internalKey, setInternalKey] = useState(activeKey ?? panels[0].key);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  // Uncontrolled: one-way sync from `activeKey` when its value changes.
  useEffect(() => {
    if (!controlled && activeKey && activeKey !== internalKey) {
      setInternalKey(activeKey);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeKey, controlled]);

  const selectedKey = controlled ? (activeKey ?? panels[0].key) : internalKey;
  const selectedPanel = panels.find((p) => p.key === selectedKey) || panels[0];

  const handleSelect = (key: string) => {
    setMobileNavOpen(false);
    if (controlled) onActiveKeyChange!(key);
    else setInternalKey(key);
  };

  const mobileNavHidden = {
    md: 'md:hidden',
    lg: 'lg:hidden',
    xl: 'xl:hidden',
  }[breakpoint];

  const gridColsClass = {
    md: 'md:grid',
    lg: 'lg:grid',
    xl: 'xl:grid',
  }[breakpoint];

  const panelResponsiveClass = {
    md: 'md:flex md:flex-col md:h-full md:min-h-0 md:w-full md:overflow-y-auto',
    lg: 'lg:flex lg:flex-col lg:h-full lg:min-h-0 lg:w-full lg:overflow-y-auto',
    xl: 'xl:flex xl:flex-col xl:h-full xl:min-h-0 xl:w-full xl:overflow-y-auto',
  }[breakpoint];

  return (
    <div className="flex flex-col flex-1 min-h-0">
      {/* Mobile dropdown selector */}
      <div className={`${mobileNavHidden} mb-4 shrink-0 relative z-20`}>
        <button
          onClick={() => setMobileNavOpen(!mobileNavOpen)}
          className="w-full flex items-center justify-between px-4 py-3 bg-panel border border-line rounded-lg text-sm font-semibold text-ink shadow-sm"
        >
          <span className="truncate">{selectedPanel.label}</span>
          <ChevronDown
            className={`w-4 h-4 transition-transform ${mobileNavOpen ? 'rotate-180' : ''}`}
          />
        </button>

        {mobileNavOpen && (
          <div className="absolute top-full left-0 right-0 mt-2 bg-panel border border-line rounded-lg shadow-xl overflow-hidden z-30">
            {panels.map((p) => (
              <button
                key={p.key}
                onClick={() => handleSelect(p.key)}
                className={`w-full text-left px-4 py-3 text-sm transition-colors hover:bg-hover ${
                  p.key === selectedKey ? 'text-accent-soft font-bold' : 'text-ink'
                }`}
              >
                {p.label}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Content area */}
      <div
        className={`flex-1 min-h-0 w-full flex flex-col ${gridColsClass} gap-6`}
        style={{ gridTemplateColumns: columns }}
      >
        {panels.map((p) => (
          <div
            key={p.key}
            className={
              p.key === selectedKey
                ? 'flex flex-col h-full min-h-0 w-full overflow-y-auto'
                : `hidden ${panelResponsiveClass}`
            }
          >
            {p.content}
          </div>
        ))}
      </div>
    </div>
  );
}
