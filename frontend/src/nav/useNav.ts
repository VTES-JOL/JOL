import { createContext, useContext } from 'react';
import type { NavBean } from '../api/types';

export const NavContext = createContext<NavBean | null>(null);
export const NavRefreshContext = createContext<() => void>(() => {});

export function useNav(): NavBean | null {
  return useContext(NavContext);
}

// For pages that mutate something /nav reflects (e.g. profile's country,
// which the TopBar shows as a flag) and want it to update immediately
// rather than waiting for the next 'main:games' WS-triggered refresh.
export function useNavRefresh(): () => void {
  return useContext(NavRefreshContext);
}
