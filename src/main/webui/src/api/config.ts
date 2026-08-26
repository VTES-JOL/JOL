import { api } from './client';

interface ConfigResponse {
  baseUrl: string;
  vapidPublicKey: string | null;
  captchaEnabled: boolean;
  captchaSiteKey: string | null;
}

// GET /jol/api/config only ever needs to happen once per page load — cache
// the in-flight/resolved promise so every caller (e.g. every card tooltip)
// shares the same request instead of firing one each.
let cached: Promise<ConfigResponse> | null = null;

function getConfig(): Promise<ConfigResponse> {
  if (!cached) {
    cached = api.get<ConfigResponse>('/config');
  }
  return cached;
}

export function getBaseUrl(): Promise<string> {
  if (import.meta.env.DEV) {
    // In dev, card assets are served by Vite itself (see serveCardAssets()
    // in vite.config.ts, serving the local static/ directory) — no absolute
    // cross-origin URL needed, and no dependency on static.dev.deckserver.net
    // resolving to anything actually reachable (it only ever worked locally
    // via /etc/hosts pointing at the docker nginx "static" service, dropped
    // when this app moved to Vite alone). ConfigResource.baseUrl's real
    // default is still correct for prod.
    //
    // Must be '/jol', not '': quarkus.http.root-path=/jol is a real,
    // comprehensive context path under Quarkus — nothing outside /jol/* is
    // routed anywhere at all, so a bare relative request (e.g. /images/123)
    // 404s before it ever reaches Quinoa's forwarding to Vite, let alone
    // serveCardAssets.ts's middleware. (This is unlike the pre-Quarkus
    // Tomcat+Vite-proxy dev setup, where Vite's own server was the browser's
    // actual origin and a bare relative path just worked — a real behavior
    // change from that migration, not a preexisting quirk.) API_BASE
    // ('/jol/api') already follows this same rule; this just brings
    // getBaseUrl() in line with it.
    return Promise.resolve('/jol');
  }
  return getConfig().then((c) => c.baseUrl);
}

// Unlike baseUrl, this has no dev-mode override — VAPID_PUBLIC_KEY is a real
// env var the local quarkus:dev backend already needs (see CLAUDE.md), and
// /config is a normal REST call, not a static asset Vite fakes.
export function getVapidPublicKey(): Promise<string | null> {
  return getConfig().then((c) => c.vapidPublicKey);
}

// ENABLE_CAPTCHA=false in local dev (see CLAUDE.md) means the login page
// skips loading Turnstile entirely, matching the server skipping
// verification for the same env var (AuthResource.register).
export function getCaptchaConfig(): Promise<{ enabled: boolean; siteKey: string | null }> {
  return getConfig().then((c) => ({ enabled: c.captchaEnabled, siteKey: c.captchaSiteKey }));
}
