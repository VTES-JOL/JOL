import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import { loadLegacyStyles } from './legacyStyles';
import './index.css';
// App-authored global styles — fonts, tokens/typography, and the shared
// card-visuals vocabulary that has to stay global (icon/card-name — see
// card-visuals.css's own header comment for why; clan/path have since moved
// to their owning components, pages/game/Clan.css and Path.css) — bundled
// by Vite like any other import, unlike loadLegacyStyles() below (which fetches
// third-party/theme stylesheets that must stay reachable as plain WAR-served
// files: bootstrap.min.css, dark-mode.css, light.css).
import './styles/fonts.css';
import './styles/theme.css';
import './styles/card-visuals.css';
// Tailwind v4 utilities (jt: -prefixed) — coexists with the Bootstrap CSS
// loadLegacyStyles() fetches below; see styles/tailwind.css for the rules.
import './styles/tailwind.css';

// Wait for the site's own stylesheets to actually load before rendering —
// see loadLegacyStyles' comment for why appending a <link> tag isn't enough
// on its own.
loadLegacyStyles().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
      <App />
    </StrictMode>,
  );
});
