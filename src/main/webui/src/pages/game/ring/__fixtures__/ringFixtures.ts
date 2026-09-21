// A turn-9, 5-player board built from the same real cards the design prototype
// used (see docs/reviews/game-ring-react-port-plan.md). Storybook + tests only.

import type { CardSnapshot, PlayerSnapshot } from '../../../../api/types';
import { card, gun, hiddenCard, minion, player, region, retainer } from '../../__fixtures__/gameFixtures';

interface VOpts {
  locked?: boolean;
  contested?: boolean;
  title?: string;
  att?: ('equip' | 'retainer')[];
}

const V = (name: string, capacity: number, blood: number, o: VOpts = {}): CardSnapshot =>
  minion(name, {
    counters: blood,
    capacity,
    locked: !!o.locked,
    contested: !!o.contested,
    label: o.title,
    disciplines: [],
    cards: (o.att ?? []).map((a) => (a === 'equip' ? gun() : retainer())),
  });

export const ally = (name: string, life: number): CardSnapshot =>
  card(name, { typeClass: 'ally', minion: true, hasLife: true, hasBlood: false, counters: life });

export const location = (name: string): CardSnapshot => card(name, { typeClass: 'master' });

/** Uncontrolled vampire the viewer can read. */
export const uncontrolled = (name: string, capacity: number, blood: number): CardSnapshot =>
  minion(name, { counters: blood, capacity });

/** Uncontrolled vampire an opponent cannot read: only the blood count shows. */
export const uncontrolledHidden = (blood: number): CardSnapshot => hiddenCard({ counters: blood });

function seat(
  name: string,
  pool: number,
  ready: CardSnapshot[],
  torpor: CardSnapshot[],
  unc: CardSnapshot[],
  opts: Partial<PlayerSnapshot> = {},
): PlayerSnapshot {
  return player(name, {
    pool,
    regions: [region('READY', ready), region('TORPOR', torpor), region('UNCONTROLLED', unc)],
    ...opts,
  });
}

export const TURN9_SEATING = ['Marcus Kane', 'Sable Voss', 'Lysette Marchetti', 'Ilya Rostova', 'The Baron'];
export const TURN9_VIEWER = 'Lysette Marchetti';

/** All five seats, in seating order (each seat's prey is the next). */
export function turn9Players(): PlayerSnapshot[] {
  return [
    seat(
      'Marcus Kane', 21,
      [
        V('Mustafa, The Heir', 6, 5, { title: 'Prince', att: ['equip', 'retainer'] }),
        V('Naomi Stewart', 5, 2, { locked: true }),
        V('Emily Carson', 5, 4, { title: 'Primogen', contested: true }),
        V('Amelia Locke', 6, 6, { locked: true, att: ['equip'] }),
        ally('Carlton Van Wyk', 2),
        location('Powerbase: Montreal'),
      ],
      [V('Hamid Mansour', 4, 0)],
      [uncontrolled('Louis Fortier', 5, 3), uncontrolled('Joseph DiGiaccomo', 6, 1), uncontrolledHidden(0), uncontrolledHidden(4)],
      { edge: true },
    ),
    seat(
      'Sable Voss', 7,
      [
        V('Vasily', 6, 4, { title: 'Prince', att: ['equip'] }),
        V('Kateline Nadasdy', 7, 7, { att: ['retainer'] }),
        V('Masdela', 5, 1, { locked: true }),
        V('Andre LeRoux', 3, 3),
        V('Mkhokheli', 6, 2, { title: 'Prince', locked: true, contested: true }),
        location("Zillah's Valley"),
      ],
      [],
      [uncontrolledHidden(2), uncontrolledHidden(0), uncontrolledHidden(5)],
    ),
    seat(
      'Lysette Marchetti', 15,
      [
        V('Ayelech', 7, 6, { title: 'Prince', locked: true, att: ['equip'] }),
        V('Trevon Parker', 6, 6),
        V('Lord Ephraim Wainwright', 6, 3),
        V('Zane', 5, 2, { title: 'Primogen', locked: true, contested: true }),
        V('Monica Chang', 3, 3),
        ally('War Ghoul', 5),
      ],
      [V('Tarautas', 4, 0)],
      [uncontrolled('Hector Trelane', 5, 3), uncontrolled('Sri Sansa', 5, 0), uncontrolled('Ayse Dhanial', 4, 2), uncontrolled('Abraham DuSable', 8, 1)],
    ),
    seat(
      'Ilya Rostova', 12,
      [
        V('Belinde', 6, 5, { title: 'Prince', locked: true }),
        V('Benjamin Rose', 7, 7, { title: 'Prince', att: ['retainer'] }),
        V('Slag', 4, 4),
        V('Toby', 5, 1, { locked: true }),
        V('Larissa Moreira', 6, 3, { title: 'Primogen', contested: true }),
        V('Federico di Padua', 7, 2, { locked: true }),
      ],
      [],
      [uncontrolledHidden(2), uncontrolledHidden(0), uncontrolledHidden(5)],
    ),
    seat(
      'The Baron', 22,
      [
        V('Tomaine', 6, 6, { title: 'Primogen' }),
        V('Octane', 6, 4),
        V('Atiena', 6, 2, { title: 'Baron', locked: true }),
        V('Theo Bell', 8, 8, { att: ['equip', 'equip'] }),
        location('Heidelberg Castle, Germany'),
      ],
      [V('Ariane', 3, 0), V('Garret', 3, 0)],
      [uncontrolledHidden(1), uncontrolledHidden(0), uncontrolledHidden(3), uncontrolledHidden(2)],
    ),
  ];
}

/** The first `n` seats of the turn-9 board, for 2–4 player stories (seating trimmed to match). */
export function turn9Table(n: 2 | 3 | 4 | 5): { players: PlayerSnapshot[]; seating: string[] } {
  const players = turn9Players().slice(0, n);
  return { players, seating: players.map((p) => p.name) };
}
