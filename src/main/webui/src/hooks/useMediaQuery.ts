import { useSyncExternalStore } from 'react';

/**
 * Subscribe to a CSS media query. `matchMedia` + a change listener via
 * useSyncExternalStore (same reason as useConnectivity — it's an external
 * store, not React state). SSR / envs without matchMedia get `false`.
 */
export function useMediaQuery(query: string): boolean {
  return useSyncExternalStore(
    (onChange) => {
      if (typeof window === 'undefined' || !window.matchMedia) return () => {};
      const mql = window.matchMedia(query);
      mql.addEventListener('change', onChange);
      return () => mql.removeEventListener('change', onChange);
    },
    () => (typeof window !== 'undefined' && window.matchMedia ? window.matchMedia(query).matches : false),
    () => false,
  );
}

// Tailwind's `md` breakpoint is 768px — "mobile" is everything below it.
export const useIsMobile = () => useMediaQuery('(max-width: 767.98px)');
