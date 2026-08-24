// Every top-level view is now React-owned (mirrors MainServlet's
// @WebServlet mapping) — path helpers for React Router <Link>s.
export function pathForView(view: string): string {
  return view === 'main' ? '/jol/' : `/jol/${view}`;
}

export function pathForGame(gameId: string): string {
  return `/jol/game/${gameId}`;
}

export function pathForHelp(section?: string): string {
  return section ? `/jol/help/${section}` : '/jol/help';
}
