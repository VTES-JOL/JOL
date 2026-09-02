import { describe, expect, it } from 'vitest';
import type { CardSnapshot, GameSnapshot, PlayerSnapshot, RegionSnapshot } from '../../api/types';
import { findCardByCoordinate } from './coordinates';

function card(id: string, nested: CardSnapshot[] = []): CardSnapshot {
  return { id, visible: true, counters: 0, cardId: id, name: id, cards: nested };
}

function region(type: string, cards: CardSnapshot[]): RegionSnapshot {
  return { type, commandKey: type.toLowerCase(), label: type, simple: false, openHand: false, hiddenHand: false, cards };
}

function player(name: string, regions: RegionSnapshot[]): PlayerSnapshot {
  return { name, pool: 30, victoryPoints: 0, active: false, edge: false, pinged: false, regions };
}

function game(players: PlayerSnapshot[]): GameSnapshot {
  return {
    id: 'g1',
    name: 'Test Game',
    players,
    currentPlayer: players[0]?.name ?? '',
    edgePlayer: players[0]?.name ?? '',
    turn: '1',
    turnLabel: 'Turn 1',
    phase: 'Unlock',
    phases: ['Unlock'],
    turns: [],
    pingOptions: [],
    player: true,
    admin: false,
    judge: false,
    globalNotes: null,
    privateNotes: null,
    edgeColor: '#ffffff',
    edgeTextColor: 'black',
    status: null,
    stamp: '2026-01-01T00:00:00Z',
    judgeRequest: null,
  };
}

describe('findCardByCoordinate', () => {
  it('finds a top-level card by its 1-based index', () => {
    const g = game([player('Player1', [region('READY', [card('a'), card('b'), card('c')])])]);
    expect(findCardByCoordinate(g, 'Player1', 'READY', '2')?.id).toBe('b');
  });

  it('finds a nested card via a dot-separated coordinate', () => {
    const equipped = card('equipment');
    const g = game([player('Player1', [region('READY', [card('vampire', [equipped])])])]);
    expect(findCardByCoordinate(g, 'Player1', 'READY', '1.1')?.id).toBe('equipment');
  });

  it('resolves arbitrarily deep nesting', () => {
    const bloodCounter = card('blood-token');
    const equipped = card('equipment', [bloodCounter]);
    const g = game([player('Player1', [region('READY', [card('vampire', [equipped])])])]);
    expect(findCardByCoordinate(g, 'Player1', 'READY', '1.1.1')?.id).toBe('blood-token');
  });

  it('returns null for an unknown player', () => {
    const g = game([player('Player1', [region('READY', [card('a')])])]);
    expect(findCardByCoordinate(g, 'NoSuchPlayer', 'READY', '1')).toBeNull();
  });

  it('returns null for a region the player has nothing in', () => {
    const g = game([player('Player1', [region('READY', [card('a')])])]);
    expect(findCardByCoordinate(g, 'Player1', 'TORPOR', '1')).toBeNull();
  });

  it('returns null for an out-of-range top-level index', () => {
    const g = game([player('Player1', [region('READY', [card('a')])])]);
    expect(findCardByCoordinate(g, 'Player1', 'READY', '2')).toBeNull();
  });

  it('returns null for an out-of-range nested index', () => {
    const g = game([player('Player1', [region('READY', [card('vampire', [card('equipment')])])])]);
    expect(findCardByCoordinate(g, 'Player1', 'READY', '1.2')).toBeNull();
  });
});
