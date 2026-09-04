import { useEffect, useState, type ReactNode } from 'react';
import {
  ArrowLeftCircle,
  ArrowRightCircle,
  ChevronLeft,
  ChevronRight,
  Eye,
  EyeOff,
  Flame,
  Lock,
  LogOut,
  Minus,
  Plus,
  Shield,
  Tag,
  Unlock,
  X,
} from 'lucide-react';
import { cardActions, type Submission, type TableCardContext } from './cardCommands';
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

// 'None' is appended for the picker's clear option — CLAN/PATH/SECT
// themselves are the closed set of real values (mirroring the Java enums),
// same source InlinePicker's badges resolve their display name from below.
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

interface ButtonConfig {
  key: string;
  label: ReactNode;
  title: string;
  regions: string[];
  lockState?: 'locked' | 'unlocked';
  contested?: boolean;
  ownerOnly?: boolean;
  topLevelOnly?: boolean;
  minionOnly?: boolean;
  nonMinionOnly?: boolean;
  controllerOnly?: boolean; // ctx.controlledByViewer — the viewer owns this board
  faceDown?: boolean; // shown only when card.faceDown matches this
  action: (ctx: TableCardContext) => Submission;
}

// Mirrors card-modal.jsp's button list exactly, in the same order, with the
// same data-region/data-lock-state/data-contested/data-owner-only/
// data-top-level-only/data-minion-only/data-non-minion-only gating.
const BUTTONS: ButtonConfig[] = [
  { key: 'influence', label: 'Influence', title: 'Influence', regions: ['inactive'], topLevelOnly: true, ownerOnly: true, minionOnly: true, action: cardActions.influence },
  { key: 'bleed', label: 'Bleed', title: 'Bleed', regions: ['ready'], lockState: 'unlocked', topLevelOnly: true, ownerOnly: true, minionOnly: true, action: cardActions.bleed },
  { key: 'contest', label: 'Contest', title: 'Contest', regions: ['ready', 'torpor'], contested: false, action: (ctx) => cardActions.contest(ctx, false) },
  { key: 'clear-contest', label: 'Clear Contest', title: 'Clear Contest', regions: ['ready', 'torpor'], contested: true, action: (ctx) => cardActions.contest(ctx, true) },
  { key: 'hunt', label: 'Hunt', title: 'Hunt', regions: ['ready'], lockState: 'unlocked', topLevelOnly: true, ownerOnly: true, minionOnly: true, action: cardActions.hunt },
  { key: 'go-anarch', label: 'Go Anarch', title: 'Go Anarch', regions: ['ready'], lockState: 'unlocked', topLevelOnly: true, ownerOnly: true, minionOnly: true, action: cardActions.goAnarch },
  { key: 'leave-torpor', label: 'Leave Torpor', title: 'Leave Torpor', regions: ['torpor'], lockState: 'unlocked', topLevelOnly: true, ownerOnly: true, minionOnly: true, action: cardActions.leaveTorpor },
  { key: 'move-ready', label: 'Move to ready', title: 'Move to ready', regions: ['torpor'], topLevelOnly: true, ownerOnly: true, minionOnly: true, action: cardActions.moveReady },
  { key: 'lock', label: <><Lock size={13} /> Lock</>, title: 'Lock', regions: ['ready', 'torpor'], lockState: 'unlocked', action: cardActions.lock },
  { key: 'unlock', label: <><Unlock size={13} /> Unlock</>, title: 'Unlock', regions: ['ready', 'torpor'], lockState: 'locked', action: cardActions.unlock },
  { key: 'hide', label: <><EyeOff size={13} /> Turn Face Down</>, title: 'Turn this card face down — only you will see it', regions: ['ready', 'torpor', 'uncontrolled'], controllerOnly: true, faceDown: false, action: cardActions.hide },
  { key: 'reveal', label: <><Eye size={13} /> Reveal</>, title: 'Turn this card face up for everyone', regions: ['ready', 'torpor', 'uncontrolled'], controllerOnly: true, faceDown: true, action: cardActions.reveal },
  { key: 'block', label: <><Shield size={13} /> Block</>, title: 'Block', regions: ['ready'], topLevelOnly: true, ownerOnly: true, minionOnly: true, action: (ctx) => cardActions.block(ctx.card.name ?? '') },
  { key: 'torpor', label: 'Send to Torpor', title: 'Torpor', regions: ['ready'], topLevelOnly: true, minionOnly: true, action: cardActions.torpor },
  { key: 'burn', label: <><Flame size={13} /> Burn</>, title: 'Burn', regions: ['ready', 'torpor', 'inactive'], action: cardActions.burn },
  { key: 'move-hand', label: 'Move to Hand', title: 'Move to Hand', regions: ['ashheap'], ownerOnly: true, action: cardActions.moveHand },
  { key: 'move-library', label: 'Move to Library', title: 'Move to bottom of Library', regions: ['ashheap'], ownerOnly: true, nonMinionOnly: true, action: (ctx) => cardActions.moveLibrary(ctx, false) },
  { key: 'move-library-top', label: 'Move to Library (Top)', title: 'Move to top of Library', regions: ['ashheap'], ownerOnly: true, nonMinionOnly: true, action: (ctx) => cardActions.moveLibrary(ctx, true) },
  { key: 'move-uncontrolled', label: 'Move to Uncontrolled', title: 'Move to uncontrolled', regions: ['ashheap'], ownerOnly: true, minionOnly: true, action: cardActions.moveUncontrolled },
  { key: 'rfg', label: <><LogOut size={13} /> Remove from Game</>, title: 'Remove from game', regions: ['ready', 'ashheap', 'inactive'], ownerOnly: true, action: cardActions.removeFromGame },
  { key: 'move-predator', label: <><ArrowLeftCircle size={13} /> Move to Predator</>, title: 'Move to Predator', regions: ['ready'], action: cardActions.movePredator },
  { key: 'move-prey', label: <><ArrowRightCircle size={13} /> Move to Prey</>, title: 'Move to Prey', regions: ['ready'], action: cardActions.movePrey },
];

