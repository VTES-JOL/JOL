// Shared country-flag glyph — the `fi fi-<cc> fis` span from flag-icons plus
// an Intl.DisplayNames lookup for the tooltip/label, which TopBar,
// OnlineUsers, NationStats and JolStats were each building on their own.
const regionNames = new Intl.DisplayNames(['en'], { type: 'region' });

/** English display name for an ISO-3166 alpha-2 code, or undefined if unknown. */
export function countryName(code: string): string | undefined {
  try {
    return regionNames.of(code.toUpperCase());
  } catch {
    return undefined;
  }
}

interface CountryFlagProps {
  code: string;
  /** Extra classes on the flag span (e.g. `rounded-1`). */
  className?: string;
  /** Attach the country name as a tippy tooltip. Default true. */
  tooltip?: boolean;
  /** Render ` <name>` after the flag. Default false. */
  withName?: boolean;
}

export function CountryFlag({ code, className, tooltip = true, withName = false }: CountryFlagProps) {
  const name = countryName(code);
  return (
    <>
      <span
        className={`fi fi-${code.toLowerCase()} fis${className ? ` ${className}` : ''}`}
        data-tippy-content={tooltip ? name : undefined}
      />
      {withName && <> {name}</>}
    </>
  );
}
