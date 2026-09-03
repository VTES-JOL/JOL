// Every top-level view is now React-owned (mirrors the route list Tomcat's
// RewriteValve serves index.html for — see rewrite.config).
//
// ROUTE_PATHS is the single source of truth for every path pattern: the
// <Route path> table in src/app/AppRoutes.tsx / src/app/AppShell.tsx reads
// it, and the path* helpers below build concrete hrefs from the same
// constants for <Link>s. Change a path in one place only.
export const ROUTE_PATHS = {
  login: '/jol/login',
  // MainPage answers all three — '/jol/' is canonical, the other two are
  // legacy entry points still linked from the wild.
  main: ['/jol/', '/jol/main', '/jol/main.jsp'] as const,
  profile: '/jol/profile',
  admin: '/jol/admin',
  tournamentAdmin: '/jol/tournamentAdmin',
  tournament: '/jol/tournament',
  judge: '/jol/judge',
  // '/jol/active' is canonical (the nav links here); '/jol/watch' is the
  // friendlier alias matching the nav label.
  watch: ['/jol/active', '/jol/watch'] as const,
  lobby: '/jol/lobby',
  deck: '/jol/deck',
  game: '/jol/game/:gameId',
  help: '/jol/help',
  helpSection: '/jol/help/:section',
} as const;

export function pathForView(view: string): string {
  return view === 'main' ? ROUTE_PATHS.main[0] : `/jol/${view}`;
}

export function pathForGame(gameId: string): string {
  return `/jol/game/${gameId}`;
}

export function pathForHelp(section?: string): string {
  return section ? `${ROUTE_PATHS.help}/${section}` : ROUTE_PATHS.help;
}
