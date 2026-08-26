// Loads the site's third-party/theme stylesheets at runtime rather than via
// <link> tags in index.html — see the comment in index.html for why: neither
// a plain nor a %BASE_URL%-prefixed href survives both dev and build
// unchanged.
//
// These are served from webapp/css, mapped at /jol/css/* (web.xml) —
// deliberately NOT import.meta.env.BASE_URL: that's '/jol/react/' in a
// production build (where the React bundle's own assets land), which would
// point here at '/jol/react/css/...' and 404. /jol/css/* sits at the
// context root in both dev (proxied straight through by vite.config.ts) and
// prod, so the prefix is hardcoded to match instead.
//
// App-authored CSS (fonts, tokens, card visuals, every component's own
// styles) is NOT here — it's plain Vite-bundled `import './Foo.css'`
// alongside its owning component (see main.tsx for the few truly global
// ones), which is both simpler and avoids the async-fetch race this
// mechanism has: a JS-appended <link> tag doesn't block rendering, so
// anything render-critical needs an inline-style belt-and-braces fallback
// (see e.g. GlobalChat.tsx) the way bundled CSS never does.
const SHEETS = ['/jol/css/bootstrap.min.css', '/jol/css/dark-mode.css', '/jol/css/light.css'];

/**
 * Returns once every stylesheet has actually loaded (or failed — one bad
 * stylesheet shouldn't block the app forever). main.tsx awaits this before
 * rendering: appending a <link> tag doesn't block on the fetch/parse, so
 * without this, components mount and lay out before rules they depend on
 * (id-selector overflow/min-height on scroll containers, tippy theme
 * overrides, ...) exist — a real, network-timing-dependent race, not just a
 * cosmetic flash-of-unstyled-content.
 */
export function loadLegacyStyles(): Promise<void> {
  return Promise.all(
    SHEETS.map(
      (path) =>
        new Promise<void>((resolve) => {
          const link = document.createElement('link');
          link.rel = 'stylesheet';
          link.type = 'text/css';
          link.href = path;
          link.onload = () => resolve();
          link.onerror = () => resolve();
          document.head.appendChild(link);
        }),
    ),
  ).then(() => undefined);
}
