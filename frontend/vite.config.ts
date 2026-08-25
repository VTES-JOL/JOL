import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'
import mdx from '@mdx-js/rollup'
import remarkGfm from 'remark-gfm'
import { serveCardAssets } from './serveCardAssets.js'

const BACKEND = 'http://localhost:8080'

// One id per `vite build` invocation (config is re-evaluated fresh each
// time), shared between the bundle itself (`define` below) and the
// version.json a running client polls — see updateCheck.ts. Not meaningful
// in `vite dev` (the id would just be "now" for the whole dev session), but
// harmless there since updateCheck.ts skips polling entirely in dev.
const buildId = Date.now().toString(36)

// Exact paths this frontend owns — must mirror MainServlet's @WebServlet
// mapping (including "/login") exactly. Everything else under
// /jol/ is proxied to the real backend (tomcat9:run) below: static assets
// (css/js/images/fonts) and the REST API/WebSocket all keep working through
// that proxy. register/logout have no GET page of their own anymore — both
// are REST calls (AuthResource) the login page itself makes.
const FRONTEND_ROUTES = new Set(['/jol', '/jol/', '/jol/main', '/jol/main.jsp', '/jol/profile', '/jol/admin', '/jol/tournamentAdmin', '/jol/tournament', '/jol/active', '/jol/lobby', '/jol/deck', '/jol/login', '/jol/help'])

// Served in prod from inside the WAR at /jol/react/*, forwarded there by
// MainServlet for converted routes. In dev, Vite terminates TLS itself and
// proxies everything it doesn't own straight to tomcat9:run — no nginx/
// Docker layer needed for frontend dev. AuthService's cookies are
// unconditionally `Secure`, which is why this needs to be HTTPS at all, even
// for local dev.
//
// TLS comes from @vitejs/plugin-basic-ssl, not a real cert: it generates a
// self-signed one automatically (cached under node_modules/.vite-plugin-
// basic-ssl/) — no cert files to obtain, generate, or gitignore, works
// identically on every machine with just `npm install && npm run dev`. Your
// browser will show a one-time "connection isn't private" warning per
// profile the first time — that's expected (self-signed, not a trusted CA)
// and safe to click through for local dev.
//
// This deliberately does NOT use nginx/certs/ — that's a real AWS-issued
// certificate for the team's actual dev.deckserver.net, not something every
// developer can or should have a copy of locally.
export default defineConfig({
  base: '/jol/',
  // mdx() must run before react() — it compiles content/help/*.mdx into
  // plain JSX-emitting JS (via the automatic jsx-runtime), which react()'s
  // babel transform then needs to see already in place. remark-gfm enables
  // GitHub-flavored markdown extensions — tables in particular, used by
  // several help sections — which plain CommonMark (MDX's default) doesn't
  // parse; without it a `| a | b |` block renders as a literal paragraph of
  // pipe characters instead of a <table>.
  plugins: [
    mdx({ remarkPlugins: [remarkGfm] }),
    react(),
    basicSsl(),
    serveCardAssets(),
    // Emits version.json into outDir root, next to index.html/assets/ — same
    // place the hashed JS/CSS land, so it's reachable through whatever
    // already serves those in prod (see MainServlet/web.xml's /react/*
    // static mapping) without needing its own route.
    {
      name: 'emit-version-json',
      apply: 'build',
      generateBundle() {
        this.emitFile({
          type: 'asset',
          fileName: 'version.json',
          source: JSON.stringify({ buildId }),
        })
      },
    },
  ],
  define: {
    __BUILD_ID__: JSON.stringify(buildId),
  },
  server: {
    // Deliberately not port 443: binding it needs root (or an OS-specific
    // privileged-port workaround — pf redirect on macOS, setcap/authbind on
    // Linux, something else again on Windows), which isn't a "just run npm
    // run dev" experience for every developer. HTTPS is what the Secure
    // cookie actually needs, not port 443 specifically — Vite's default port
    // works identically with zero privilege escalation on any OS.
    //
    // Without this Vite binds only ::1 (IPv6 loopback) — unreachable if your
    // /etc/hosts entry points the custom hostname at 127.0.0.1 (IPv4).
    host: true,
    // Vite's DNS-rebinding protection rejects requests whose Host header isn't
    // localhost/127.0.0.1 unless listed here — add your /etc/hosts entry.
    allowedHosts: ['dev.deckserver.net', 'localhost'],
    proxy: {
      '/jol/ws': {
        target: BACKEND.replace('http', 'ws'),
        ws: true,
      },
      '/jol': {
        target: BACKEND,
        bypass(req) {
          const path = (req.url ?? '').split('?')[0]
          // Let Vite handle its own dev module graph/HMR client, and any
          // route this frontend owns, locally instead of proxying it.
          if (path.startsWith('/jol/@') || path.startsWith('/jol/src/') || path.startsWith('/jol/node_modules/')) {
            return path
          }
          // /jol/game/<id> and /jol/help/<section> — both have a dynamic
          // trailing segment, so they can't live in the static FRONTEND_ROUTES
          // set above (mirrors MainServlet's "/game/*"/"/help/*" wildcard
          // mappings treating any such path as React-owned).
          if (path.startsWith('/jol/game/')) return path
          if (path.startsWith('/jol/help/')) return path
          if (FRONTEND_ROUTES.has(path)) return path
          return undefined // fall through to the proxy target above
        },
      },
    },
  },
  build: {
    outDir: '../target/react-dist',
    emptyOutDir: true,
  },
})
