import type { CardModeTarget, CardSnapshot } from '../../api/types';

// Everything here mirrors card-modal.js's command-string builders exactly —
// see that file for the source of truth. `command` is submitted via the
// existing POST /game/{id}/view/submit `command` field, `chat` (if present)
// via that same request's `chat` field (sendCommand(command, message) there
// submits both together in one call).
export interface Submission {
  command?: string;
  chat?: string;
}

export interface TableCardContext {
  controller: string; // player.jsp's data-player, e.g. "Player1 (...)"
  controllerPool: number;
  regionType: string; // RegionSnapshot.type, e.g. "READY"
  regionCommandKey: string; // RegionSnapshot.commandKey, e.g. "ready"
  coordinate: string;
  card: CardSnapshot;
  isChild: boolean;
  controlledByViewer: boolean; // the viewer owns the board this card sits on
}

function controllerFirstName(ctx: TableCardContext): string {
  return ctx.controller.split(' ')[0];
}

// doCardCommand(commandKeyword, message, commandTail, closeModal, omitPlayer)
function doCardCommand(ctx: TableCardContext, keyword: string, tail = '', omitPlayer = false): string {
  const parts = [keyword];
  if (!omitPlayer) parts.push(controllerFirstName(ctx));
  parts.push(ctx.regionCommandKey, ctx.coordinate, tail);
  return parts.join(' ').replace(/\s+/g, ' ').trim();
}

export const cardActions = {
  lock: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'lock') }),
  unlock: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'unlock') }),
  hide: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'hide') }),
  reveal: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'reveal') }),
  contest: (ctx: TableCardContext, clear: boolean): Submission => ({ command: doCardCommand(ctx, 'contest', clear ? 'clear' : '') }),
  bleed: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'lock'), chat: 'Bleed' }),
  hunt: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'lock'), chat: 'Hunt' }),
  torpor: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'move', `${controllerFirstName(ctx)} torpor`) }),
  goAnarch: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'lock'), chat: 'Go anarch' }),
  leaveTorpor: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'lock'), chat: 'Leave Torpor' }),
  burn: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'burn') }),
  influence: (ctx: TableCardContext): Submission => ({ command: `influence ${ctx.coordinate}` }),
  block: (cardName: string): Submission => ({ chat: `${cardName} blocks` }),
  moveHand: (ctx: TableCardContext): Submission => ({ command: `move ${ctx.regionCommandKey} ${ctx.coordinate} hand` }),
  moveReady: (ctx: TableCardContext): Submission => ({ command: `move ${ctx.regionCommandKey} ${ctx.coordinate} ready` }),
  moveLibrary: (ctx: TableCardContext, top: boolean): Submission => ({
    command: `move ${ctx.regionCommandKey} ${ctx.coordinate} library${top ? ' top' : ''}`,
  }),
  moveUncontrolled: (ctx: TableCardContext): Submission => ({ command: `move ${ctx.regionCommandKey} ${ctx.coordinate} inactive` }),
  removeFromGame: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'rfg') }),
  movePredator: (ctx: TableCardContext): Submission => ({
    command: `move ${controllerFirstName(ctx)} ${ctx.regionCommandKey} ${ctx.coordinate} predator`,
  }),
  movePrey: (ctx: TableCardContext): Submission => ({
    command: `move ${controllerFirstName(ctx)} ${ctx.regionCommandKey} ${ctx.coordinate} prey`,
  }),
  addCounter: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'blood', '+1') }),
  removeCounter: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'blood', '-1') }),
  transferToCard: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'transfer', '+1', true) }),
  transferToPool: (ctx: TableCardContext): Submission => ({ command: doCardCommand(ctx, 'transfer', '-1', true) }),
  label: (ctx: TableCardContext, label: string): Submission => ({
    command: `label ${controllerFirstName(ctx)} ${ctx.regionCommandKey} ${ctx.coordinate} ${label}`.trim(),
  }),
  clan: (ctx: TableCardContext, newKey: string): Submission => ({
    command: `clan ${controllerFirstName(ctx)} ${ctx.regionCommandKey} ${ctx.coordinate} ${newKey.split('_')[0]}`.trim(),
  }),
  path: (ctx: TableCardContext, newKey: string): Submission => ({
    command: `path ${controllerFirstName(ctx)} ${ctx.regionCommandKey} ${ctx.coordinate} ${newKey.split('_')[0]}`.trim(),
  }),
  sect: (ctx: TableCardContext, newKey: string): Submission => ({
    command: `sect ${controllerFirstName(ctx)} ${ctx.regionCommandKey} ${ctx.coordinate} ${newKey}`.trim(),
  }),
};

export interface HandCardContext {
  regionType: string; // "HAND" | "RESEARCH"
  regionCommandKey: string; // "hand" | "research"
  coordinate: string;
}

// playCardCommand() — region/hand-index/disciplines/target come from the
// clicked mode button; `target` here is the mode's raw Target enum value,
// not the resolved target-picker string (`pickedTarget`), which is appended
// verbatim when present (mirrors modal.data('target') set by pickTarget()).
export function buildPlayCommand(
  ctx: HandCardContext,
  disciplines: string[] | null,
  target: CardModeTarget | null,
  pickedTarget: string | null,
  doNotReplace: boolean,
): string {
  let command = `play ${ctx.regionCommandKey} ${ctx.coordinate}`;
  if (disciplines && disciplines.length > 0) command += ` @ ${disciplines.join(',')}`;
  if (target === 'READY_REGION') command += ' ready';
  if (target === 'REMOVE_FROM_GAME') command += ' rfg';
  if (target === 'INACTIVE_REGION') command += ' inactive';
  if (pickedTarget) command += ` ${pickedTarget}`;
  // Only a card played from hand draws a replacement — a face-down card being
  // played "for real" from the table has no hand slot to refill.
  if (!doNotReplace && ctx.regionCommandKey === 'hand') command += ' draw';
  return command;
}

export function buildDiscardCommand(ctx: HandCardContext, replace: boolean): string {
  return `discard ${ctx.coordinate}${replace ? ' draw' : ''}`;
}

// updateNotesHand() hardcodes region "hand" regardless of the card's actual
// region (research included) — a legacy quirk replicated verbatim rather
// than fixed, since this migration doesn't change game-engine behavior.
export function buildHandLabelCommand(viewerName: string, coordinate: string, label: string): string {
  return `label ${viewerName} hand ${coordinate} ${label}`.trim();
}

export function needsTargetPicker(target: CardModeTarget | null): boolean {
  return target === 'MINION_YOU_CONTROL' || target === 'SELF' || target === 'SOMETHING';
}