function buttonVisible(btn: ButtonConfig, region: string, locked: boolean, contested: boolean, isOwner: boolean, isChild: boolean, minion: boolean, faceDown: boolean, controlledByViewer: boolean): boolean {
  if (!btn.regions.includes(region)) return false;
  if (btn.lockState && btn.lockState !== (locked ? 'locked' : 'unlocked')) return false;
  if (btn.contested !== undefined && btn.contested !== contested) return false;
  if (btn.ownerOnly && !isOwner) return false;
  if (btn.topLevelOnly && isChild) return false;
  if (btn.minionOnly && !minion) return false;
  if (btn.nonMinionOnly && minion) return false;
  if (btn.controllerOnly && !controlledByViewer) return false;
  if (btn.faceDown !== undefined && btn.faceDown !== faceDown) return false;
  return true;
}

// Read-only clan/path/sect glyph shown in the modal header. Nothing renders
// for an unset value — the editable AttrRow below is where a blank one is
// filled in.
function AttrIcon({ kind, value, resolveName }: {
  kind: 'clan' | 'path' | 'sect';
  value: string | null | undefined;
  resolveName: (value?: string | null) => string | undefined;
}) {
  const display = resolveName(value);
  if (!display) return null;
  return <span className={`${kind} ${nameToKey(display)} mx-1`} title={display} />;
}

