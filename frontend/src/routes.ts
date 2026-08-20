// Views converted to React so far — kept in sync with MainServlet.REACT_ROUTES on
// the backend. Anything not listed here is still legacy JSP/jQuery, so links to it
// must be plain <a href> (full page load) rather than a React Router <Link>.
export const CONVERTED_VIEWS = new Set<string>(['main', 'profile', 'admin', 'tournamentAdmin', 'tournament', 'active', 'lobby']);

export function pathForView(view: string): string {
  return view === 'main' ? '/jol/' : `/jol/${view}`;
}

export function isConverted(view: string): boolean {
  return CONVERTED_VIEWS.has(view);
}
