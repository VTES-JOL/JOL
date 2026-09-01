import { useEffect, useState, type MouseEvent, type ReactNode } from 'react';
import {
  ArrowLeftCircle,
  ArrowRightCircle,
  Ban,
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

function buttonVisible(btn: ButtonConfig, region: string, locked: boolean, contested: boolean, isOwner: boolean, isChild: boolean, minion: boolean): boolean {
  if (!btn.regions.includes(region)) return false;
  if (btn.lockState && btn.lockState !== (locked ? 'locked' : 'unlocked')) return false;
  if (btn.contested !== undefined && btn.contested !== contested) return false;
  if (btn.ownerOnly && !isOwner) return false;
  if (btn.topLevelOnly && isChild) return false;
  if (btn.minionOnly && !minion) return false;
  if (btn.nonMinionOnly && minion) return false;
  return true;
}

function InlinePicker({
  value,
  values,
  kind,
  resolveName,
  onChange,
}: {
  value: string | null | undefined;
  values: string[];
  kind: 'clan' | 'path' | 'sect';
  resolveName: (value?: string | null) => string | undefined;
  onChange: (newKey: string) => void;
}) {
  const [editing, setEditing] = useState(false);
  const display = resolveName(value) ?? 'None';

  if (editing) {
    return (
      <Select
        srLabel={kind}
        size="sm"
        className="w-auto ms-2"
        autoFocus
        defaultValue={nameToKey(display)}
        onBlur={() => setEditing(false)}
        onChange={(e) => {
          onChange(e.target.value);
          setEditing(false);
        }}
      >
        {values.map((v) => (
          <option key={v} value={nameToKey(v)}>
            {v}
          </option>
        ))}
      </Select>
    );
  }

  return display.toLowerCase() === 'none' ? (
    <span className="text-ink-muted mx-1 cursor-pointer" title="None" onClick={() => setEditing(true)}>
      <Ban size={14} />
    </span>
  ) : (
    <span
      className={`${kind} ${nameToKey(display)} mx-1 cursor-pointer`}
      title={display}
      onClick={() => setEditing(true)}
    />
  );
}

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
  const hasVotes = !!card.votes && card.votes !== '0';
  const counterText = `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`;
  const counterStyle = COUNTER_STYLE(!!card.hasLife, !!card.hasBlood, card.capacity ?? 0, OTHER_VISIBLE_REGIONS.has(ctx.regionType));

  const doAction = (action: (ctx: TableCardContext) => Submission, close = true) => {
    onSubmit(action(ctx));
    if (close) onClose();
  };

  const vialClick = (e: MouseEvent<HTMLElement>, onUp: () => void, onDown: () => void) => {
    const bounds = e.currentTarget.getBoundingClientRect();
    if (e.clientX - bounds.left >= bounds.width / 2) onUp();
    else onDown();
  };

  return (
    <Modal onClose={onClose} bodyClassName="flex flex-col min-h-0 overflow-y-auto flex-1">
      <>
          <div className="flex justify-between items-center px-2 py-1 border-b border-line bg-panel/45">
            <span className="flex items-center">
              {minion && (
                <InlinePicker
                  value={card.clan}
                  values={CLANS}
                  kind="clan"
                  resolveName={clanName}
                  onChange={(key) => doAction((c) => cardActions.clan(c, key), false)}
                />
              )}
              <span className="card-name text-lg">{cardName}</span>
              {hasVotes && <span className={`${PILL} bg-gold text-surface mx-2`}>{card.votes}</span>}
            </span>
            <span className="flex items-center">
              {minion && (
                <InlinePicker
                  value={card.path}
                  values={PATHS}
                  kind="path"
                  resolveName={pathName}
                  onChange={(key) => doAction((c) => cardActions.path(c, key), false)}
                />
              )}
              {minion && (
                <InlinePicker
                  value={card.sect}
                  values={SECTS}
                  kind="sect"
                  resolveName={sectName}
                  onChange={(key) => doAction((c) => cardActions.sect(c, key), false)}
                />
              )}
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
          <div className="flex flex-col items-center p-3 border-t border-line">
            {(showCounters || showTransfers) && (
              <div className="flex justify-between items-center gap-1 rounded-full bg-blood/15 p-1 text-lg">
                {showCounters && (
                  <div
                    className={`${PILL} ${counterStyle} gap-1 text-base cursor-pointer`}
                    title="Counters; click right side to increase, left to decrease"
                    onClick={(e) => vialClick(e, () => doAction(cardActions.addCounter, false), () => doAction(cardActions.removeCounter, false))}
                  >
                    <Minus size={13} />
                    {counterText}
                    <Plus size={13} />
                  </div>
                )}
                {showTransfers && (
                  <>
                    <div className="text-2xl cursor-pointer px-1" title="Transfer one pool to this card" onClick={() => doAction(cardActions.transferToCard, false)}>
                      &#9668;
                    </div>
                    <div className="text-2xl cursor-pointer px-1" title="Transfer one blood to your pool" onClick={() => doAction(cardActions.transferToPool, false)}>
                      &#9658;
                    </div>
                    <div className={`${PILL} bg-blood text-surface text-base`}>{ctx.controllerPool} pool</div>
                  </>
                )}
              </div>
            )}
            <div className="mt-2 flex flex-wrap justify-center">
              {BUTTONS.map((btn) =>
                buttonVisible(btn, ctx.regionCommandKey, locked, contested, isOwner, ctx.isChild, minion) ? (
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
