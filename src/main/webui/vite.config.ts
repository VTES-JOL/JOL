import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import mdx from '@mdx-js/rollup'
import remarkGfm from 'remark-gfm'
import { serveCardAssets } from './serveCardAssets.js'

// One id per `vite build` invocation (config is re-evaluated fresh each
// time), shared between the bundle itself (`define` below) and the
// version.json a running client polls — see updateCheck.ts. Not meaningful
// in `vite dev` (the id would just be "now" for the whole dev session), but
// harmless there since updateCheck.ts skips polling entirely in dev.
const buildId = Date.now().toString(36)

// Quarkus + Quinoa (quarkus.quinoa.ui-dir=frontend, in application.properties)
// replaced the old Tomcat/Jersey/RewriteValve stack this file used to talk
// to directly. Three things that setup needed and Quinoa makes unnecessary:
//   - @vitejs/plugin-basic-ssl: Quinoa proxies this dev server over plain
//     HTTP internally: the browser only ever talks to Quarkus itself, which
//     already terminates HTTPS in dev (see application.properties's
//     %dev.quarkus.http.ssl-port block) — the Secure-cookie requirement that
//     motivated TLS here is satisfied there instead.
//   - The manual server.proxy/bypass + FRONTEND_ROUTES allowlist: this is
//     exactly what quarkus.quinoa.enable-spa-routing does.
//   - host: true / allowedHosts: the browser never connects to this dev
//     server directly at all anymore — only Quinoa's own internal Java-side
//     HTTP client does, over localhost, so the custom-hostname/DNS-rebinding
//     concerns that motivated those don't apply.
export default defineConfig(() => ({
  // Has to stay '/jol/' in both dev and build (no more command-dependent
  // '/jol/react/' — Quinoa serves built assets directly at the app's root,
  // no nested /react/ subpath the way the old WAR staging did): a
  // leading-slash src/href resolves against the browser's origin root, not
  // the current page, so this has to match quarkus.http.root-path or every
  // asset request 404s before Quinoa ever sees it — confirmed the hard way.
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
    // Tailwind v4 — see src/styles/tailwind.css for how it's scoped to
    // coexist with the legacy Bootstrap CSS (jt: prefix, no Preflight).
    tailwindcss(),
    serveCardAssets(),
    // Emits version.json into outDir root, next to index.html/assets/ — same
    // place the hashed JS/CSS land, so it's reachable through whatever
    // already serves those (Quinoa) without needing its own route.
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
}))
