import type { MiniCardMeter } from './MiniCard';

// Board-wide switch between the two MiniCard blood displays: a filled gauge or a
// row of pips. Small enough to sit under the zoom controls.

const OPTIONS: { value: MiniCardMeter; label: string }[] = [
  { value: 'gauge', label: 'Gauge' },
  { value: 'pips', label: 'Pips' },
];

export function MeterToggle({ value, onChange }: { value: MiniCardMeter; onChange: (m: MiniCardMeter) => void }) {
  return (
    <div role="radiogroup" aria-label="Card blood display" className="inline-flex overflow-hidden rounded border border-line-accent">
      {OPTIONS.map((o) => (
        <button
          key={o.value}
          type="button"
          role="radio"
          aria-checked={o.value === value}
          onClick={() => onChange(o.value)}
          className={`h-6 px-2 text-[11px] ${o.value === value ? 'bg-accent text-white' : 'bg-panel text-ink-secondary hover:bg-hover'}`}
        >
          {o.label}
        </button>
      ))}
    </div>
  );
}
