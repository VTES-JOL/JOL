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
    <div className="help-layout">
      <aside className="help-sidebar">
        <button
          className="lg:hidden w-full flex items-center justify-between px-3 py-2 rounded border border-line-accent text-ink-secondary hover:bg-hover"
          onClick={() => setNavOpen((open) => !open)}
          aria-expanded={navOpen}
          aria-controls="helpNav"
        >
          Help Sections
          <ChevronDown size={16} className={`transition-transform ${navOpen ? 'rotate-180' : ''}`} />
        </button>
        <nav
          id="helpNav"
          className={`help-nav flex-col ${navOpen ? 'flex' : 'hidden'} lg:flex`}
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
