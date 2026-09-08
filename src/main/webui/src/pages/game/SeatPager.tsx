import { useRef, useState, type ReactNode } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import type { PlayerSnapshot } from '../../api/types';

// The <md replacement for the #opponents grid + YourSeatDock: one seat at a
// time, full width, horizontal scroll-snap, your own seat as page 0 (home).
// A dot row + ‹ › buttons for non-swipe navigation. Each seat panel is its
// own vertical scroller.
export function SeatPager({
  seats,
  renderSeat,
}: {
  seats: PlayerSnapshot[];
  renderSeat: (seat: PlayerSnapshot) => ReactNode;
}) {
  const scrollerRef = useRef<HTMLDivElement>(null);
  const [page, setPage] = useState(0);

  const onScroll = () => {
    const el = scrollerRef.current;
    if (!el) return;
    const next = Math.round(el.scrollLeft / Math.max(1, el.clientWidth));
    if (next !== page) setPage(next);
  };

  const go = (i: number) => {
    const el = scrollerRef.current;
    if (!el) return;
    const clamped = Math.max(0, Math.min(seats.length - 1, i));
    el.scrollTo({ left: clamped * el.clientWidth, behavior: 'smooth' });
  };

  return (
    <div className="flex min-h-0 min-w-0 flex-1 flex-col">
      <div className="flex shrink-0 items-center justify-center gap-1.5 py-1.5">
        <button
          type="button"
          onClick={() => go(page - 1)}
          disabled={page === 0}
          aria-label="Previous seat"
          className="inline-flex min-h-11 min-w-11 md:min-h-0 md:min-w-0 items-center justify-center rounded p-1 text-ink-muted hover:text-ink disabled:opacity-30"
        >
          <ChevronLeft size={16} />
        </button>
        {seats.map((s, i) => (
          <span
            key={s.name}
            aria-hidden
            className={`h-1.5 w-1.5 rounded-full ${i === page ? 'bg-accent' : 'bg-line-accent'}`}
          />
        ))}
        <button
          type="button"
          onClick={() => go(page + 1)}
          disabled={page === seats.length - 1}
          aria-label="Next seat"
          className="inline-flex min-h-11 min-w-11 md:min-h-0 md:min-w-0 items-center justify-center rounded p-1 text-ink-muted hover:text-ink disabled:opacity-30"
        >
          <ChevronRight size={16} />
        </button>
        <span className="ml-1 text-[0.6rem] font-semibold uppercase tracking-wide text-ink-muted">
          {page === 0 ? 'your seat' : 'swipe the table'}
        </span>
      </div>
      <div
        ref={scrollerRef}
        onScroll={onScroll}
        className="game-board no-scrollbar flex min-w-0 flex-1 snap-x snap-mandatory overflow-x-auto overflow-y-hidden"
      >
        {seats.map((seat) => (
          <div key={seat.name} className="w-full min-w-0 shrink-0 snap-center overflow-x-hidden overflow-y-auto px-1">
            {renderSeat(seat)}
          </div>
        ))}
      </div>
    </div>
  );
}
