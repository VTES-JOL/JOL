import { useMemo, type ReactNode } from 'react';
import type { GameStatusBean, PlayerRelationship, RegistrationStatus } from '../../../api/types';
import { GameList } from '../../lobby/GameList';
import { childrenOfType } from './mdxChildren';

export interface GameExampleProps {
  name: string;
  visibility?: 'PUBLIC' | 'PRIVATE';
  format?: string;
  relationship?: PlayerRelationship;
  registered?: number;
  total?: number;
  /** Days since the game's last activity — drives the "closes in N days" label. */
  updatedDaysAgo?: number;
}

// Marker only — GameListExample reads its props and never renders it
// directly. Exists as its own component so content authors write one
// <GameExample .../> tag per row, matching the Lobby section's authoring
// pattern for CardExample/CommandOption.
export function GameExample(_props: GameExampleProps) {
  return null;
}

// The Lobby's "My Games"/"Public Games" list, built from <GameExample> rows
// and rendered through the real `pages/lobby/GameList.tsx`.
export function GameListExample({ children }: { children: ReactNode }) {
  // Captured once per mount rather than read fresh in the map() below — the
  // exact instant doesn't matter for a "closes in N days" example, and
  // reading Date.now() during render is otherwise an impurity lint flags.
  const now = useMemo(() => Date.now(), []);

  const games: GameStatusBean[] = childrenOfType(children, GameExample).map((child, i) => {
    const p = child.props;
    const total = p.total ?? 0;
    const registered = p.registered ?? 0;
    const registrations: RegistrationStatus[] = Array.from({ length: total }, (_, idx) => ({
      player: `Player${idx + 1}`,
      playerId: `player-${idx + 1}`,
      gameName: p.name,
      registered: idx < registered,
      deckName: null,
      deckSummary: null,
      valid: true,
    }));

    return {
      name: p.name,
      gameId: `example-${i}`,
      gameStatus: 'Inviting',
      format: p.format ?? 'Standard',
      owner: '',
      visibility: p.visibility ?? 'PRIVATE',
      players: {},
      registrations,
      activePlayer: null,
      predator: null,
      prey: null,
      turn: null,
      round: 0,
      edge: null,
      seating: [],
      updated: p.updatedDaysAgo != null ? new Date(now - p.updatedDaysAgo * 86_400_000).toISOString() : null,
      playerRelationship: p.relationship ?? null,
    };
  });

  // Height follows the row count (a docs mock is 1–3 rows) rather than a fixed
  // 18rem box that left dead space under a short list; cap it so a longer
  // example still scrolls internally.
  return (
    <div className="my-3 flex flex-col" style={{ maxWidth: '26rem', maxHeight: '18rem' }}>
      <GameList games={games} selectedName={null} onSelect={() => {}} onNew={() => {}} />
    </div>
  );
}
