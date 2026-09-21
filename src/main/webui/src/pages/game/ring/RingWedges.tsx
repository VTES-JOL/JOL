import { memo, useId } from 'react';
import {
  arcPath,
  polar,
  rad,
  sectorPath,
  type RingGeometry,
} from './ringGeometry';
import type { RingLayout } from './ringLayout';
import type { RingSeatModel } from './ringModel';

// The ring's static SVG chrome: one clickable wedge per seat, the divider between
// the play area and the rim lane, a direction chevron on each wedge boundary
// (play flows predator → prey, i.e. clockwise), and the hub. Cards are drawn by
// RingSeat in an HTML layer above this, so a click on empty wedge reaches the
// wedge and a click on a card reaches the card.

export interface RingWedgesProps {
  geometry: RingGeometry;
  layout: RingLayout;
  seats: RingSeatModel[];
  /** Seat outlined in gold — the viewer's own. */
  viewerIndex: number;
  /** Seat the board is focused on (outlined in the accent colour). */
  focusIndex?: number;
  showNames?: boolean;
  onSeatClick?: (seat: RingSeatModel) => void;
}

const NAME_MAX = 16;

function chevronPoints(g: RingGeometry, boundaryDeg: number): string {
  const c = polar(g, boundaryDeg, g.outR + 6 * g.k);
  const tan = rad(boundaryDeg + 90);
  const rr = rad(boundaryDeg);
  const f = (n: number) => Math.round(n * 10) / 10;
  const tip = { x: c.x + 6 * Math.cos(tan), y: c.y + 6 * Math.sin(tan) };
  const b1 = { x: c.x - 3 * Math.cos(tan) + 4 * Math.cos(rr), y: c.y - 3 * Math.sin(tan) + 4 * Math.sin(rr) };
  const b2 = { x: c.x - 3 * Math.cos(tan) - 4 * Math.cos(rr), y: c.y - 3 * Math.sin(tan) - 4 * Math.sin(rr) };
  return `${f(tip.x)},${f(tip.y)} ${f(b1.x)},${f(b1.y)} ${f(b2.x)},${f(b2.y)}`;
}

export const RingWedges = memo(function RingWedges({
  geometry: g,
  layout,
  seats,
  viewerIndex,
  focusIndex = -1,
  showNames = false,
  onSeatClick,
}: RingWedgesProps) {
  const uid = useId();
  return (
    <svg width={g.size} height={g.size} viewBox={`0 0 ${g.size} ${g.size}`} className="absolute inset-0">
      {layout.seats.map((sl, i) => {
        const seat = seats[i];
        const self = i === viewerIndex;
        const focused = i === focusIndex;
        const label = `${seat.name}${seat.ousted ? ', out of the game' : ''}: ${seat.ready.length} ready, ${seat.torpor.length} in torpor, ${seat.uncontrolled.length} uncontrolled`;
        const activate = onSeatClick ? () => onSeatClick(seat) : undefined;
        // Seat name runs along the gap between the play area and the rim lane. On the
        // bottom half the path runs left → right (counter-clockwise) so text is never upside-down.
        const bottom = Math.sin(rad(sl.theta)) > 0.01;
        const nameR = g.playOut + (bottom ? 16 : 8) * g.k;
        const span = 24;
        const nameId = `${uid}-name-${seat.seatIndex}`;
        const p0 = polar(g, bottom ? sl.theta + span : sl.theta - span, nameR);
        const p1 = polar(g, bottom ? sl.theta - span : sl.theta + span, nameR);
        const namePath = `M ${p0.x.toFixed(1)} ${p0.y.toFixed(1)} A ${nameR.toFixed(1)} ${nameR.toFixed(1)} 0 0 ${bottom ? 0 : 1} ${p1.x.toFixed(1)} ${p1.y.toFixed(1)}`;
        return (
          <g key={seat.name} opacity={seat.ousted ? 0.4 : 1}>
            <path
              d={sectorPath(g, sl.a0, sl.a1, g.hubR + 8 * g.k, g.outR)}
              fill={seat.color}
              fillOpacity={seat.active ? 0.16 : 0.07}
              stroke={focused ? 'var(--color-accent)' : self ? '#cf9f3f' : seat.color}
              strokeOpacity={focused || self ? 0.95 : 0.5}
              strokeWidth={focused ? 2.6 : self ? 1.8 : 1}
              className={activate ? 'cursor-pointer hover:fill-opacity-20 focus-visible:[stroke-width:3px] focus-visible:[stroke:var(--color-accent)]' : undefined}
              style={{ outline: 'none' }}
              role={activate ? 'button' : undefined}
              tabIndex={activate ? 0 : undefined}
              aria-label={label}
              onClick={activate}
              onKeyDown={
                activate
                  ? (e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        activate();
                      }
                    }
                  : undefined
              }
            >
              <title>{label}</title>
            </path>
            <path
              d={arcPath(g, sl.a0 + 0.8, sl.a1 - 0.8, g.playOut + 4 * g.k)}
              fill="none"
              stroke={seat.color}
              strokeOpacity={0.35}
              strokeDasharray="2 4"
              pointerEvents="none"
            />
            <polygon points={chevronPoints(g, sl.theta + (sl.a1 - sl.a0) / 2 + 1.6)} fill={seat.color} fillOpacity={0.7} pointerEvents="none" />
            {showNames && (
              <>
                <path id={nameId} d={namePath} fill="none" />
                <text fontSize={8 * Math.max(0.9, g.k)} fontWeight={600} letterSpacing={0.4} fill={seat.color} pointerEvents="none">
                  <textPath href={`#${nameId}`} startOffset="50%" textAnchor="middle">
                    {seat.name.length > NAME_MAX ? `${seat.name.slice(0, NAME_MAX - 1)}…` : seat.name}
                  </textPath>
                </text>
              </>
            )}
          </g>
        );
      })}
      <circle cx={g.cx} cy={g.cy} r={g.hubR} className="fill-panel stroke-line-accent" strokeWidth={1} pointerEvents="none" />
    </svg>
  );
});
