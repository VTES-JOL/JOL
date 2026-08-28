import { type SvgIconProps, SvgImg } from './svgIcon';
import { svgUrl } from './svgPath';

/** Card-type icon — e.g. type "Action Modifier" -> /svg/type/actionmodifier.svg */
export function TypeIcon({ type, ...rest }: { type: string } & SvgIconProps) {
  if (!type) return null;
  return <SvgImg src={svgUrl(`type/${type.toLowerCase().replace(/\s+/g, '')}.svg`)} alt={type} {...rest} />;
}

/** Clan / creed icon — e.g. "Banu Haqim" -> /svg/clan/banuhaqim.svg */
export function ClanIcon({ clan, ...rest }: { clan: string } & SvgIconProps) {
  if (!clan) return null;
  return <SvgImg src={svgUrl(`clan/${clan.toLowerCase().replace(/\s+/g, '')}.svg`)} alt={clan} {...rest} />;
}

/**
 * Discipline icon. Case marks level: UPPERCASE = superior (/svg/disc/sup/ANI.svg),
 * lowercase = inferior (/svg/disc/inf/ani.svg).
 */
export function DisciplineIcon({ discipline, ...rest }: { discipline: string } & SvgIconProps) {
  if (!discipline) return null;
  const superior = discipline === discipline.toUpperCase() && discipline !== discipline.toLowerCase();
  const file = superior ? discipline : discipline.toLowerCase();
  return <SvgImg src={svgUrl(`disc/${superior ? 'sup' : 'inf'}/${file}.svg`)} alt={discipline} {...rest} />;
}

/** Path icon — first word of the path string, e.g. "Power and the Inner Voice" -> /svg/path/power.svg */
export function PathIcon({ path, ...rest }: { path: string } & SvgIconProps) {
  if (!path) return null;
  return <SvgImg src={svgUrl(`path/${path.split(/\s+/)[0].toLowerCase()}.svg`)} alt={path} {...rest} />;
}

/**
 * Pool / blood cost icon. `blood4` -> /svg/cost/blood4.svg; falls back to
 * `/svg/cost/{type}cost.svg`, then hides on a second failure.
 */
export function CostIcon({
  type,
  amount,
  title,
  ...rest
}: { type: 'blood' | 'pool'; amount?: string | number } & SvgIconProps) {
  const t = type.toLowerCase();
  const a = amount !== undefined ? String(amount).toLowerCase() : '';
  const primary = svgUrl(`cost/${a ? `${t}${a}` : `${t}cost`}.svg`);
  const fallback = svgUrl(`cost/${t}cost.svg`);

  return (
    <SvgImg
      src={primary}
      alt={`${type} cost ${amount ?? ''}`.trim()}
      title={title || `${amount ?? ''} ${type}`.trim()}
      {...rest}
      onError={(e) => {
        const img = e.currentTarget;
        if (!img.src.endsWith(fallback)) img.src = fallback;
        else img.style.display = 'none';
      }}
    />
  );
}
