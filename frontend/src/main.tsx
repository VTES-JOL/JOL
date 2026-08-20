import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import { loadLegacyStyles } from './legacyStyles';
import './index.css';

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
