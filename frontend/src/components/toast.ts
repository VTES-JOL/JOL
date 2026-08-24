import { useEffect, useState } from 'react';

// App-wide toast notifications for surfacing failures that previously only
// went to console.error — a user clicking "Save" against a flaky connection
// got no indication anything went wrong. Same module-singleton pattern as
// dialog.ts/connectivity.ts (callable from plain event handlers/.catch()
// with no component tree position of their own), but a list rather than a
// single pending item since more than one can be relevant at once.
export interface Toast {
  id: number;
  message: string;
  kind: 'error' | 'success';
}

let toasts: Toast[] = [];
let nextId = 1;
const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((listener) => listener());
}

const AUTO_DISMISS_MS = 6000;

function show(message: string, kind: Toast['kind']) {
  const id = nextId++;
  toasts = [...toasts, { id, message, kind }];
  notify();
  setTimeout(() => dismissToast(id), AUTO_DISMISS_MS);
}

/** Surfaces a failure to the user instead of only logging it — pair with (not replace) console.error. */
export function showError(message: string) {
  show(message, 'error');
}

export function showSuccess(message: string) {
  show(message, 'success');
}

export function dismissToast(id: number) {
  if (!toasts.some((t) => t.id === id)) return;
  toasts = toasts.filter((t) => t.id !== id);
  notify();
}

export function useToasts(): Toast[] {
  const [, setTick] = useState(0);
  useEffect(() => {
    const listener = () => setTick((t) => t + 1);
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  }, []);
  return toasts;
}
