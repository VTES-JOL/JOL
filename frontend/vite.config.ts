import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import basicSsl from '@vitejs/plugin-basic-ssl'
import { serveCardAssets } from './serveCardAssets.js'

const BACKEND = 'http://localhost:8080'

// Exact paths this frontend owns — must mirror MainServlet.REACT_ROUTES
// (Java) exactly. Everything else under /jol/ is proxied to the real
// backend (tomcat9:run) below, so unconverted JSP routes, static assets
// (css/js/images/fonts), and the REST API/WebSocket all keep working.
const FRONTEND_ROUTES = new Set(['/jol', '/jol/', '/jol/main', '/jol/main.jsp', '/jol/profile', '/jol/admin'])

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
  plugins: [react(), basicSsl(), serveCardAssets()],
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
