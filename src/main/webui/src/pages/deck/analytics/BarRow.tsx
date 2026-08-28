import type { ReactNode } from 'react';

/** A single labeled horizontal bar row used across all analytics sections. */
interface Props {
  label: ReactNode;
  count: number;
  max: number;
  color?: string;
}

export function BarRow({ label, count, max, color = 'jt:bg-accent/70' }: Props) {
  const pct = max > 0 ? Math.round((count / max) * 100) : 0;
  return (
    <div className="jt:flex jt:items-center jt:gap-2 jt:px-3 jt:py-1">
      <div className="jt:shrink-0 jt:w-[88px] jt:text-[11px] jt:text-ink-secondary jt:truncate jt:leading-none">
        {label}
      </div>
      <div className="jt:flex-1 jt:h-1.5 jt:rounded-full jt:bg-hover jt:overflow-hidden jt:min-w-0">
        <div className={`jt:h-full jt:rounded-full jt:transition-all ${color}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="jt:shrink-0 jt:text-[11px] jt:tabular-nums jt:text-ink-muted jt:w-5 jt:text-right">{count}</span>
    </div>
  );
}
