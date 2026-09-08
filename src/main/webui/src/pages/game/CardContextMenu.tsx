import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Minus, Plus } from 'lucide-react';
import { cardActions, type Submission, type TableCardContext } from './cardCommands';
import type { CounterKind } from './useCounterBump';
import {
  CARD_ACTIONS,
  GROUP_LABEL,
  GROUP_ORDER,
  actionAvailable,
  likelyNow,
  type ActionEnv,
} from './cardActionRegistry';
import { useIsMobile } from '../../hooks/useMediaQuery';
import { CardActionSheet } from './CardActionSheet';
import { CardAttrEditor } from './CardAttrEditor';

export type MenuAnchor = { x: number; y: number };

// ≥44px hit area on touch (finding #9), no visual bloat on pointer devices.
const STEP =
  'inline-flex h-5 w-5 min-h-11 min-w-11 md:min-h-0 md:min-w-0 items-center justify-center rounded border border-line-accent text-ink-secondary hover:bg-hover';
const ITEM = 'flex w-full min-h-11 md:min-h-0 items-center gap-2 px-3 py-1.5 text-left text-sm hover:bg-hover';

// The desktop quick-action menu (click / right-click a card). #8 (D32): this is
// now the *only* desktop on-table surface — the old "Open panel…" →
// CardActionModal is gone; its blood transfer / clan-path-sect / label editors
// are folded in as conditional inline rows. To keep it from being a 14-row
// wall, it leads with the phase-aware "Likely now" set (shared with the mobile
// sheet's LIKELY NOW) and hides the full grouped list behind "More…".
export function CardContextMenu({
  ctx,
  anchor,
  phase,
  viewerName,
  onSubmit,
  onCounterBump,
  onRequestTarget,
  onClose,
}: {
  ctx: TableCardContext;
  anchor: MenuAnchor;
  phase: string;
  viewerName: string | null;
  onSubmit: (submission: Submission) => void;
  onCounterBump: (ctx: TableCardContext, kind: CounterKind, step: number) => void;
  onRequestTarget: (actionId: string, rescuerName: string) => void;
  onClose: () => void;
}) {
  const ref = useRef<HTMLDivElement>(null);
  const [pos, setPos] = useState(anchor);
  const [showAll, setShowAll] = useState(false);
  const [showAttrs, setShowAttrs] = useState(false);
  const [label, setLabel] = useState(ctx.card.label ?? '');
  const isMobile = useIsMobile();

  // Clamp the menu into the viewport, measuring from the original click anchor.
  // Re-run whenever the menu's own size changes — expanding "More actions…" or
  // the clan/path/sect editor grows it, and without a re-clamp the bottom runs
  // off-screen.
  const clamp = useCallback(() => {
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

  useLayoutEffect(() => {
    clamp();
    const el = ref.current;
    if (!el || typeof ResizeObserver === 'undefined') return;
    const ro = new ResizeObserver(() => clamp());
    ro.observe(el);
    return () => ro.disconnect();
  }, [clamp]);

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

  const activate = (a: (typeof CARD_ACTIONS)[number]) => {
    if (a.requestTarget) {
      onRequestTarget(a.id, card.name ?? '');
      onClose();
      return;
    }
    onSubmit(a.build(ctx));
    onClose();
  };
  // Counter steps keep the menu open so you can tap a few in a row — each tap
  // updates the board immediately and the merged command is sent on a short
  // debounce (see useCounterBump).
  const bump = (step: number) => onCounterBump(ctx, 'blood', step);

  const isMinion = !!card.minion;
  const showCounters = ctx.regionCommandKey !== 'ashheap';
  const showTransfers = ctx.controlledByViewer && isMinion && ctx.regionCommandKey !== 'ashheap';
  const showAttributes = isMinion && ctx.controlledByViewer && ctx.regionCommandKey === 'ready';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const lead = likelyNow(env);

  // <md: the point-anchored popup becomes a bottom sheet (ActSheet.dc.html).
  if (isMobile) {
    return (
      <CardActionSheet
        ctx={ctx}
        phase={phase}
        viewerName={viewerName}
        onSubmit={onSubmit}
        onCounterBump={onCounterBump}
        onRequestTarget={onRequestTarget}
        onClose={onClose}
      />
    );
  }

  const actionButton = (a: (typeof CARD_ACTIONS)[number], danger = false) => (
    <button
      key={a.id}
      type="button"
      role="menuitem"
      className={`${ITEM} ${danger ? 'text-blood' : 'text-ink-secondary'}`}
      title={a.title}
      onClick={() => activate(a)}
    >
      {a.label}
    </button>
  );

  return createPortal(
    <div
      ref={ref}
      role="menu"
      aria-label={`Actions for ${card.name ?? 'card'}`}
      className="fixed z-50 min-w-[13rem] max-w-[16rem] max-h-[80vh] overflow-y-auto rounded-md border border-line-accent bg-surface py-1 text-sm shadow-xl"
      style={{ left: pos.x, top: pos.y }}
    >
      <div className="truncate border-b border-line/60 px-3 py-1 text-xs font-semibold text-ink">{card.name}</div>

      {showCounters && (
        <div className="flex items-center justify-between px-3 py-1.5">
          <span className="text-xs text-ink-muted">Counters</span>
          <span className="inline-flex items-center gap-1.5">
            <button type="button" className={STEP} aria-label="Remove a counter" onClick={() => bump(-1)}>
              <Minus size={13} />
            </button>
            <span className="min-w-[3ch] text-center text-xs tabular-nums">{counterText}</span>
            <button type="button" className={STEP} aria-label="Add a counter" onClick={() => bump(1)}>
              <Plus size={13} />
            </button>
          </span>
        </div>
      )}

      {showTransfers && (
        <div className="flex items-center justify-between gap-1 border-t border-line/60 px-3 py-1.5 text-xs">
          <span className="text-ink-muted">Blood</span>
          <span className="inline-flex items-center gap-1">
            <button
              type="button"
              className="rounded border border-line-accent px-1.5 py-0.5 text-ink-secondary hover:bg-hover"
              title="Move one blood from your pool onto this card"
              onClick={() => onCounterBump(ctx, 'transfer', 1)}
            >
              ← here
            </button>
            <span className="tabular-nums text-ink-muted">{ctx.controllerPool}</span>
            <button
              type="button"
              className="rounded border border-line-accent px-1.5 py-0.5 text-ink-secondary hover:bg-hover"
              title="Move one blood from this card back to your pool"
              onClick={() => onCounterBump(ctx, 'transfer', -1)}
            >
              pool →
            </button>
          </span>
        </div>
      )}

      {/* Likely now — the phase-weighted lead, no group heading. */}
      {lead.length > 0 && (
        <div className="border-t border-line/60 py-1" role="group" aria-label="Likely now">
          {lead.map((a) => actionButton(a))}
        </div>
      )}

      {lead.length > 0 && !showAll ? (
        <button
          type="button"
          role="menuitem"
          className={`${ITEM} text-ink-muted`}
          onClick={() => setShowAll(true)}
        >
          More actions…
          <span className="ml-auto">›</span>
        </button>
      ) : (
        (showAll || lead.length === 0) &&
        GROUP_ORDER.map((group) => {
          const items = CARD_ACTIONS.filter((a) => a.group === group && actionAvailable(a, env));
          if (items.length === 0) return null;
          return (
            <div key={group} className="border-t border-line/60 py-1" role="group" aria-label={GROUP_LABEL[group]}>
              {items.map((a) => actionButton(a, group === 'remove'))}
            </div>
          );
        })
      )}

      {/* Label — free text every player sees. */}
      <div className="border-t border-line/60 px-3 py-1.5">
        <input
          type="text"
          className="w-full rounded border border-line bg-surface/70 px-2 py-1 text-xs text-ink outline-none focus:border-accent/60"
          placeholder="Label…"
          value={label}
          onChange={(e) => setLabel(e.target.value)}
          onBlur={() => onSubmit(cardActions.label(ctx, label))}
          onKeyDown={(e) => {
            if (e.key === 'Enter') e.currentTarget.blur();
          }}
        />
      </div>

      {showAttributes && (
        <div className="border-t border-line/60">
          <button
            type="button"
            role="menuitem"
            className={`${ITEM} text-ink-muted`}
            onClick={() => setShowAttrs((v) => !v)}
          >
            Edit clan / path / sect
            <span className="ml-auto">{showAttrs ? '▾' : '▸'}</span>
          </button>
          {showAttrs && (
            <div className="px-3 pb-2">
              <CardAttrEditor card={card} onChange={(attr, key) => onSubmit(cardActions[attr](ctx, key))} />
            </div>
          )}
        </div>
      )}
    </div>,
    document.body,
  );
}
