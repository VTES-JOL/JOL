import { useEffect, useState } from 'react';

// A single, app-wide confirm/alert dialog replacing native confirm()/alert()
// — those are jarring OS-chrome dialogs that break the app's visual identity
// (see DialogHost.tsx, mounted once in App.tsx). Modeled on connectivity.ts's
// module-level-state-plus-listeners pattern rather than React context, since
// these need to be callable from plain event handlers with no component
// tree position of their own.
interface DialogRequest {
  message: string;
  title?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  danger?: boolean;
  mode: 'confirm' | 'alert';
  resolve: (value: boolean) => void;
}

let current: DialogRequest | null = null;
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((listener) => listener());
}

export interface ConfirmOptions {
  title?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  danger?: boolean;
}

/** Replaces window.confirm(message) — resolves true/false instead of blocking. */
export function confirmDialog(message: string, options?: ConfirmOptions): Promise<boolean> {
  return new Promise((resolve) => {
    current = { message, mode: 'confirm', resolve, ...options };
    notify();
  });
}

/** Replaces window.alert(message) — resolves once dismissed. */
export function alertDialog(message: string, options?: { title?: string }): Promise<void> {
  return new Promise((resolve) => {
    current = { message, mode: 'alert', resolve: () => resolve(), ...options };
    notify();
  });
}

export function useDialogRequest(): DialogRequest | null {
  const [, setTick] = useState(0);
  useEffect(() => {
    const listener = () => setTick((t) => t + 1);
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  }, []);
  return current;
}

export function resolveDialog(value: boolean) {
  if (!current) return;
  const request = current;
  current = null;
  notify();
  request.resolve(value);
}
