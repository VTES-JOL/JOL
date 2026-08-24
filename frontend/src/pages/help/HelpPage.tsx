import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { HELP_SECTIONS } from '../../content/help/meta';
import { pathForHelp } from '../../routes';
import './HelpPage.css';

// Docs-style shell for the Help route: a section sidebar on desktop that
// collapses into a toggleable accordion on mobile (see the Help route
// plan's visual design direction), with the active section rendered by
// HelpSection via <Outlet/>.
export function HelpPage() {
  const [navOpen, setNavOpen] = useState(false);

  return (
    <div className="help-layout">
      <aside className="help-sidebar">
        <button
          className="btn btn-outline-secondary w-100 d-flex justify-content-between align-items-center d-lg-none"
          onClick={() => setNavOpen((open) => !open)}
          aria-expanded={navOpen}
          aria-controls="helpNav"
        >
          Help Sections
          <i className={`bi ${navOpen ? 'bi-chevron-up' : 'bi-chevron-down'}`} />
        </button>
        <nav id="helpNav" className={`help-nav flex-column ${navOpen ? 'd-flex' : 'd-none'} d-lg-flex`}>
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
