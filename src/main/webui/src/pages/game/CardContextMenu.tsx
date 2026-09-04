import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Minus, Plus } from 'lucide-react';
import { cardActions, type Submission, type TableCardContext } from './cardCommands';
import { CARD_ACTIONS, GROUP_LABEL, GROUP_ORDER, actionAvailable, type ActionEnv } from './cardActionRegistry';

export type MenuAnchor = { x: number; y: number };

const STEP =
  'inline-flex h-5 w-5 items-center justify-center rounded border border-line-accent text-ink-secondary hover:bg-hover';
const ITEM = 'flex w-full items-center gap-2 px-3 py-1.5 text-left text-sm hover:bg-hover';

// The quick-action menu that opens on a card click / right-click (the modal is
// now behind "Open panel…"). Same action list as CardActionModal — both read
// cardActionRegistry — so they never drift.
export function CardContextMenu({
  ctx,
  anchor,
  phase,
  viewerName,
  onSubmit,
  onOpenPanel,
  onClose,
}: {
  ctx: TableCardContext;
  anchor: MenuAnchor;
  phase: string;
  viewerName: string | null;
  onSubmit: (submission: Submission) => void;
  onOpenPanel: () => void;
  onClose: () => void;
}) {
  const ref = useRef<HTMLDivElement>(null);
  const [pos, setPos] = useState(anchor);

  // Clamp into the viewport once the menu has a measured size.
  useLayoutEffect(() => {
    const el = ref.current;
    if (!el) return;
    const r = el.getBoundingClientRect();
    const pad = 8;
    let x = anchor.x;
    let y = anchor.y;
    if (x + r.width + pad > window.innerWidth) x = window.innerWidth - r.width - pad;
    if (y + r.height + pad > window.innerHeight) y = window.innerHeight - r.height - pad;
    setPos({ x: Math.max(pad, x), y: Math.max(pad, y) });
  }, [anchor]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    const onDown = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onClose();
    };
    document.addEventListener('keydown', onKey);
    // Defer so the click that opened the menu doesn't immediately close it.
    const t = window.setTimeout(() => document.addEventListener('mousedown', onDown), 0);
    return () => {
      document.removeEventListener('keydown', onKey);
      document.removeEventListener('mousedown', onDown);
      window.clearTimeout(t);
    };
  }, [onClose]);

  const { card } = ctx;
  const env: ActionEnv = {
    region: ctx.regionCommandKey,
    phase,
    controlledByViewer: ctx.controlledByViewer,
    isCardOwner: !!card.owner && card.owner === viewerName,
    isChild: ctx.isChild,
    minion: !!card.minion,
    locked: !!card.locked,
    contested: !!card.contested,
    faceDown: !!card.faceDown,
  };

  const run = (build: (c: TableCardContext) => Submission) => {
    onSubmit(build(ctx));
    onClose();
  };
  // Counter steps keep the menu open so you can tap a few in a row.
  const bump = (build: (c: TableCardContext) => Submission) => onSubmit(build(ctx));

  const showCounters = ctx.regionCommandKey !== 'ashheap';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;

  return createPortal(
    <div
      ref={ref}
      role="menu"
      aria-label={`Actions for ${card.name ?? 'card'}`}
      className="fixed z-50 min-w-[12rem] max-h-[80vh] overflow-y-auto rounded-md border border-line-accent bg-surface py-1 text-sm shadow-xl"
      style={{ left: pos.x, top: pos.y }}
    >
      <div className="truncate border-b border-line/60 px-3 py-1 text-xs font-semibold text-ink">
        {card.name}
      </div>

      {showCounters && (
        <div className="flex items-center justify-between px-3 py-1.5">
          <span className="text-xs text-ink-muted">Counters</span>
          <span className="inline-flex items-center gap-1.5">
            <button type="button" className={STEP} aria-label="Remove a counter" onClick={() => bump(cardActions.removeCounter)}>
              <Minus size={13} />
            </button>
            <span className="min-w-[3ch] text-center text-xs tabular-nums">{counterText}</span>
            <button type="button" className={STEP} aria-label="Add a counter" onClick={() => bump(cardActions.addCounter)}>
              <Plus size={13} />
            </button>
          </span>
        </div>
      )}

      {GROUP_ORDER.map((group) => {
        const items = CARD_ACTIONS.filter((a) => a.group === group && actionAvailable(a, env));
        if (items.length === 0) return null;
        return (
          <div key={group} className="border-t border-line/60 py-1" role="group" aria-label={GROUP_LABEL[group]}>
            {items.map((a) => (
              <button
                key={a.id}
                type="button"
                role="menuitem"
                className={`${ITEM} ${group === 'remove' ? 'text-blood' : 'text-ink-secondary'}`}
                onClick={() => run(a.build)}
              >
                {a.label}
              </button>
            ))}
          </div>
        );
      })}

      <div className="border-t border-line/60 pt-1">
        <button type="button" role="menuitem" className={`${ITEM} text-ink-muted`} onClick={onOpenPanel}>
          Open panel…
        </button>
      </div>
    </div>,
    document.body,
  );
}
