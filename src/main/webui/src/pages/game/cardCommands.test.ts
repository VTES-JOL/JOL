import { describe, expect, it } from 'vitest';
import type { CardSnapshot } from '../../api/types';
import { buildDiscardCommand, buildHandLabelCommand, buildPlayCommand, cardActions, needsTargetPicker, type TableCardContext } from './cardCommands';

const card: CardSnapshot = { id: 'c1', visible: true, counters: 0, cardId: '200689', name: 'Jennie "Cassie247" Orne' };

function makeCtx(overrides: Partial<TableCardContext> = {}): TableCardContext {
  return {
    controller: 'Player1',
    controllerPool: 30,
    regionType: 'READY',
    regionCommandKey: 'ready',
    coordinate: '1',
    card,
    isChild: false,
    controlledByViewer: true,
    ...overrides,
  };
}

describe('cardActions', () => {
  it('lock builds a plain lock command with no chat', () => {
    expect(cardActions.lock(makeCtx())).toEqual({ command: 'lock Player1 ready 1' });
  });

  it('unlock builds a plain unlock command', () => {
    expect(cardActions.unlock(makeCtx())).toEqual({ command: 'unlock Player1 ready 1' });
  });

  it('hide / reveal build a plain flip command with the controller and position', () => {
    expect(cardActions.hide(makeCtx())).toEqual({ command: 'hide Player1 ready 1' });
    expect(cardActions.reveal(makeCtx({ regionCommandKey: 'torpor', coordinate: '2' }))).toEqual({ command: 'reveal Player1 torpor 2' });
  });

  it('bleed locks the card and echoes "Bleed" to chat', () => {
    expect(cardActions.bleed(makeCtx())).toEqual({ command: 'lock Player1 ready 1', chat: 'Bleed' });
  });

  it('hunt locks the card and echoes "Hunt" to chat', () => {
    expect(cardActions.hunt(makeCtx())).toEqual({ command: 'lock Player1 ready 1', chat: 'Hunt' });
  });

  it('goAnarch locks the card and echoes "Go anarch" to chat', () => {
    expect(cardActions.goAnarch(makeCtx())).toEqual({ command: 'lock Player1 ready 1', chat: 'Go anarch' });
  });

  it('leaveTorpor locks the card and echoes "Leave Torpor" to chat', () => {
    expect(cardActions.leaveTorpor(makeCtx())).toEqual({ command: 'lock Player1 ready 1', chat: 'Leave Torpor' });
  });

  it('contest(false) sets the contest, contest(true) clears it', () => {
    expect(cardActions.contest(makeCtx(), false)).toEqual({ command: 'contest Player1 ready 1' });
    expect(cardActions.contest(makeCtx(), true)).toEqual({ command: 'contest Player1 ready 1 clear' });
  });

  it('torpor sends the card to its controller\'s torpor region', () => {
    expect(cardActions.torpor(makeCtx())).toEqual({ command: 'move Player1 ready 1 Player1 torpor' });
  });

  it('burn burns without a controller name', () => {
    expect(cardActions.burn(makeCtx())).toEqual({ command: 'burn Player1 ready 1' });
  });

  it('influence uses just the coordinate, no controller/region', () => {
    expect(cardActions.influence(makeCtx({ coordinate: '2' }))).toEqual({ command: 'influence 2' });
  });

  it('block is chat-only, no command; card name is bracketed for link resolution', () => {
    expect(cardActions.block('Some Vampire')).toEqual({ chat: '[Some Vampire] blocks' });
  });

  it('rescue is chat-only; rescuer and target are both bracketed', () => {
    expect(cardActions.rescue('Nosferatu', 'Sasha Miklos')).toEqual({
      chat: '[Nosferatu] attempts to rescue [Sasha Miklos]',
    });
  });

  it('moveHand/moveReady/moveUncontrolled move by region+coordinate only (no controller name)', () => {
    const ctx = makeCtx({ regionCommandKey: 'ashheap', coordinate: '3' });
    expect(cardActions.moveHand(ctx)).toEqual({ command: 'move ashheap 3 hand' });
    expect(cardActions.moveReady(ctx)).toEqual({ command: 'move ashheap 3 ready' });
    expect(cardActions.moveUncontrolled(ctx)).toEqual({ command: 'move ashheap 3 inactive' });
  });

  it('moveLibrary appends "top" only when requested', () => {
    const ctx = makeCtx({ regionCommandKey: 'ashheap', coordinate: '3' });
    expect(cardActions.moveLibrary(ctx, false)).toEqual({ command: 'move ashheap 3 library' });
    expect(cardActions.moveLibrary(ctx, true)).toEqual({ command: 'move ashheap 3 library top' });
  });

  it('removeFromGame uses the rfg keyword with controller name', () => {
    expect(cardActions.removeFromGame(makeCtx())).toEqual({ command: 'rfg Player1 ready 1' });
  });

  it('movePredator/movePrey include the controller\'s first name', () => {
    expect(cardActions.movePredator(makeCtx())).toEqual({ command: 'move Player1 ready 1 predator' });
    expect(cardActions.movePrey(makeCtx())).toEqual({ command: 'move Player1 ready 1 prey' });
  });

  it('addCounter/removeCounter use the blood keyword with controller name', () => {
    expect(cardActions.addCounter(makeCtx())).toEqual({ command: 'blood Player1 ready 1 +1' });
    expect(cardActions.removeCounter(makeCtx())).toEqual({ command: 'blood Player1 ready 1 -1' });
  });

  it('transferToCard/transferToPool omit the controller name', () => {
    expect(cardActions.transferToCard(makeCtx())).toEqual({ command: 'transfer ready 1 +1' });
    expect(cardActions.transferToPool(makeCtx())).toEqual({ command: 'transfer ready 1 -1' });
  });

  it('label sets a free-text label on the card', () => {
    expect(cardActions.label(makeCtx(), 'my note')).toEqual({ command: 'label Player1 ready 1 my note' });
  });

  it('clan/path take only the first underscore-delimited segment of the key', () => {
    expect(cardActions.clan(makeCtx(), 'brujah_antitribu')).toEqual({ command: 'clan Player1 ready 1 brujah' });
    expect(cardActions.path(makeCtx(), 'death_and_the_soul')).toEqual({ command: 'path Player1 ready 1 death' });
  });

  it('sect uses the full key verbatim (unlike clan/path)', () => {
    expect(cardActions.sect(makeCtx(), 'independent')).toEqual({ command: 'sect Player1 ready 1 independent' });
  });

  it('derives the controller\'s first name from a multi-word data-player value', () => {
    expect(cardActions.lock(makeCtx({ controller: 'Player1 (2)' }))).toEqual({ command: 'lock Player1 ready 1' });
  });
});

