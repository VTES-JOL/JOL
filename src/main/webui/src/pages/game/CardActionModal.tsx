import { useEffect, useState, type MouseEvent, type ReactNode } from 'react';
import type { CardDefinition } from '../../api/types';
import { fetchCardDefinition } from './cardDefinitions';
import { cardActions, type Submission, type TableCardContext } from './cardCommands';
import { CardImage } from './CardImage';
import { runRequest } from '../../api/mutate';
import { Modal } from '../../components/Modal';
import { COUNTER_STYLE, OTHER_VISIBLE_REGIONS } from './Card';

const CLANS = [
  'Abomination', 'Ahrimane', 'Akunanse', 'Baali', 'Banu Haqim', 'Blood Brother', 'Brujah', 'Brujah Antitribu',
  'Caitiff', 'Daughter of Cacophony', 'Gangrel', 'Gangrel Antitribu', 'Gargoyle', 'Giovanni', 'Guruhi', 'Harbinger of Skulls',
  'Ishtarri', 'Kiasyd', 'Lasombra', 'Malkavian', 'Malkavian Antitribu', 'Nagaraja', 'Nosferatu', 'Nosferatu Antitribu',
  'Hecata', 'Ministry', 'Osebo', 'Pander', 'Ravnos', 'Salubri', 'Salubri Antitribu', 'Samedi', 'Toreador', 'Toreador Antitribu',
  'Tremere', 'Tremere Antitribu', 'True Brujah', 'Tzimisce', 'Ventrue', 'Ventrue Antitribu', 'Avenger', 'Defender', 'Innocent',
  'Judge', 'Martyr', 'Redeemer', 'Visionary', 'None',
]; // eslint-disable-line prettier/prettier
const PATHS = ['Death and the Soul', 'Power and the Inner Voice', 'Cathari', 'Caine', 'None'];
const SECTS = ['Camarilla', 'Sabbat', 'Independent', 'Laibon', 'Anarch', 'None'];

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
  { key: 'lock', label: <><i className="bi bi-lock" /> Lock</>, title: 'Lock', regions: ['ready', 'torpor'], lockState: 'unlocked', action: cardActions.lock },
  { key: 'unlock', label: <span style={{ transform: 'rotate(-90deg)' }}><i className="bi bi-unlock" /> Unlock</span>, title: 'Unlock', regions: ['ready', 'torpor'], lockState: 'locked', action: cardActions.unlock },
  { key: 'block', label: <><i className="bi bi-shield" /> Block</>, title: 'Block', regions: ['ready'], topLevelOnly: true, ownerOnly: true, minionOnly: true, action: (ctx) => cardActions.block(ctx.card.name ?? '') },
  { key: 'torpor', label: 'Send to Torpor', title: 'Torpor', regions: ['ready'], topLevelOnly: true, minionOnly: true, action: cardActions.torpor },
  { key: 'burn', label: <><i className="bi bi-fire" /> Burn</>, title: 'Burn', regions: ['ready', 'torpor', 'inactive'], action: cardActions.burn },
  { key: 'move-hand', label: 'Move to Hand', title: 'Move to Hand', regions: ['ashheap'], ownerOnly: true, action: cardActions.moveHand },
  { key: 'move-library', label: 'Move to Library', title: 'Move to bottom of Library', regions: ['ashheap'], ownerOnly: true, nonMinionOnly: true, action: (ctx) => cardActions.moveLibrary(ctx, false) },
  { key: 'move-library-top', label: 'Move to Library (Top)', title: 'Move to top of Library', regions: ['ashheap'], ownerOnly: true, nonMinionOnly: true, action: (ctx) => cardActions.moveLibrary(ctx, true) },
  { key: 'move-uncontrolled', label: 'Move to Uncontrolled', title: 'Move to uncontrolled', regions: ['ashheap'], ownerOnly: true, minionOnly: true, action: cardActions.moveUncontrolled },
  { key: 'rfg', label: <><i className="bi bi-escape" /> Remove from Game</>, title: 'Remove from game', regions: ['ready', 'ashheap', 'inactive'], ownerOnly: true, action: cardActions.removeFromGame },
  { key: 'move-predator', label: <><i className="bi bi-arrow-left-circle" /> Move to Predator</>, title: 'Move to Predator', regions: ['ready'], action: cardActions.movePredator },
  { key: 'move-prey', label: <><i className="bi bi-arrow-right-circle" /> Move to Prey</>, title: 'Move to Prey', regions: ['ready'], action: cardActions.movePrey },
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
  onChange,
}: {
  value: string | null | undefined;
  values: string[];
  kind: 'clan' | 'path' | 'sect';
  onChange: (newKey: string) => void;
}) {
  const [editing, setEditing] = useState(false);
  const display = value && value !== 'NONE' ? value : 'None';

  if (editing) {
    return (
      <select
        className="form-select form-select-sm ms-2"
        style={{ width: 'auto' }}
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
      </select>
    );
  }

  return display.toLowerCase() === 'none' ? (
    <span className="text-muted mx-1" style={{ cursor: 'pointer' }} title="None" onClick={() => setEditing(true)}>
      <i className="bi bi-ban" />
    </span>
  ) : (
    <span
      className={`${kind} ${nameToKey(display)} mx-1`}
      style={{ cursor: 'pointer' }}
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
  const [definition, setDefinition] = useState<CardDefinition | null>(null);
  const [label, setLabel] = useState(card.label ?? '');

  useEffect(() => {
    setDefinition(null);
    runRequest(fetchCardDefinition(card.cardId ?? '', !!card.playtest), 'Failed to load card details', setDefinition);
  }, [card.cardId, card.playtest]);

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
    <Modal onClose={onClose} contentStyle={{ textAlign: 'center' }}>
      {!definition ? (
        <div style={{ height: '30vh' }} className="d-flex align-items-center justify-content-center">
          <h2>Loading...</h2>
        </div>
      ) : (
        <>
          <div className="modal-header py-1 px-2 justify-content-between align-items-center">
            <span className="d-flex align-items-center">
              {minion && (
                <InlinePicker
                  value={card.clan}
                  values={CLANS}
                  kind="clan"
                  onChange={(key) => doAction((c) => cardActions.clan(c, key), false)}
                />
              )}
              <span className="card-name fs-5">{definition.displayName}</span>
              {hasVotes && <span className="badge rounded-pill text-bg-warning mx-2">{card.votes}</span>}
            </span>
            <span className="d-flex align-items-center">
              {minion && (
                <InlinePicker
                  value={card.path}
                  values={PATHS}
                  kind="path"
                  onChange={(key) => doAction((c) => cardActions.path(c, key), false)}
                />
              )}
              {minion && (
                <InlinePicker
                  value={card.sect}
                  values={SECTS}
                  kind="sect"
                  onChange={(key) => doAction((c) => cardActions.sect(c, key), false)}
                />
              )}
              <button className="btn-close" title="Close" onClick={onClose} />
            </span>
          </div>
          <div className="modal-body">
            <CardImage cardId={card.cardId ?? ''} secured={!!card.playtest} name={definition.displayName} />
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
                onBlur={() => doAction((c) => cardActions.label(c, label), false)}
              />
            </div>
          </div>
          <div className="modal-footer d-flex flex-column flex-wrap justify-content-center">
            {(showCounters || showTransfers) && (
              <div className="d-flex justify-content-between fs-5 rounded-pill align-items-center bg-danger-subtle gap-1 p-1">
                {showCounters && (
                  <div
                    className={`badge rounded-pill ${counterStyle} fs-5 gap-1 d-flex align-items-center`}
                    title="Counters; click right side to increase, left to decrease"
                    style={{ cursor: 'pointer' }}
                    onClick={(e) => vialClick(e, () => doAction(cardActions.addCounter, false), () => doAction(cardActions.removeCounter, false))}
                  >
                    <i className="bi bi-dash-lg" />
                    {counterText}
                    <i className="bi bi-plus-lg" />
                  </div>
                )}
                {showTransfers && (
                  <>
                    <div className="fs-3" style={{ cursor: 'pointer' }} title="Transfer one pool to this card" onClick={() => doAction(cardActions.transferToCard, false)}>
                      &#9668;
                    </div>
                    <div className="fs-3" style={{ cursor: 'pointer' }} title="Transfer one blood to your pool" onClick={() => doAction(cardActions.transferToPool, false)}>
                      &#9658;
                    </div>
                    <div className="badge rounded-pill text-bg-danger fs-5">{ctx.controllerPool} pool</div>
                  </>
                )}
              </div>
            )}
            <div className="mt-2">
              {BUTTONS.map((btn) =>
                buttonVisible(btn, ctx.regionCommandKey, locked, contested, isOwner, ctx.isChild, minion) ? (
                  <button
                    key={btn.key}
                    type="button"
                    className="btn btn-outline-dark m-1"
                    title={btn.title}
                    onClick={() => doAction(btn.action)}
                  >
                    {btn.label}
                  </button>
                ) : null,
              )}
            </div>
          </div>
        </>
      )}
    </Modal>
  );
}
