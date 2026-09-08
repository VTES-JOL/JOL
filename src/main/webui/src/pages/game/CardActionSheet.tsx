import { useState } from 'react';
import { ChevronRight, Minus, Plus } from 'lucide-react';
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
import { BottomSheet } from './BottomSheet';
import { CardImage } from './CardImage';
import { CardAttrEditor } from './CardAttrEditor';
import { Clan } from './Clan';
import { Sect } from './Sect';
import { Path } from './Path';

// The <md card-action surface (ActSheet.dc.html) — the touch replacement for
// CardContextMenu's point-anchored popup, and now (#8 / D32) the only on-table
// card surface below md: the blood-transfer / clan-path-sect / label editors
// that used to sit behind "Open panel…" → CardActionModal are folded in here.
//
// #10b (D32): the sheet doubles as the card inspector on touch (no hover
// preview). It opens showing just the identity header, the card image and the
// counter stepper; the action list + editors are one tap away behind "Actions".
const ROW =
  'flex min-h-12 w-full items-center gap-3 border-t border-line/40 px-4 text-sm text-ink-secondary hover:bg-hover';
const STEP =
  'inline-flex h-11 w-11 items-center justify-center rounded-full border border-line-accent text-ink-secondary hover:bg-hover';

export function CardActionSheet({
  ctx,
  phase,
  viewerName,
  onSubmit,
  onCounterBump,
  onRequestTarget,
  onClose,
}: {
  ctx: TableCardContext;
  phase: string;
  viewerName: string | null;
  onSubmit: (submission: Submission) => void;
  onCounterBump: (ctx: TableCardContext, kind: CounterKind, step: number) => void;
  onRequestTarget: (actionId: string, rescuerName: string) => void;
  onClose: () => void;
}) {
  const [showActions, setShowActions] = useState(false);
  const [showAll, setShowAll] = useState(false);
  const [showAttrs, setShowAttrs] = useState(false);
  const [label, setLabel] = useState(ctx.card.label ?? '');
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
  const bump = (step: number) => onCounterBump(ctx, 'blood', step);

  const lead = likelyNow(env);
  const isMinion = !!card.minion;
  const showCounters = ctx.regionCommandKey !== 'ashheap';
  const showTransfers = ctx.controlledByViewer && isMinion && ctx.regionCommandKey !== 'ashheap';
  const showAttributes = isMinion && ctx.controlledByViewer && ctx.regionCommandKey === 'ready';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;

  const header = (
    <>
      <span className="min-w-0 flex-1">
        <span className="card-name block truncate text-base font-semibold text-ink">
          {card.name}
          {card.advanced && <i className="icon adv" />}
        </span>
        <span className="mt-0.5 flex flex-wrap items-center gap-x-2 gap-y-0.5 text-xs text-ink-muted">
          <Clan value={card.clan} />
          {(card.capacity ?? 0) > 0 && <span>capacity {card.capacity}</span>}
          <Sect value={card.sect} />
          <Path value={card.path} />
          {card.votes && card.votes !== '0' && <span>{card.votes} votes</span>}
        </span>
      </span>
      {(card.disciplines?.length ?? 0) > 0 && (
        <span className="flex shrink-0 items-center gap-1">
          {card.disciplines!.map((disc) => (
            <span key={disc} className={`icon ${disc}`} />
          ))}
        </span>
      )}
    </>
  );

  return (
    <BottomSheet open onClose={onClose} header={header} label={`Actions for ${card.name ?? 'card'}`}>
      {/* Inspector: image + counters, always visible (#10b). */}
      <div className="flex justify-center px-4 pt-3">
        <div className="w-40 max-w-full">
          <CardImage cardId={card.cardId ?? ''} secured={!!card.playtest} name={card.name ?? ''} card={card} />
        </div>
      </div>

      {showCounters && (
        <div className="flex items-center gap-3 border-t border-line/40 px-4 py-3">
          <span className="w-14 text-xs text-ink-muted">Counters</span>
          <div className="inline-flex items-center gap-2 rounded-full bg-blood/15 p-1">
            <button type="button" className={STEP} aria-label="Remove a counter" onClick={() => bump(-1)}>
              <Minus size={16} />
            </button>
            <span className="min-w-11 text-center text-base font-semibold tabular-nums">{counterText}</span>
            <button type="button" className={STEP} aria-label="Add a counter" onClick={() => bump(1)}>
              <Plus size={16} />
            </button>
          </div>
        </div>
      )}

      {showTransfers && (
        <div className="flex items-center gap-2 border-t border-line/40 px-4 py-3 text-sm">
          <span className="w-14 shrink-0 text-xs text-ink-muted">Blood</span>
          <button
            type="button"
            className="inline-flex min-h-11 items-center gap-1 rounded border border-line-accent px-2 text-ink-secondary hover:bg-hover"
            onClick={() => onCounterBump(ctx, 'transfer', 1)}
          >
            ← Move here
          </button>
          <span className="rounded-full bg-blood px-2 py-0.5 text-xs text-white">{ctx.controllerPool} pool</span>
          <button
            type="button"
            className="inline-flex min-h-11 items-center gap-1 rounded border border-line-accent px-2 text-ink-secondary hover:bg-hover"
            onClick={() => onCounterBump(ctx, 'transfer', -1)}
          >
            To pool →
          </button>
        </div>
      )}

      {!showActions ? (
        <button
          type="button"
          className={`${ROW} font-medium text-ink`}
          onClick={() => setShowActions(true)}
        >
          Actions
          <ChevronRight size={16} className="ml-auto" />
        </button>
      ) : (
        <>
          {lead.length > 0 && (
            <>
              <div className="border-t border-line/40 px-4 pb-1 pt-3 text-[0.7rem] font-semibold uppercase tracking-wide text-ink-muted">
                Likely now · {phase} phase
              </div>
              {lead.map((a) => (
                <button key={a.id} type="button" className={ROW} onClick={() => activate(a)}>
                  {a.label}
                </button>
              ))}
            </>
          )}

          {lead.length > 0 && !showAll && (
            <button type="button" className={`${ROW} text-ink-muted`} onClick={() => setShowAll(true)}>
              More actions…
              <span className="ml-auto">›</span>
            </button>
          )}

          {(showAll || lead.length === 0) &&
            GROUP_ORDER.map((group) => {
              const items = CARD_ACTIONS.filter((a) => a.group === group && actionAvailable(a, env));
              if (items.length === 0) return null;
              return (
                <div key={group}>
                  <div className="border-t border-line/40 px-4 pb-1 pt-3 text-[0.7rem] font-semibold uppercase tracking-wide text-ink-muted">
                    {GROUP_LABEL[group]}
                  </div>
                  {items.map((a) => (
                    <button
                      key={a.id}
                      type="button"
                      className={`${ROW} ${group === 'remove' ? 'text-blood' : ''}`}
                      onClick={() => activate(a)}
                    >
                      {a.label}
                    </button>
                  ))}
                </div>
              );
            })}

          {/* Label — free text every player sees. */}
          <div className="flex items-center gap-2 border-t border-line/40 px-4 py-3">
            <input
              type="text"
              className="min-w-0 flex-1 rounded border border-line bg-surface/70 px-2 py-2 text-sm text-ink outline-none focus:border-accent/60"
              placeholder="Label for all players…"
              value={label}
              onChange={(e) => setLabel(e.target.value)}
              onBlur={() => onSubmit(cardActions.label(ctx, label))}
              onKeyDown={(e) => {
                if (e.key === 'Enter') e.currentTarget.blur();
              }}
            />
          </div>

          {showAttributes && (
            <>
              <button
                type="button"
                className={`${ROW} text-ink-muted`}
                onClick={() => setShowAttrs((v) => !v)}
              >
                Edit clan / path / sect
                <span className="ml-auto">{showAttrs ? '›' : '‹'}</span>
              </button>
              {showAttrs && (
                <div className="border-t border-line/40 px-4 py-3">
                  <CardAttrEditor card={card} onChange={(attr, key) => onSubmit(cardActions[attr](ctx, key))} />
                </div>
              )}
            </>
          )}
        </>
      )}
      <div className="h-4 shrink-0" />
    </BottomSheet>
  );
}
