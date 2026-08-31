import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { ChevronDown } from 'lucide-react';
import { HELP_SECTIONS } from '../content/help/meta';
import { pathForHelp } from '../routes';
import './HelpPage.css';

// Docs-style shell for the Help route: a section sidebar on desktop that
// collapses into a toggleable accordion on mobile, with the active section
// rendered by HelpSection via <Outlet/>.
export function HelpPage() {
  const [navOpen, setNavOpen] = useState(false);

  return (
    <div className="jt-scope help-layout">
      <aside className="help-sidebar">
        <button
          className="jt:lg:hidden jt:w-full jt:flex jt:items-center jt:justify-between jt:px-3 jt:py-2 jt:rounded jt:border jt:border-line-accent jt:text-ink-secondary jt:hover:bg-hover"
          onClick={() => setNavOpen((open) => !open)}
          aria-expanded={navOpen}
          aria-controls="helpNav"
        >
          Help Sections
          <ChevronDown size={16} className={`jt:transition-transform ${navOpen ? 'jt:rotate-180' : ''}`} />
        </button>
        <nav
          id="helpNav"
          className={`help-nav jt:flex-col ${navOpen ? 'jt:flex' : 'jt:hidden'} jt:lg:flex`}
        >
          {HELP_SECTIONS.map((section) => (
            <NavLink
              key={section.slug}
              to={pathForHelp(section.slug)}
              className={({ isActive }) => `help-nav-link${isActive ? ' active' : ''}`}
              onClick={() => setNavOpen(false)}
            >
              <span className="help-nav-title">{section.title}</span>
              <span className="help-nav-summary">{section.summary}</span>
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="help-content">
        <Outlet />
      </div>
    </div>
  );
}
