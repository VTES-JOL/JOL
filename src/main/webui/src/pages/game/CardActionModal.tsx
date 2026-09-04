import { useEffect, useState } from 'react';
import { ChevronLeft, ChevronRight, Minus, Plus, Tag, X } from 'lucide-react';
import { cardActions, type Submission, type TableCardContext } from './cardCommands';
import { CARD_ACTIONS, GROUP_LABEL, GROUP_ORDER, actionAvailable, type ActionEnv } from './cardActionRegistry';
import { CardImage } from './CardImage';
import { Modal } from '../../components/ui/Modal';
import { Select } from '../../components/ui/Select';
import { COUNTER_STYLE, OTHER_VISIBLE_REGIONS } from './Card';
import { CLAN, resolveClan } from './Clan';
import { PATH, resolvePath } from './Path';
import { SECT, resolveSect } from './Sect';

const PILL = 'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium';
const ACTION_BTN =
  'm-1 inline-flex items-center gap-1 rounded border border-line-accent px-2.5 py-1 text-sm text-ink-secondary hover:bg-hover';
const ACTION_BTN_DANGER =
  'm-1 inline-flex items-center gap-1 rounded border border-blood/40 px-2.5 py-1 text-sm text-blood hover:bg-blood/10';

const CLANS = [...Object.values(CLAN), 'None'];
const PATHS = [...Object.values(PATH), 'None'];
const SECTS = [...Object.values(SECT), 'None'];

const clanName = (value?: string | null) => {
  const code = resolveClan(value);
  return code ? CLAN[code] : undefined;
};
const pathName = (value?: string | null) => {
  const code = resolvePath(value);
  return code ? PATH[code] : undefined;
};
const sectName = (value?: string | null) => {
  const code = resolveSect(value);
  return code ? SECT[code] : undefined;
};

function nameToKey(name: string): string {
  return name.toLowerCase().replace(/ /g, '_');
}

// Read-only clan/path/sect glyph shown in the modal header. Nothing renders
// for an unset value — the editable AttrSelect row below is where a blank one
// is filled in.
function AttrIcon({ kind, value, resolveName }: {
  kind: 'clan' | 'path' | 'sect';
  value: string | null | undefined;
  resolveName: (value?: string | null) => string | undefined;
}) {
  const display = resolveName(value);
  if (!display) return null;
  return <span className={`${kind} ${nameToKey(display)} mx-1`} title={display} />;
}

// One labelled dropdown in the attributes row.
function AttrSelect({ label, value, values, resolveName, onChange }: {
  label: string;
  value: string | null | undefined;
  values: string[];
  resolveName: (value?: string | null) => string | undefined;
  onChange: (newKey: string) => void;
}) {
  const display = resolveName(value) ?? 'None';
  return (
    <Select label={label} size="sm" value={nameToKey(display)} onChange={(e) => onChange(e.target.value)}>
      {values.map((v) => (
        <option key={v} value={nameToKey(v)}>
          {v}
        </option>
      ))}
    </Select>
  );
}

const STEP_BTN =
  'inline-flex h-6 w-6 items-center justify-center rounded-full border border-line-accent text-ink-secondary hover:bg-hover';
const XFER_BTN =
  'inline-flex items-center gap-1 rounded border border-line-accent px-2 py-1 text-sm text-ink-secondary hover:bg-hover';

