import { type ReactNode, useEffect, useState } from 'react';
import { ChevronDown } from 'lucide-react';

/**
 * Master/detail layout, ported from the jol-quarkus rewrite. Tailwind-based
 * (all classes `jt:` -prefixed — see styles/tailwind.css).
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
    md: 'jt:md:hidden',
    lg: 'jt:lg:hidden',
    xl: 'jt:xl:hidden',
  }[breakpoint];

  const gridColsClass = {
    md: 'jt:md:grid',
    lg: 'jt:lg:grid',
    xl: 'jt:xl:grid',
  }[breakpoint];

  const panelResponsiveClass = {
    md: 'jt:md:flex jt:md:flex-col jt:md:h-full jt:md:min-h-0 jt:md:w-full jt:md:overflow-y-auto',
    lg: 'jt:lg:flex jt:lg:flex-col jt:lg:h-full jt:lg:min-h-0 jt:lg:w-full jt:lg:overflow-y-auto',
    xl: 'jt:xl:flex jt:xl:flex-col jt:xl:h-full jt:xl:min-h-0 jt:xl:w-full jt:xl:overflow-y-auto',
  }[breakpoint];

  return (
    <div className="jt:flex jt:flex-col jt:flex-1 jt:min-h-0">
      {/* Mobile dropdown selector */}
      <div className={`${mobileNavHidden} jt:mb-4 jt:shrink-0 jt:relative jt:z-20`}>
        <button
          onClick={() => setMobileNavOpen(!mobileNavOpen)}
          className="jt:w-full jt:flex jt:items-center jt:justify-between jt:px-4 jt:py-3 jt:bg-panel jt:border jt:border-line jt:rounded-lg jt:text-sm jt:font-semibold jt:text-ink jt:shadow-sm"
        >
          <span className="jt:truncate">{selectedPanel.label}</span>
          <ChevronDown
            className={`jt:w-4 jt:h-4 jt:transition-transform ${mobileNavOpen ? 'jt:rotate-180' : ''}`}
          />
        </button>

        {mobileNavOpen && (
          <div className="jt:absolute jt:top-full jt:left-0 jt:right-0 jt:mt-2 jt:bg-panel jt:border jt:border-line jt:rounded-lg jt:shadow-xl jt:overflow-hidden jt:z-30">
            {panels.map((p) => (
              <button
                key={p.key}
                onClick={() => handleSelect(p.key)}
                className={`jt:w-full jt:text-left jt:px-4 jt:py-3 jt:text-sm jt:transition-colors jt:hover:bg-hover ${
                  p.key === selectedKey ? 'jt:text-accent-soft jt:font-bold' : 'jt:text-ink'
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
        className={`jt:flex-1 jt:min-h-0 jt:w-full jt:flex jt:flex-col ${gridColsClass} jt:gap-6`}
        style={{ gridTemplateColumns: columns }}
      >
        {panels.map((p) => (
          <div
            key={p.key}
            className={
              p.key === selectedKey
                ? 'jt:flex jt:flex-col jt:h-full jt:min-h-0 jt:w-full jt:overflow-y-auto'
                : `jt:hidden ${panelResponsiveClass}`
            }
          >
            {p.content}
          </div>
        ))}
      </div>
    </div>
  );
}
