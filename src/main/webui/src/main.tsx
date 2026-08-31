import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import './index.css';
// App-authored global styles — fonts, tokens/typography, the shared
// card-visuals vocabulary (icon/card-name — see card-visuals.css's header for
// why it stays global), and the Tailwind layer (Preflight reset + utilities).
import './styles/fonts.css';
import './styles/theme.css';
import './styles/card-visuals.css';
import './styles/tailwind.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
