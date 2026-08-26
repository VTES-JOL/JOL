import { useEffect, useRef, useState } from 'react';

/**
 * Single-toggle dropdown open/close state, closing on outside click or
 * Escape — Bootstrap's JS bundle (which normally drives
 * data-bs-toggle="dropdown") isn't loaded in this app, only its CSS is (see
 * legacyStyles.ts). TopBar has its own inline version of this same pattern
 * scoped to two id-keyed dropdowns sharing one navbar-wide ref; this is the
 * plain single-dropdown case for everywhere else.
 */
export function useSimpleDropdown<T extends HTMLElement = HTMLElement>() {
  const [open, setOpen] = useState(false);
  const rootRef = useRef<T>(null);

  useEffect(() => {
    if (!open) return;
    const closeOnOutsideClick = (e: MouseEvent) => {
      if (rootRef.current && !rootRef.current.contains(e.target as Node)) setOpen(false);
    };
    const closeOnEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false);
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
