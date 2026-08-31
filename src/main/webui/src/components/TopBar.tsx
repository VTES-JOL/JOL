import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { ChevronDown, LogOut, Menu, Moon, UserCircle } from 'lucide-react';
import { useNav } from '../auth/useNav';
import { pathForGame, pathForView, pathForHelp } from '../routes';
import { logout as logoutRequest } from '../pages/login/authApi';
import { CountryFlag } from './CountryFlag';

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
  // Hard redirect (not client-side navigation) is deliberate: the whole
  // authenticated shell needs a clean remount once logged out.
  logoutRequest().finally(() => {
    window.location.href = '/jol/login';
  });
}

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

const NAV_LINK = 'px-3 py-2 no-underline text-sm text-white/75 hover:text-white whitespace-nowrap';
const MENU_ITEM = 'flex items-center gap-2 px-3 py-1.5 text-sm text-ink hover:bg-hover w-full text-left';

export function TopBar() {
  const nav = useNav();
  const { open, setOpen, rootRef } = useDropdown();
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <nav
      ref={rootRef}
      id="navbar"
      className="relative z-40 flex items-center gap-1 bg-[#1c1e21] px-2 shrink-0"
    >
      <Link className="px-2 py-3 text-white font-serif text-lg no-underline" to="/jol/">
        V:TES Online
      </Link>

      <button
        type="button"
        aria-label="Toggle navigation"
        aria-expanded={mobileOpen}
        className="lg:hidden ml-1 p-2 rounded text-white/80 hover:bg-white/10"
        onClick={() => setMobileOpen((prev) => !prev)}
      >
        <Menu size={18} />
      </button>

      <div
        className={`lg:flex items-center ${
          mobileOpen
            ? 'absolute top-full left-0 right-0 flex flex-col items-start bg-[#1c1e21] py-2 shadow-xl'
            : 'hidden'
        }`}
      >
        <div className="relative">
          <button
            type="button"
            aria-expanded={open === 'games'}
            className={`${NAV_LINK} inline-flex items-center gap-1`}
            onClick={() => setOpen(open === 'games' ? null : 'games')}
          >
            My Games <ChevronDown size={13} />
          </button>
          {open === 'games' && (
            <ul className="absolute left-0 mt-1 min-w-40 list-none rounded border border-line bg-panel shadow-xl overflow-hidden z-50">
              {Object.entries(nav?.gameButtons ?? {}).map(([id, label]) => (
                <li key={id}>
                  <Link
                    className={MENU_ITEM}
                    to={pathForGame(id.slice(1))}
                    onClick={() => {
                      setOpen(null);
                      setMobileOpen(false);
                    }}
                  >
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>

        {(nav?.buttons ?? []).map((entry) => {
          const [view, label] = entry.split(':');
          return (
            <Link key={view} className={NAV_LINK} to={pathForView(view)} onClick={() => setMobileOpen(false)}>
              {label}
            </Link>
          );
        })}
        <Link className={NAV_LINK} to={pathForHelp()} onClick={() => setMobileOpen(false)}>
          Help
        </Link>
      </div>

      <div className="flex items-center gap-1 ml-auto">
        {nav?.player && (
          <div className="relative">
            <button
              type="button"
              aria-expanded={open === 'user'}
              className="flex items-center gap-2 rounded-full border border-white/15 px-2 py-1 text-sm text-white hover:bg-white/10"
              onClick={() => setOpen(open === 'user' ? null : 'user')}
            >
              {nav.country ? (
                <CountryFlag code={nav.country} className="rounded-sm" tooltip={false} />
              ) : (
                <UserCircle size={16} className="text-white/60" />
              )}
              <span>{nav.player}</span>
              <ChevronDown size={13} />
            </button>
            {open === 'user' && (
              <ul className="absolute right-0 mt-1 min-w-44 list-none rounded border border-line bg-panel shadow-xl overflow-hidden z-50">
                <li>
                  <Link className={MENU_ITEM} to={pathForView('profile')} onClick={() => setOpen(null)}>
                    <UserCircle size={14} /> Profile
                  </Link>
                </li>
                <li>
                  <hr className="border-line my-1" />
                </li>
                <li>
                  <button
                    type="button"
                    className={MENU_ITEM}
                    onClick={() => {
                      toggleDarkMode();
                      setOpen(null);
                    }}
                  >
                    <Moon size={14} /> Dark Mode
                  </button>
                </li>
                <li>
                  <hr className="border-line my-1" />
                </li>
                <li>
                  <button type="button" className={`${MENU_ITEM} text-blood`} onClick={logout}>
                    <LogOut size={14} /> Log Out
                  </button>
                </li>
              </ul>
            )}
          </div>
        )}
      </div>
    </nav>
  );
}