describe('buildPlayCommand', () => {
  const ctx = { regionType: 'HAND', regionCommandKey: 'hand', coordinate: '3' };

  it('builds a minimal play command with a trailing draw by default', () => {
    expect(buildPlayCommand(ctx, null, null, null, false)).toBe('play hand 3 draw');
  });

  it('omits draw when doNotReplace is true', () => {
    expect(buildPlayCommand(ctx, null, null, null, true)).toBe('play hand 3');
  });

  it('includes disciplines with an "@" separator, comma-joined', () => {
    expect(buildPlayCommand(ctx, ['aus', 'dom'], null, null, true)).toBe('play hand 3 @ aus,dom');
  });

  it('ignores an empty disciplines array', () => {
    expect(buildPlayCommand(ctx, [], null, null, true)).toBe('play hand 3');
  });

  it('appends a fixed suffix for READY_REGION/REMOVE_FROM_GAME/INACTIVE_REGION targets', () => {
    expect(buildPlayCommand(ctx, null, 'READY_REGION', null, true)).toBe('play hand 3 ready');
    expect(buildPlayCommand(ctx, null, 'REMOVE_FROM_GAME', null, true)).toBe('play hand 3 rfg');
    expect(buildPlayCommand(ctx, null, 'INACTIVE_REGION', null, true)).toBe('play hand 3 inactive');
  });

  it('appends the resolved target-picker string verbatim when present', () => {
    expect(buildPlayCommand(ctx, null, 'MINION_YOU_CONTROL', 'Player2 ready 1', true)).toBe('play hand 3 Player2 ready 1');
  });

  it('combines disciplines, a fixed target suffix, and draw together', () => {
    expect(buildPlayCommand(ctx, ['for'], 'READY_REGION', null, false)).toBe('play hand 3 @ for ready draw');
  });

  it('never appends draw when the card is played from a table region, not hand', () => {
    const tableCtx = { regionType: 'READY', regionCommandKey: 'ready', coordinate: '2' };
    expect(buildPlayCommand(tableCtx, null, null, null, false)).toBe('play ready 2');
    expect(buildPlayCommand(tableCtx, ['aus'], 'READY_REGION', null, false)).toBe('play ready 2 @ aus ready');
  });
});

describe('buildDiscardCommand', () => {
  it('discards without drawing by default', () => {
    expect(buildDiscardCommand({ regionType: 'HAND', regionCommandKey: 'hand', coordinate: '4' }, false)).toBe('discard 4');
  });

  it('appends draw when replace is true', () => {
    expect(buildDiscardCommand({ regionType: 'HAND', regionCommandKey: 'hand', coordinate: '4' }, true)).toBe('discard 4 draw');
  });
});

describe('buildHandLabelCommand', () => {
  it('hardcodes "hand" as the region regardless of caller intent (legacy quirk)', () => {
    expect(buildHandLabelCommand('Player1', '2', 'a note')).toBe('label Player1 hand 2 a note');
  });
});

describe('needsTargetPicker', () => {
  it('is true only for MINION_YOU_CONTROL/SELF/SOMETHING', () => {
    expect(needsTargetPicker('MINION_YOU_CONTROL')).toBe(true);
    expect(needsTargetPicker('SELF')).toBe(true);
    expect(needsTargetPicker('SOMETHING')).toBe(true);
  });

  it('is false for other targets and null', () => {
    expect(needsTargetPicker('READY_REGION')).toBe(false);
    expect(needsTargetPicker('REMOVE_FROM_GAME')).toBe(false);
    expect(needsTargetPicker('INACTIVE_REGION')).toBe(false);
    expect(needsTargetPicker(null)).toBe(false);
  });
});