// The on-table card action panel (card-modal.jsp's descendant). Opened by
// clicking a card in READY / TORPOR / UNCONTROLLED / ASH_HEAP. The action list
// itself lives in cardActionRegistry so the (phase 2) context menu shares it.
export function CardActionModal({
  ctx,
  viewerName,
  phase,
  onSubmit,
  onRequestTarget,
  onClose,
}: {
  ctx: TableCardContext;
  viewerName: string | null;
  phase: string;
  onSubmit: (submission: Submission) => void;
  onRequestTarget: (actionId: string, rescuerName: string) => void;
  onClose: () => void;
}) {
  const { card } = ctx;
  const cardName = card.name ?? '';
  const [label, setLabel] = useState(card.label ?? '');

  useEffect(() => setLabel(card.label ?? ''), [card.label]);

  // controller = the card sits on the viewer's board; cardOwner = the viewer
  // is its start-of-game owner. Most actions key off the board; only pulling a
  // card into the owner's private zones / influencing keys off ownership.
  const isController = ctx.controlledByViewer;
  const isCardOwner = !!card.owner && card.owner === viewerName;
  const minion = !!card.minion;
  const region = ctx.regionCommandKey;

  const showCounters = region !== 'ashheap';
  const showTransfers = isController && minion && region !== 'ashheap';
  const showAttributes = minion && isController && region === 'ready';
  const hasVotes = !!card.votes && card.votes !== '0';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const counterStyle = COUNTER_STYLE(!!card.hasLife, !!card.hasBlood, card.capacity ?? 0, OTHER_VISIBLE_REGIONS.has(ctx.regionType));

  const env: ActionEnv = {
    region,
    phase,
    controlledByViewer: isController,
    isCardOwner,
    isChild: ctx.isChild,
    minion,
    locked: !!card.locked,
    contested: !!card.contested,
    faceDown: !!card.faceDown,
  };

  const doAction = (build: (ctx: TableCardContext) => Submission, close = true) => {
    onSubmit(build(ctx));
    if (close) onClose();
  };

  return (
    <Modal onClose={onClose} bodyClassName="flex flex-col min-h-0 overflow-y-auto flex-1">
      <>
        <div className="flex justify-between items-center px-2 py-1 border-b border-line bg-panel/45">
          <span className="flex items-center">
            <AttrIcon kind="clan" value={card.clan} resolveName={clanName} />
            <span className="card-name text-lg">{cardName}</span>
            {hasVotes && <span className={`${PILL} bg-gold text-surface mx-2`}>{card.votes}</span>}
          </span>
          <span className="flex items-center">
            <AttrIcon kind="path" value={card.path} resolveName={pathName} />
            <AttrIcon kind="sect" value={card.sect} resolveName={sectName} />
            <button type="button" title="Close" onClick={onClose} className="p-1 rounded hover:bg-hover text-ink-muted">
              <X size={14} />
            </button>
          </span>
        </div>

        <div className="p-4 text-center">
          <CardImage cardId={card.cardId ?? ''} secured={!!card.playtest} name={cardName} />
          <div className="flex items-stretch mt-2">
            <span className="flex items-center rounded-l border border-r-0 border-line bg-panel px-2 text-ink-muted">
              <Tag size={14} />
            </span>
            <input
              type="text"
              className="flex-1 min-w-0 rounded-r border border-line bg-surface/70 px-2 py-1 text-sm text-ink outline-none focus:border-accent/60"
              placeholder="Add a label for all players to see."
              value={label}
              onChange={(e) => setLabel(e.target.value)}
              onBlur={() => doAction((c) => cardActions.label(c, label), false)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') e.currentTarget.blur();
              }}
            />
          </div>
        </div>

        <div className="flex flex-col items-center gap-3 p-3 border-t border-line">
          {(showCounters || showTransfers) && (
            <div className="flex flex-col items-center gap-2">
              {showCounters && (
                <div className="flex items-center gap-2">
                  <span className="w-14 text-right text-xs text-ink-muted">Counters</span>
                  <div className="inline-flex items-center gap-2 rounded-full bg-blood/15 p-1">
                    <button type="button" aria-label="Remove a counter" className={STEP_BTN} onClick={() => doAction(cardActions.removeCounter, false)}>
                      <Minus size={14} />
                    </button>
                    <span className={`${PILL} ${counterStyle} min-w-[3ch] justify-center text-base`}>{counterText}</span>
                    <button type="button" aria-label="Add a counter" className={STEP_BTN} onClick={() => doAction(cardActions.addCounter, false)}>
                      <Plus size={14} />
                    </button>
                  </div>
                </div>
              )}
              {showTransfers && (
                <div className="flex items-center gap-2">
                  <span className="w-14 text-right text-xs text-ink-muted">Blood</span>
                  <button type="button" className={XFER_BTN} title="Move one blood from your pool onto this card" onClick={() => doAction(cardActions.transferToCard, false)}>
                    <ChevronLeft size={14} /> Move here
                  </button>
                  <span className={`${PILL} bg-blood text-surface`}>{ctx.controllerPool} pool</span>
                  <button type="button" className={XFER_BTN} title="Move one blood from this card back to your pool" onClick={() => doAction(cardActions.transferToPool, false)}>
                    To pool <ChevronRight size={14} />
                  </button>
                </div>
              )}
            </div>
          )}

          {showAttributes && (
            <div className="grid w-full max-w-xs grid-cols-3 gap-2">
              <AttrSelect label="Clan" value={card.clan} values={CLANS} resolveName={clanName} onChange={(key) => doAction((c) => cardActions.clan(c, key), false)} />
              <AttrSelect label="Path" value={card.path} values={PATHS} resolveName={pathName} onChange={(key) => doAction((c) => cardActions.path(c, key), false)} />
              <AttrSelect label="Sect" value={card.sect} values={SECTS} resolveName={sectName} onChange={(key) => doAction((c) => cardActions.sect(c, key), false)} />
            </div>
          )}

          {GROUP_ORDER.map((group) => {
            const items = CARD_ACTIONS.filter((a) => a.group === group && actionAvailable(a, env));
            if (items.length === 0) return null;
            return (
              <div key={group} className="w-full">
                <div className="mb-1 text-center text-[0.7rem] font-semibold uppercase tracking-wide text-ink-muted">
                  {GROUP_LABEL[group]}
                </div>
                <div className="flex flex-wrap justify-center">
                  {items.map((a) => (
                    <button
                      key={a.id}
                      type="button"
                      className={group === 'remove' ? ACTION_BTN_DANGER : ACTION_BTN}
                      title={a.title}
                      onClick={() => {
                        if (a.requestTarget) {
                          onRequestTarget(a.id, cardName);
                          onClose();
                        } else {
                          doAction(a.build);
                        }
                      }}
                    >
                      {a.label}
                    </button>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      </>
    </Modal>
  );
}
