import { useLocation } from 'react-router-dom';
import { backgroundForPath } from './routeBackgrounds';
import './RouteBackground.css';

// Fixed, full-viewport backdrop painted behind the whole app (TopBar +
// #content, and the pre-auth login shell). The image is chosen by route
// (see routeBackgrounds.ts). Text contrast is guaranteed by the surfaces
// text sits on — Panel/Card are `bg-surface/92 backdrop-blur-md`, opaque
// enough to hold WCAG AA over any plate — not by this layer; the
// `.route-bg::after` veil is only a light atmosphere/settle wash (heavier
// in dark). Purely decorative — aria-hidden, never interactive.
export function RouteBackground() {
  const { pathname } = useLocation();
  return (
    <div
      aria-hidden
      className="route-bg"
      style={{ backgroundImage: `url("${backgroundForPath(pathname)}")` }}
    />
  );
}
