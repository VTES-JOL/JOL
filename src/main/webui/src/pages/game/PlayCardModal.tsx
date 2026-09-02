import { useEffect, useState } from 'react';
import type { CardMode, CardModeTarget, CardSnapshot } from '../../api/types';
import { buildDiscardCommand, buildHandLabelCommand, buildPlayCommand, needsTargetPicker, type HandCardContext } from './cardCommands';
import { Recycle, Tag, Trash2 } from 'lucide-react';
import { CardImage } from './CardImage';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Switch } from '../../components/ui/Switch';
import { Clan } from './Clan';

const MODE_BTN =
  'w-full rounded border px-3 py-1.5 text-sm transition-colors border-line-accent text-ink-secondary hover:bg-hover';

export interface PendingTarget {
  ctx: HandCardContext;
  disciplines: string[] | null;
  target: CardModeTarget;
  doNotReplace: boolean;
  cardName: string;
  typeClass: string;
}

// Mirrors play-card-modal.jsp + showPlayCardModal()/modeClicked()/playCard()/
// discard()/multiModeButtonClicked() from card-modal.js. Triggered by
// clicking a HAND (or RESEARCH) region card belonging to the viewer. The card
// definition (modes / replace rules / preamble / cost) rides on the
// CardSnapshot itself — GameSnapshotFactory enriches the viewer's own
// hand/research cards — so there's no separate fetch.
export function PlayCardModal({
  ctx,
  card,
  viewerName,
  onSubmit,
  onClose,
  onRequestTarget,
}: {
  ctx: HandCardContext;
  card: CardSnapshot;
  viewerName: string;
  onSubmit: (submission: { command?: string; chat?: string }) => void;
  onClose: () => void;
  onRequestTarget: (pending: PendingTarget) => void;
}) {
  const [label, setLabel] = useState(card.label ?? '');
  const [selected, setSelected] = useState<Set<number>>(new Set());
  // Default on: play refills the hand, matching pre-toggle behaviour. Unticking
  // suppresses the `draw` so a player can chain "played at the same time" cards
  // (e.g. "only as announced …") before drawing back up manually.
  const [replace, setReplace] = useState(true);

  useEffect(() => {
    setSelected(new Set());
    setReplace(true);
  }, [card.cardId]);

  const cardName = card.name ?? '';
  const typeClass = card.typeClass ?? '';
  const modes = card.modes ?? [];
  const isResearch = ctx.regionCommandKey === 'research';
  const doNotReplace = isResearch ? true : !!card.doNotReplace;
  // doNotReplace cards (and research cards) are structurally never replaced —
  // nothing to opt out of, so the toggle is shown disabled/off for them.
  const canOptOut = !doNotReplace;
  const effectiveNoReplace = doNotReplace || !replace;

  const play = (mode: CardMode) => {
    if (needsTargetPicker(mode.target)) {
      onRequestTarget({
        ctx,
        disciplines: mode.disciplines,
        target: mode.target!,
        doNotReplace: effectiveNoReplace,
        cardName,
        typeClass,
      });
      onClose();
      return;
    }
    onSubmit({ command: buildPlayCommand(ctx, mode.disciplines, mode.target, null, effectiveNoReplace) });
    onClose();
  };

  const playMulti = () => {
    const picked = modes.filter((_, i) => selected.has(i));
    const disciplines = picked.flatMap((m) => m.disciplines ?? []);
    const target = picked[0]?.target ?? null;
    if (target && needsTargetPicker(target)) {
      onRequestTarget({ ctx, disciplines, target, doNotReplace: effectiveNoReplace, cardName, typeClass });
      onClose();
      return;
    }
    onSubmit({ command: buildPlayCommand(ctx, disciplines, target, null, effectiveNoReplace) });
    onClose();
  };

  const discard = (replace: boolean) => {
    onSubmit({ command: buildDiscardCommand(ctx, replace) });
    onClose();
  };

  const updateLabel = () => {
    onSubmit({ command: buildHandLabelCommand(viewerName, ctx.coordinate, label) });
  };

  return (
    <Modal
      onClose={onClose}
      bodyClassName="flex flex-col gap-3 p-4 min-h-0 overflow-y-auto flex-1 text-center"
      title={
        <>
          <span className={`icon card-type ${typeClass}`} /> <span className="card-name">{cardName}</span>
        </>
      }
    >
      <CardImage cardId={card.cardId ?? ''} secured={!!card.playtest} name={cardName} />
      <div className="flex justify-center items-center gap-2">
        {(card.clanClasses ?? []).map((clan) => (
          <Clan key={clan} value={clan} />
        ))}
        {card.cost && <span>Cost: {card.cost}</span>}
      </div>
      {card.preamble && <p className="text-sm text-ink-secondary">{card.preamble}</p>}
      <div className="flex flex-col gap-2">
        {modes.map((mode, i) =>
          card.multiMode ? (
            <button
              key={i}
              type="button"
              className={`${MODE_BTN} ${selected.has(i) ? 'bg-accent text-surface border-accent' : ''}`}
              onClick={() =>
                setSelected((prev) => {
                  const next = new Set(prev);
                  if (next.has(i)) next.delete(i);
                  else next.add(i);
                  return next;
                })
              }
            >
              {(mode.disciplines ?? []).map((d) => (
                <span key={d} className={`icon ${d}`} />
              ))}{' '}
              <span dangerouslySetInnerHTML={{ __html: mode.text }} />
            </button>
          ) : (
            <button key={i} type="button" className={MODE_BTN} onClick={() => play(mode)}>
              {(mode.disciplines ?? []).map((d) => (
                <span key={d} className={`icon ${d}`} />
              ))}{' '}
              <span dangerouslySetInnerHTML={{ __html: mode.text }} />
            </button>
          ),
        )}
      </div>
      {card.multiMode && (
        <div>
          <hr className="my-2 border-line" />
          <Button variant="secondary" size="sm" disabled={selected.size < 1} onClick={playMulti}>
            {selected.size < 1 ? 'Select one or more disciplines' : 'Play'}
          </Button>
        </div>
      )}
      <div className="flex justify-center">
        <Switch
          id="play-replace"
          checked={replace && canOptOut}
          disabled={!canOptOut}
          onChange={(e) => setReplace(e.target.checked)}
          title={
            canOptOut
              ? 'Draw a replacement card when this is played. Untick to chain same-time cards, then draw manually.'
              : isResearch
                ? 'Research cards are never replaced.'
                : 'This card is flagged "do not replace".'
          }
          label="Replace this card"
        />
      </div>
      <div className="flex justify-center items-center gap-2">
        <Button variant="danger" size="sm" icon={<Trash2 size={14} />} title="Discard" onClick={() => discard(false)}>
          Discard
        </Button>
        <Button variant="danger" size="sm" icon={<Recycle size={14} />} title="Discard and replace" onClick={() => discard(true)}>
          Discard + Draw
        </Button>
      </div>
      <div className="flex items-stretch">
        <span className="flex items-center rounded-l border border-r-0 border-line bg-panel px-2 text-ink-muted">
          <Tag size={14} />
        </span>
        <input
          type="text"
          className="flex-1 min-w-0 rounded-r border border-line bg-surface/70 px-2 py-1 text-sm text-ink outline-none focus:border-accent/60"
          placeholder="Add a label for all players to see."
          value={label}
          onChange={(e) => setLabel(e.target.value)}
          onBlur={updateLabel}
        />
      </div>
    </Modal>
  );
}
