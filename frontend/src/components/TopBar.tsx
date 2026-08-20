import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { useNav } from '../nav/useNav';
import { isConverted, pathForGame, pathForView } from '../routes';

function toggleDarkMode() {
  const isDark = document.body.getAttribute('data-bs-theme') !== 'dark';
  if (isDark) {
    document.body.setAttribute('data-bs-theme', 'dark');
  } else {
    document.body.removeAttribute('data-bs-theme');
  }
  localStorage.setItem('jol-theme', isDark ? 'dark' : '');
}

function logout() {
  const form = document.createElement('form');
  form.method = 'post';
  form.action = '/jol/logout';
  document.body.appendChild(form);
  form.submit();
}

// Bootstrap's JS bundle (which normally drives data-bs-toggle="dropdown")
// isn't loaded in this app — only its CSS is (legacyStyles.ts) — so these
// two dropdowns need their own open/close state instead.
type DropdownId = 'games' | 'user' | null;

function useDropdown() {
  const [open, setOpen] = useState<DropdownId>(null);
  const rootRef = useRef<HTMLElement>(null);

  useEffect(() => {
    if (!open) return;
    const closeOnOutsideClick = (e: MouseEvent) => {
      if (rootRef.current && !rootRef.current.contains(e.target as Node)) setOpen(null);
    };
    const closeOnEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(null);
    };
    document.addEventListener('mousedown', closeOnOutsideClick);
    document.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('mousedown', closeOnOutsideClick);
      document.removeEventListener('keydown', closeOnEscape);
    };
  }, [open]);

  return { open, setOpen, rootRef };
}

export function TopBar() {
  const nav = useNav();
  const { open, setOpen, rootRef } = useDropdown();

  return (
    <nav ref={rootRef} className="navbar navbar-expand-lg bg-dark px-2" id="navbar" data-bs-theme="dark">
      <Link className="navbar-brand" to="/jol/">
        <span id="titleLink">V:TES Online</span>
      </Link>
      <button
        className="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#navbarNavAltMarkup"
        aria-controls="navbarNavAltMarkup"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span className="navbar-toggler-icon"></span>
      </button>
      <div className="collapse navbar-collapse navbar-nav" id="navbarNavAltMarkup">
        <div className="navbar-nav">
          <div className={`nav-item dropdown ${open === 'games' ? 'show' : ''}`}>
            <a
              className="nav-link dropdown-toggle"
              href="#"
              id="myGamesLink"
              role="button"
              aria-expanded={open === 'games'}
              onClick={(e) => {
                e.preventDefault();
                setOpen(open === 'games' ? null : 'games');
              }}
            >
              My Games
            </a>
            {/* Same missing-[data-bs-popper] positioning gap as the user menu below — see its comment. */}
            <ul
              className={`dropdown-menu ${open === 'games' ? 'show' : ''}`}
              style={{ left: 0, right: 'auto' }}
              aria-labelledby="myGamesLink"
            >
              {Object.entries(nav?.gameButtons ?? {}).map(([id, label]) =>
                isConverted('game') ? (
                  <li key={id}>
                    <Link className="dropdown-item" to={pathForGame(id.slice(1))}>
                      {label}
                    </Link>
                  </li>
                ) : (
                  <li key={id}>
                    <a className="dropdown-item" href={`/jol/game/${id.slice(1)}`}>
                      {label}
                    </a>
                  </li>
                ),
              )}
            </ul>
          </div>
        </div>
        <div className="navbar-nav">
          {(nav?.buttons ?? []).map((entry) => {
            const [view, label] = entry.split(':');
            return isConverted(view) ? (
              <Link key={view} className="nav-link" to={pathForView(view)}>
                {label}
              </Link>
            ) : (
              <a key={view} className="nav-link" href={pathForView(view)}>
                {label}
              </a>
            );
          })}
        </div>
      </div>
      <div className="d-flex align-items-center gap-1 ms-auto">
        {nav?.player && (
          <div className={`nav-item dropdown ${open === 'user' ? 'show' : ''}`}>
            <a
              className="nav-link dropdown-toggle d-flex align-items-center gap-2 py-1 px-2 user-menu-toggle text-white"
              href="#"
              role="button"
              aria-expanded={open === 'user'}
              onClick={(e) => {
                e.preventDefault();
                setOpen(open === 'user' ? null : 'user');
              }}
            >
              <span>
                {nav.country ? (
                  <span className={`fi fi-${nav.country.toLowerCase()} fis rounded-1`} />
                ) : (
                  <i className="bi bi-person-circle text-secondary" />
                )}
              </span>
              <span>{nav.player}</span>
            </a>
            {/*
              .dropdown-menu-end only works via a [data-bs-popper] attribute
              Bootstrap's JS (unloaded here) normally sets — see bootstrap.min.css:
              every .dropdown-menu[data-bs-popper] positioning rule is gated
              behind it, so without it the menu falls back to unpositioned
              position:absolute (wherever it naturally flows), which overflows
              off the right edge for a menu anchored this close to it. Setting
              right/left explicitly bypasses that requirement entirely.
            */}
            <ul
              className={`dropdown-menu dropdown-menu-end shadow ${open === 'user' ? 'show' : ''}`}
              style={{ right: 0, left: 'auto' }}
            >
              <li>
                {isConverted('profile') ? (
                  <Link className="dropdown-item" to={pathForView('profile')}>
                    <i className="bi bi-person-circle me-2"></i>Profile
                  </Link>
                ) : (
                  <a className="dropdown-item" href={pathForView('profile')}>
                    <i className="bi bi-person-circle me-2"></i>Profile
                  </a>
                )}
              </li>
              <li>
                <hr className="dropdown-divider" />
              </li>
              <li>
                <a className="dropdown-item" href="/jol/help" target="_blank" rel="noreferrer">
                  <i className="bi bi-question-circle me-2"></i>Help
                </a>
              </li>
              <li>
                <a
                  className="dropdown-item"
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    toggleDarkMode();
                    setOpen(null);
                  }}
                >
                  <i className="bi bi-moon me-2"></i>Dark Mode
                </a>
              </li>
              <li>
                <hr className="dropdown-divider" />
              </li>
              <li>
                <a
                  className="dropdown-item text-danger-emphasis"
                  href="#"
                  onClick={(e) => {
                    e.preventDefault();
                    logout();
                  }}
                >
                  <i className="bi bi-box-arrow-right me-2"></i>Log Out
                </a>
              </li>
            </ul>
          </div>
        )}
      </div>
    </nav>
  );
}
