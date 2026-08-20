import { useEffect, useState, type ReactNode } from 'react';
import { api } from '../api/client';
import type { NavBean } from '../api/types';
import { useJolSocket } from '../ws/useJolSocket';
import { NavContext, NavRefreshContext } from './useNav';

/**
 * Single shared /nav fetch for the whole app — TopBar needs the full bean
 * (gameButtons, buttons, ...) and GlobalChat needs just .player for mention
 * detection. Without this each would fetch /nav independently.
 */
export function NavProvider({ children }: { children: ReactNode }) {
  const [nav, setNav] = useState<NavBean | null>(null);

  const refresh = () => {
    api.get<NavBean>('/nav').then(setNav).catch((err) => console.error('Failed to load /nav', err));
  };

  useEffect(refresh, []);
  // Only "games" affects anything currently rendered from this (gameButtons).
  useJolSocket('main:games', refresh);

  return (
    <NavContext.Provider value={nav}>
      <NavRefreshContext.Provider value={refresh}>{children}</NavRefreshContext.Provider>
    </NavContext.Provider>
  );
}
