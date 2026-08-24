import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import { loadLegacyStyles } from './legacyStyles';
import './index.css';
// App-authored global styles — fonts, tokens/typography, and the shared
// card-visuals vocabulary (icon/clan/path/card-name) — bundled by Vite like
// any other import, unlike loadLegacyStyles() below (which fetches
// third-party/theme stylesheets that must stay reachable as plain WAR-served
// files: bootstrap.min.css, dark-mode.css, light.css).
import './styles/fonts.css';
import './styles/theme.css';
import './styles/card-visuals.css';

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
