import type { Plugin } from 'vite'
import { createReadStream, existsSync, statSync } from 'node:fs'
import { join, normalize } from 'node:path'

const STATIC_ROOT = normalize(new URL('../static', import.meta.url).pathname)

const CONTENT_TYPES: Record<string, string> = {
  images: 'image/jpeg',
  html: 'text/html; charset=utf-8',
  json: 'application/json',
  icons: 'image/svg+xml',
}

const ASSET_PATH = /^\/(secured\/)?(images|html|json|icons)\/(.+)$/

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
        if (!filePath.startsWith(STATIC_ROOT) || !existsSync(filePath) || !statSync(filePath).isFile()) {
          return next()
        }

        res.setHeader('Content-Type', CONTENT_TYPES[kind] ?? 'application/octet-stream')
        createReadStream(filePath).pipe(res)
      })
    },
  }
}
