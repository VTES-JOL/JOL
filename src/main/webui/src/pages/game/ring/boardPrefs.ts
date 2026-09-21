// Which seat layout to show, and how the choice is resolved. Pure.
//
//   session toggle for this game  >  profile preference  >  width default
//
// The session toggle lives in component state / sessionStorage and is never
// written to the profile; only the Preferences page persists `profile`.

export type RingLayoutKind = 'wedge' | 'panels';
export type BoardStyle = 'grid' | 'ring';

/** Wedge from 1024px up, Panels below (the 768–1023 band; mobile uses the level views instead). */
export function defaultLayoutForWidth(width: number): RingLayoutKind {
  return width >= 1024 ? 'wedge' : 'panels';
}

export function resolveLayout(
  session: RingLayoutKind | null | undefined,
  profile: RingLayoutKind | null | undefined,
  width: number,
): RingLayoutKind {
  return session ?? profile ?? defaultLayoutForWidth(width);
}
