import { matchPath } from 'react-router-dom';
import { ROUTE_PATHS } from '../routes';

// Route → location plate. Ported from the jol-quarkus rewrite's per-page
// `background` prop (shared/layout/AppLayout.tsx there) — same five images,
// copied into public/backgrounds/. Related pages share a scene so navigating
// within an area (all tournament views, all watch views, …) doesn't swap the
// backdrop out from under you.
const bg = (name: string): string => `${import.meta.env.BASE_URL}backgrounds/${name}`;

interface Rule {
  patterns: readonly string[];
  // matchPath's `end` — false lets a pattern match its own sub-routes too
  // (e.g. /jol/help also covering /jol/help/:section).
  prefix?: boolean;
  image: string;
}

const RULES: readonly Rule[] = [
  { patterns: [ROUTE_PATHS.login], image: bg('Locations76.jpg') },
  { patterns: [ROUTE_PATHS.lobby], image: bg('Locations4.jpg') },
  { patterns: [ROUTE_PATHS.deck, ROUTE_PATHS.game], prefix: true, image: bg('Locations23.jpg') },
  {
    patterns: [ROUTE_PATHS.tournament, ROUTE_PATHS.tournamentAdmin, ROUTE_PATHS.judge],
    image: bg('Locations52.jpg'),
  },
  {
    patterns: [...ROUTE_PATHS.watch, ROUTE_PATHS.profile, ROUTE_PATHS.admin, ROUTE_PATHS.help],
    prefix: true,
    image: bg('Locations14.jpg'),
  },
  // Home / main and anything unmatched.
  { patterns: ROUTE_PATHS.main, image: bg('Locations76.jpg') },
];

const FALLBACK = bg('Locations76.jpg');

export function backgroundForPath(pathname: string): string {
  for (const rule of RULES) {
    for (const pattern of rule.patterns) {
      if (matchPath({ path: pattern, end: !rule.prefix }, pathname)) return rule.image;
    }
  }
  return FALLBACK;
}
