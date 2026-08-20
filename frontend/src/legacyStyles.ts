// Loads the site's existing (non-Vite) stylesheets at runtime rather than via
// <link> tags in index.html — see the comment in index.html for why: neither
// a plain nor a %BASE_URL%-prefixed href survives both dev and build
// unchanged. import.meta.env.BASE_URL is a plain string Vite sets correctly
// in both modes, with no HTML attribute-rewriting involved.
const SHEETS = [
  'css/bootstrap.min.css',
  'css/styles.css',
  'css/dark-mode.css',
  'css/jquery-ui.min.css',
  'css/jquery-ui.structure.min.css',
  'css/jquery-ui.theme.min.css',
  'css/light.css',
];

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
          link.href = `${import.meta.env.BASE_URL}${path}`;
          link.onload = () => resolve();
          link.onerror = () => resolve();
          document.head.appendChild(link);
        }),
    ),
  ).then(() => undefined);
}
