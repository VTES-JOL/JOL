import { api } from './client';

interface ConfigResponse {
  baseUrl: string;
  vapidPublicKey: string | null;
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
    // In dev, card assets are served by Vite itself at a plain relative path
    // (see serveCardAssets() in vite.config.ts, serving the local static/
    // directory) — no absolute cross-origin URL needed, and no dependency on
    // static.dev.deckserver.net resolving to anything actually reachable
    // (it only ever worked locally via /etc/hosts pointing at the docker
    // nginx "static" service, dropped when this app moved to Vite alone).
    // ConfigResource.baseUrl's real default is still correct for prod.
    return Promise.resolve('');
  }
  return getConfig().then((c) => c.baseUrl);
}

// Unlike baseUrl, this has no dev-mode override — VAPID_PUBLIC_KEY is a real
// env var the local tomcat9:run backend already needs (see CLAUDE.md), and
// /config is a normal proxied API call, not a static asset Vite fakes.
export function getVapidPublicKey(): Promise<string | null> {
  return getConfig().then((c) => c.vapidPublicKey);
}
