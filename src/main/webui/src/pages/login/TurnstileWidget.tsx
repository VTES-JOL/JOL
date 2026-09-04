import { useEffect, useRef } from 'react';
import { resolveDark, useThemePref } from '../../theme';

// Cloudflare Turnstile's implicit "scan the DOM for .cf-turnstile on script
// load" rendering doesn't work well for a conditionally-shown React panel
// (the register panel starts hidden, and the script may load before or
// after it's ever shown) — this uses the explicit render API instead, same
// widget legacy's login.jsp rendered implicitly via the cf-turnstile class.
interface Turnstile {
  render(container: HTMLElement, options: { sitekey: string; theme?: string; callback: (token: string) => void }): string;
  remove(widgetId: string): void;
  reset(widgetId: string): void;
}

declare global {
  interface Window {
    turnstile?: Turnstile;
  }
}

const SCRIPT_SRC = 'https://challenges.cloudflare.com/turnstile/v0/api.js';
let scriptLoad: Promise<void> | null = null;

function loadScript(): Promise<void> {
  if (window.turnstile) return Promise.resolve();
  if (!scriptLoad) {
    scriptLoad = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = SCRIPT_SRC;
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Failed to load Turnstile'));
      document.body.appendChild(script);
    });
  }
  return scriptLoad;
}

export function TurnstileWidget({ siteKey, onToken }: { siteKey: string; onToken: (token: string) => void }) {
  const containerRef = useRef<HTMLDivElement>(null);
  const dark = resolveDark(useThemePref());

  useEffect(() => {
    let widgetId: string | undefined;
    let cancelled = false;

    loadScript().then(() => {
      if (cancelled || !containerRef.current || !window.turnstile) return;
      // Match the app theme, not the OS ('auto' keys off prefers-color-scheme,
      // which the in-app toggle can override).
      widgetId = window.turnstile.render(containerRef.current, {
        sitekey: siteKey,
        theme: dark ? 'dark' : 'light',
        callback: onToken,
      });
    });

    return () => {
      cancelled = true;
      if (widgetId && window.turnstile) window.turnstile.remove(widgetId);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [siteKey, dark]);

  return <div ref={containerRef} />;
}