// One labelled dropdown in the attributes row. Always visible (unlike the old
// click-to-reveal InlinePicker), so it's clear these are editable fields, not
// decoration. Same command path as before (cardActions.clan/path/sect).
function AttrSelect({ label, value, values, resolveName, onChange }: {
  label: string;
  value: string | null | undefined;
  values: string[];
  resolveName: (value?: string | null) => string | undefined;
  onChange: (newKey: string) => void;
}) {
  const display = resolveName(value) ?? 'None';
  return (
    <Select
      label={label}
      size="sm"
      value={nameToKey(display)}
      onChange={(e) => onChange(e.target.value)}
    >
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

// Mirrors card-modal.jsp + showCardModal()/doCardCommand() and friends from
// card-modal.js — the core on-table card action modal used by every seated
// player (bleed/hunt/torpor/lock/unlock/burn/move/transfer/contest/...).
export function CardActionModal({
  ctx,
  viewerName,
  onSubmit,
  onClose,
}: {
  ctx: TableCardContext;
  viewerName: string | null;
  onSubmit: (submission: Submission) => void;
  onClose: () => void;
}) {
  const { card } = ctx;
  const cardName = card.name ?? '';
  const [label, setLabel] = useState(card.label ?? '');

  useEffect(() => setLabel(card.label ?? ''), [card.label]);

  const isOwner = ctx.controller.split(' ')[0] === viewerName;
  const minion = !!card.minion;
  const locked = !!card.locked;
  const contested = !!card.contested;
  const showCounters = ctx.regionCommandKey !== 'ashheap';
  const showTransfers = isOwner && minion && ctx.regionCommandKey !== 'ashheap';
  // Clan / path / sect are only editable for a minion you own that's in play.
  const showAttributes = minion && isOwner && ctx.regionCommandKey === 'ready';
  const hasVotes = !!card.votes && card.votes !== '0';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const counterStyle = COUNTER_STYLE(!!card.hasLife, !!card.hasBlood, card.capacity ?? 0, OTHER_VISIBLE_REGIONS.has(ctx.regionType));

  const doAction = (action: (ctx: TableCardContext) => Submission, close = true) => {
    onSubmit(action(ctx));
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
              <button
                type="button"
                title="Close"
                onClick={onClose}
                className="p-1 rounded hover:bg-hover text-ink-muted"
              >
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
                      <button
                        type="button"
                        aria-label="Remove a counter"
                        className={STEP_BTN}
                        onClick={() => doAction(cardActions.removeCounter, false)}
                      >
                        <Minus size={14} />
                      </button>
                      <span className={`${PILL} ${counterStyle} min-w-[3ch] justify-center text-base`}>{counterText}</span>
                      <button
                        type="button"
                        aria-label="Add a counter"
                        className={STEP_BTN}
                        onClick={() => doAction(cardActions.addCounter, false)}
                      >
                        <Plus size={14} />
                      </button>
                    </div>
                  </div>
                )}
                {showTransfers && (
                  <div className="flex items-center gap-2">
                    <span className="w-14 text-right text-xs text-ink-muted">Blood</span>
                    <button
                      type="button"
                      className={XFER_BTN}
                      title="Move one blood from your pool onto this card"
                      onClick={() => doAction(cardActions.transferToCard, false)}
                    >
                      <ChevronLeft size={14} /> Move here
                    </button>
                    <span className={`${PILL} bg-blood text-surface`}>{ctx.controllerPool} pool</span>
                    <button
                      type="button"
                      className={XFER_BTN}
                      title="Move one blood from this card back to your pool"
                      onClick={() => doAction(cardActions.transferToPool, false)}
                    >
                      To pool <ChevronRight size={14} />
                    </button>
                  </div>
                )}
              </div>
            )}
            {showAttributes && (
              <div className="grid w-full max-w-xs grid-cols-3 gap-2">
                <AttrSelect
                  label="Clan"
                  value={card.clan}
                  values={CLANS}
                  resolveName={clanName}
                  onChange={(key) => doAction((c) => cardActions.clan(c, key), false)}
                />
                <AttrSelect
                  label="Path"
                  value={card.path}
                  values={PATHS}
                  resolveName={pathName}
                  onChange={(key) => doAction((c) => cardActions.path(c, key), false)}
                />
                <AttrSelect
                  label="Sect"
                  value={card.sect}
                  values={SECTS}
                  resolveName={sectName}
                  onChange={(key) => doAction((c) => cardActions.sect(c, key), false)}
                />
              </div>
            )}
            <div className="flex flex-wrap justify-center">
              {BUTTONS.map((btn) =>
                buttonVisible(btn, ctx.regionCommandKey, locked, contested, isOwner, ctx.isChild, minion, !!card.faceDown, ctx.controlledByViewer) ? (
                  <button key={btn.key} type="button" className={ACTION_BTN} title={btn.title} onClick={() => doAction(btn.action)}>
                    {btn.label}
                  </button>
                ) : null,
              )}
            </div>
          </div>
      </>
    </Modal>
  );
}
