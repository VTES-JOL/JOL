import type { CardSnapshot, GameSnapshot } from '../../api/types';

// A card's "coordinate" (e.g. "3" or "3.1" for a nested card) is a 1-based,
// dot-separated positional index into RegionSnapshot.cards/CardSnapshot.cards
// — never sent by the server (see GameSnapshotFactory), computed here purely
// from array position, exactly like CardDetail.buildAttributes' varStatus
// counter did in card.jsp. Legacy commands (e.g. `move ready 3.1 hand`)
// address cards by this coordinate, not by CardSnapshot.id.
export function findCardByCoordinate(game: GameSnapshot, playerName: string, regionType: string, coordinate: string): CardSnapshot | null {
  const player = game.players.find((p) => p.name === playerName);
  const region = player?.regions.find((r) => r.type === regionType);
  return region ? walkCoordinate(region.cards, coordinate) : null;
}

// Like findCardByCoordinate but keyed the way a command string addresses a card
// (player's *first name* + region *commandKey*, e.g. `lock Player1 ready 3.1`) —
// used by GamePage's optimistic-apply to locate the card a command targets.
export function findCardByCommandCoordinate(
  game: GameSnapshot,
  playerFirstName: string,
  regionCommandKey: string,
  coordinate: string,
): CardSnapshot | null {
  const player = game.players.find((p) => p.name.split(' ')[0] === playerFirstName);
  const region = player?.regions.find((r) => r.commandKey === regionCommandKey);
  return region ? walkCoordinate(region.cards, coordinate) : null;
}

function walkCoordinate(cards: CardSnapshot[], coordinate: string): CardSnapshot | null {
  let list = cards;
  let card: CardSnapshot | null = null;
  for (const part of coordinate.split('.')) {
    card = list[Number(part) - 1] ?? null;
    if (!card) return null;
    list = card.cards ?? [];
  }
  return card;
}
