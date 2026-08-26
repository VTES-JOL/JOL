import type { Plugin } from 'vite'
import { createReadStream, existsSync, statSync } from 'node:fs'
import { join, normalize } from 'node:path'

// This file lives at src/main/webui/ (Quinoa's default ui-dir) — three
// levels below the repo root, where the actual static/ directory is.
const STATIC_ROOT = normalize(new URL('../../../static', import.meta.url).pathname)

const CONTENT_TYPES: Record<string, string> = {
  images: 'image/jpeg',
  html: 'text/html; charset=utf-8',
  json: 'application/json',
  icons: 'image/svg+xml',
}

// Leading /jol matches quarkus.http.root-path (application.properties) and
// getBaseUrl()'s dev-mode return value (api/config.ts) — both requests
// arriving here via Quinoa's forwarding, and this plugin's own match
// against them, have to agree on that prefix. Vite's dev server passes the
// raw incoming request URL (including it) through to configureServer
// middlewares unchanged — it doesn't strip `base` before this runs.
const ASSET_PATH = /^\/jol\/(secured\/)?(images|html|json|icons)\/(.+)$/

/**
 * Dev-only: serves the locally-generated static/ directory (card images/
 * html/json/icons — see CLAUDE.md's CardDatabaseBuilder note; this
 * directory is gitignored, a developer needs it populated locally already,
 * same prerequisite the old nginx-based setup had) directly, at the same
 * relative paths getBaseUrl() requests them at in dev (see api/config.ts).
 *
 * Replaces relying on static.dev.deckserver.net actually resolving to
 * something reachable: that hostname only ever worked locally via
 * /etc/hosts pointing it at the docker nginx "static" service, which this
 * migration dropped in favor of Vite alone — nothing was left listening on
 * that host afterward. Prod is unaffected: it genuinely uses the real CDN,
 * this plugin never runs there (`apply: 'serve'` excludes it from `vite build`).
 */
export function serveCardAssets(): Plugin {
  return {
    name: 'serve-card-assets',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const url = (req.url ?? '').split('?')[0]
        const match = ASSET_PATH.exec(url)
        if (!match) return next()

        const [, secured, kind, rest] = match
        const filePath = normalize(join(STATIC_ROOT, secured ? 'secured' : '', kind, rest))
        // A real 404 here, not next() — matching ASSET_PATH already means
        // this is definitively a card-asset request, never a SPA route, so
        // falling through just let Quinoa's SPA-routing catch-all serve
        // index.html for it instead. That came back 200 text/html, which a
        // browser <img> silently accepts as "loaded" with a 0x0 natural
        // size — no failed-request signal, so onError-based fallback UI
        // (CardImage.tsx, useCardTooltips.ts) never fired and a missing
        // card asset just looked like a blank/broken image inconsistently,
        // depending on which cards happened to be present in the local
        // static/ mirror (see FINDINGS.md-style bug writeup this fixes).
        if (!filePath.startsWith(STATIC_ROOT) || !existsSync(filePath) || !statSync(filePath).isFile()) {
          res.statusCode = 404
          res.setHeader('Content-Type', 'text/plain; charset=utf-8')
          res.end('Not found')
          return
        }

        res.setHeader('Content-Type', CONTENT_TYPES[kind] ?? 'application/octet-stream')
        createReadStream(filePath).pipe(res)
      })
    },
  }
}
