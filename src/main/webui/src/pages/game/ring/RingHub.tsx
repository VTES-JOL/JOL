import type { RingGeometry } from './ringGeometry';

// Centre of the ring: turn / phase / direction hint. Pure text overlay.
export function RingHub({ geometry: g, lines }: { geometry: RingGeometry; lines: string[] }) {
  const w = Math.round(128 * g.k);
  const [first, ...rest] = lines;
  return (
    <div
      className="pointer-events-none absolute flex flex-col items-center justify-center gap-0.5 text-center"
      style={{ left: g.cx - w / 2, top: g.cy - w / 2, width: w, height: w }}
    >
      {first && <span className="text-[13px] font-semibold text-ink">{first}</span>}
      {rest.map((l, i) => (
        <span
          key={i}
          className={i === 0 ? 'text-ink-secondary' : 'px-2 leading-tight text-ink-muted'}
          style={{ fontSize: i === 0 ? 10 : 8 }}
        >
          {l}
        </span>
      ))}
    </div>
  );
}
