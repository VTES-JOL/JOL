import { useEffect, useState } from 'react';
import type { CardDefinition, CardMode, CardModeTarget, CardSnapshot } from '../../api/types';
import { fetchCardDefinition } from './cardDefinitions';
import { buildDiscardCommand, buildHandLabelCommand, buildPlayCommand, needsTargetPicker, type HandCardContext } from './cardCommands';
import { Recycle, Tag, Trash2 } from 'lucide-react';
import { CardImage } from './CardImage';
import { runRequest } from '../../api/mutate';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
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
// clicking a HAND (or RESEARCH) region card belonging to the viewer.
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
  const [definition, setDefinition] = useState<CardDefinition | null>(null);
  const [label, setLabel] = useState(card.label ?? '');
  const [selected, setSelected] = useState<Set<number>>(new Set());

  useEffect(() => {
    setDefinition(null);
    setSelected(new Set());
    runRequest(fetchCardDefinition(card.cardId ?? '', !!card.playtest), 'Failed to load card details', setDefinition);
  }, [card.cardId, card.playtest]);

  const doNotReplace = ctx.regionCommandKey === 'research' ? true : !!definition?.doNotReplace;

  const play = (mode: CardMode) => {
    if (needsTargetPicker(mode.target)) {
      onRequestTarget({
        ctx,
        disciplines: mode.disciplines,
        target: mode.target!,
        doNotReplace,
        cardName: definition?.displayName ?? card.name ?? '',
        typeClass: definition?.type ?? '',
      });
      onClose();
      return;
    }
    onSubmit({ command: buildPlayCommand(ctx, mode.disciplines, mode.target, null, doNotReplace) });
    onClose();
  };

  const playMulti = () => {
    const modes = (definition?.modes ?? []).filter((_, i) => selected.has(i));
    const disciplines = modes.flatMap((m) => m.disciplines ?? []);
    const target = modes[0]?.target ?? null;
    if (target && needsTargetPicker(target)) {
      onRequestTarget({
        ctx,
        disciplines,
        target,
        doNotReplace,
        cardName: definition?.displayName ?? card.name ?? '',
        typeClass: definition?.type ?? '',
      });
      onClose();
      return;
    }
    onSubmit({ command: buildPlayCommand(ctx, disciplines, target, null, doNotReplace) });
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
        definition ? (
          <>
            <span className={`icon card-type ${definition.type?.toLowerCase().replace(/ /g, '_').replace('/', ' ')}`} />{' '}
            <span className="card-name">{definition.displayName}</span>
          </>
        ) : undefined
      }
    >
      {!definition ? (
        <div style={{ height: '30vh' }} className="flex items-center justify-center text-ink-muted">
          Loading…
        </div>
      ) : (
        <>
          <CardImage cardId={card.cardId ?? ''} secured={!!card.playtest} name={definition.displayName} />
          <div className="flex justify-center items-center gap-2">
            {(definition.clans ?? []).map((clan) => (
              <Clan key={clan} value={clan} />
            ))}
            {definition.cost && <span>Cost: {definition.cost}</span>}
          </div>
          {definition.preamble && <p className="text-sm text-ink-secondary">{definition.preamble}</p>}
          <div className="flex flex-col gap-2">
            {(definition.modes ?? []).map((mode, i) =>
              definition.multiMode ? (
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
          {definition.multiMode && (
            <div>
              <hr className="my-2 border-line" />
              <Button variant="secondary" size="sm" disabled={selected.size < 1} onClick={playMulti}>
                {selected.size < 1 ? 'Select one or more disciplines' : 'Play'}
              </Button>
            </div>
          )}
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
        </>
      )}
    </Modal>
  );
}
