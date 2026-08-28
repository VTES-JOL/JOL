// The VTES SVG icon set copied from the jol-quarkus rewrite into public/svg/.
// Vite emits public/ at the build root, so with base '/jol/' the files resolve
// at `${BASE_URL}svg/...` in dev and prod alike (and at `/svg/...` under
// Storybook's root base).
const BASE = import.meta.env.BASE_URL;

export function svgUrl(path: string): string {
  return `${BASE}svg/${path}`;
}
