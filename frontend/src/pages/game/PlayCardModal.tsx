import { useEffect, useState } from 'react';
import type { CardDefinition, CardMode, CardModeTarget, CardSnapshot } from '../../api/types';
import { fetchCardDefinition } from './cardDefinitions';
import { buildDiscardCommand, buildHandLabelCommand, buildPlayCommand, needsTargetPicker, type HandCardContext } from './cardCommands';
import { CardImage } from './CardImage';
import { showError } from '../../components/toast';

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
    fetchCardDefinition(card.cardId ?? '', !!card.playtest)
      .then(setDefinition)
      .catch((err) => {
        console.error('Failed to load card definition', err);
        showError('Failed to load card details.');
      });
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
    <div className="modal d-block" tabIndex={-1} style={{ background: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog" role="document">
        <div className="modal-content" style={{ textAlign: 'center' }}>
          {!definition ? (
            <div style={{ height: '30vh' }} className="d-flex align-items-center justify-content-center">
              <h2>Loading...</h2>
            </div>
          ) : (
            <>
              <div className="modal-header">
                <h5 className="modal-title">
                  <span className={`icon card-type ${definition.type?.toLowerCase().replace(/ /g, '_').replace('/', ' ')}`} />{' '}
                  <span className="card-name">{definition.displayName}</span>
                </h5>
                <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
              </div>
              <div className="modal-body">
                <CardImage cardId={card.cardId ?? ''} secured={!!card.playtest} name={definition.displayName} />
                <div className="requirements d-flex justify-content-center gap-2">
                  {(definition.clans ?? []).map((clan) => (
                    <span key={clan} className={`clan ${clan.toLowerCase().replace(/ /g, '_')}`} title={clan} />
                  ))}
                  {definition.cost && <span>Cost: {definition.cost}</span>}
                </div>
                {definition.preamble && <p className="mb-2">{definition.preamble}</p>}
                <div className="d-grid gap-2">
                  {(definition.modes ?? []).map((mode, i) =>
                    definition.multiMode ? (
                      <button
                        key={i}
                        type="button"
                        className={`btn mb-2 ${selected.has(i) ? 'btn-dark' : 'btn-outline-dark'}`}
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
                      <button key={i} type="button" className="btn btn-outline-dark mb-2" onClick={() => play(mode)}>
                        {(mode.disciplines ?? []).map((d) => (
                          <span key={d} className={`icon ${d}`} />
                        ))}{' '}
                        <span dangerouslySetInnerHTML={{ __html: mode.text }} />
                      </button>
                    ),
                  )}
                </div>
                {definition.multiMode && (
                  <div className="mt-2">
                    <hr />
                    <button type="button" className="btn btn-outline-secondary mb-2" disabled={selected.size < 1} onClick={playMulti}>
                      {selected.size < 1 ? 'Select one or more disciplines' : 'Play'}
                    </button>
                  </div>
                )}
                <div className="d-flex justify-content-center align-items-center mt-2">
                  <button type="button" className="btn btn-outline-danger mx-1" title="Discard" onClick={() => discard(false)}>
                    <i className="bi bi-trash" /> Discard
                  </button>
                  <button type="button" className="btn btn-outline-danger mx-1" title="Discard and replace" onClick={() => discard(true)}>
                    <i className="bi bi-recycle" /> Discard + Draw
                  </button>
                </div>
                <div className="input-group mt-2">
                  <label className="input-group-text">
                    <i className="bi bi-tag" />
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Add a label for all players to see."
                    value={label}
                    onChange={(e) => setLabel(e.target.value)}
                    onBlur={updateLabel}
                  />
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
