import { useRef } from 'react';
import { LayoutGrid, MessageSquare, Sparkles, Layers } from 'lucide-react';

export type MobileTab = 'table' | 'hand' | 'log' | 'act';

// The <md thumb-zone tab bar (Mobile.dc.html). In-flow at the foot of the table
// column (not position:fixed — a fixed bar overlapped the controls, D28). Each
// non-table tab toggles its bottom sheet; re-tapping the active one returns to
// the table.
export function MobileTabBar({
  active,
  onSelect,
  showHand,
  showAct,
  handCount,
  logUnread,
}: {
  active: MobileTab;
  onSelect: (tab: MobileTab) => void;
  showHand: boolean;
  showAct: boolean;
  handCount: number;
  logUnread: boolean;
}) {
  // Swipe-up anywhere on the bar opens the hand sheet ("drag up to play").
  const startY = useRef<number | null>(null);
  const onTouchStart = (e: React.TouchEvent) => {
    startY.current = e.touches[0].clientY;
  };
  const onTouchEnd = (e: React.TouchEvent) => {
    if (startY.current === null) return;
    const dy = startY.current - e.changedTouches[0].clientY;
    startY.current = null;
    if (dy > 60 && showHand && active !== 'hand') onSelect('hand');
  };

  const tab = (
    id: MobileTab,
    icon: React.ReactNode,
    label: string,
    opts: { badge?: number; dot?: boolean } = {},
  ) => (
    <button
      type="button"
      onClick={() => onSelect(id)}
      aria-pressed={active === id}
      className={`relative flex flex-1 flex-col items-center justify-center gap-0.5 text-[0.65rem] ${
        active === id ? 'font-bold text-accent' : 'text-ink-muted'
      }`}
    >
      <span className="relative">
        {icon}
        {opts.dot && (
          <span className="absolute -right-1.5 -top-0.5 h-2 w-2 rounded-full bg-accent" aria-hidden />
        )}
      </span>
      <span className="flex items-center gap-1">
        {label}
        {opts.badge !== undefined && opts.badge > 0 && (
          <span className="rounded-full bg-accent px-1 text-[0.6rem] leading-none text-surface">{opts.badge}</span>
        )}
      </span>
    </button>
  );

  return (
    // relative z-[60] keeps the bar painted above an open BottomSheet (z-50) so
    // it stays visible and tappable — switch sheets without closing first.
    <div
      className="relative z-[60] flex h-14 shrink-0 border-t border-line-accent bg-hover"
      onTouchStart={onTouchStart}
      onTouchEnd={onTouchEnd}
    >
      {tab('table', <LayoutGrid size={21} />, 'Table')}
      {showHand && tab('hand', <Layers size={21} />, 'Hand', { badge: handCount })}
      {tab('log', <MessageSquare size={21} />, 'Log', { dot: logUnread })}
      {showAct && tab('act', <Sparkles size={21} />, 'Act')}
    </div>
  );
}
