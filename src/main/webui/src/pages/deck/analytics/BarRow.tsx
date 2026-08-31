import type { ReactNode } from 'react';

/** A single labeled horizontal bar row used across all analytics sections. */
interface Props {
  label: ReactNode;
  count: number;
  max: number;
  color?: string;
}

export function BarRow({ label, count, max, color = 'bg-accent/70' }: Props) {
  const pct = max > 0 ? Math.round((count / max) * 100) : 0;
  return (
    <div className="flex items-center gap-2 px-3 py-1">
      <div className="shrink-0 w-[88px] text-[11px] text-ink-secondary truncate leading-none">
        {label}
      </div>
      <div className="flex-1 h-1.5 rounded-full bg-hover overflow-hidden min-w-0">
        <div className={`h-full rounded-full transition-all ${color}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="shrink-0 text-[11px] tabular-nums text-ink-muted w-5 text-right">{count}</span>
    </div>
  );
}
