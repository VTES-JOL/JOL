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
  if (!region) return null;
  let list = region.cards;
  let card: CardSnapshot | null = null;
  for (const part of coordinate.split('.')) {
    const index = Number(part) - 1;
    card = list[index] ?? null;
    if (!card) return null;
    list = card.cards ?? [];
  }
  return card;
}
