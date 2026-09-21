import { memo, type CSSProperties } from 'react';
import { MARKER_SIZE } from './miniCardFootprint';
import { markerLabel, type MarkerView } from './ringModel';

// A crypt vampire still in the uncontrolled region, drawn on the ring's rim: a
// dashed circle with a blood-progress arc and the blood count in the middle.
// Owns its own 30px box; size comes to the packer via markerFootprint().
// When the viewer cannot read the card (no capacity known) it shows the count only.

const R = 12.5;
const CIRC = 2 * Math.PI * R;

export interface UncontrolledMarkerProps {
  marker: MarkerView;
  seatColor: string;
  scale?: number;
  onClick?: (marker: MarkerView) => void;
}

export const UncontrolledMarker = memo(function UncontrolledMarker({
  marker,
  seatColor,
  scale = 1,
  onClick,
}: UncontrolledMarkerProps) {
  const label = markerLabel(marker);
  const frac = marker.capacity > 0 ? Math.max(0, Math.min(1, marker.counters / marker.capacity)) : 0;
  const style = {
    '--seat': seatColor,
    width: MARKER_SIZE,
    height: MARKER_SIZE,
    transform: `scale(${scale})`,
  } as CSSProperties;
  const cls = `relative flex shrink-0 items-center justify-center rounded-full bg-surface p-0 ${
    onClick ? 'cursor-pointer hover:brightness-125 focus-visible:outline-2 focus-visible:outline-accent' : ''
  }`;
  const content = (
    <>
      <svg viewBox="0 0 30 30" width={MARKER_SIZE} height={MARKER_SIZE} className="absolute inset-0 -rotate-90" aria-hidden>
        <circle cx="15" cy="15" r={R} fill="none" strokeWidth="2" strokeDasharray="3 2.4" className="stroke-[var(--seat)] opacity-60" />
        {frac > 0 && (
          <circle
            cx="15"
            cy="15"
            r={R}
            fill="none"
            strokeWidth="3"
            strokeLinecap="butt"
            strokeDasharray={`${(frac * CIRC).toFixed(1)} ${CIRC.toFixed(1)}`}
            className="stroke-blood"
          />
        )}
      </svg>
      <span className="relative font-mono text-[11px] font-bold leading-none text-ink">{marker.counters}</span>
    </>
  );
  return onClick ? (
    <button type="button" aria-label={label} title={label} className={cls} style={style} onClick={() => onClick(marker)}>
      {content}
    </button>
  ) : (
    <div role="img" aria-label={label} title={label} className={cls} style={style}>
      {content}
    </div>
  );
});
