// Composable builders for the Storybook stories under src/pages/game/. Every
// game component takes some slice of the GET /game/{id}/view snapshot
// (net.deckserver.rest.bean.GameSnapshot) plus callbacks — these builders
// produce that slice with sensible defaults so a story only spells out the one
// state it is demonstrating.
//
// Not used by tests (those have their own inline fixtures); Storybook only.

import type {
  CardMode,
  CardSnapshot,
  ChatData,
  GameSnapshot,
  PendingAction,
  PlayerSnapshot,
  RegionSnapshot,
} from '../../../api/types';

let seq = 0;
/** Stable-ish unique instance id — card snapshots key on this. */
export const nextId = (prefix = 'c'): string => `${prefix}${++seq}`;

// ── Cards ──────────────────────────────────────────────────────────────────

export interface CardOpts extends Partial<CardSnapshot> {}

/** A visible library-style card (action / reaction / combat / master…). */
export function card(name: string, opts: CardOpts = {}): CardSnapshot {
  return {
    id: opts.id ?? nextId(),
    visible: true,
    counters: 0,
    cardId: '100000',
    name,
    typeClass: 'action',
    minion: false,
    ...opts,
  };
}

/** A visible crypt minion — capacity ring, disciplines, clan/sect. */
export function minion(name: string, opts: CardOpts = {}): CardSnapshot {
  return {
    id: opts.id ?? nextId('m'),
    visible: true,
    counters: opts.counters ?? 3,
    cardId: '200000',
    name,
    minion: true,
    capacity: 6,
    disciplines: ['AUS', 'dom', 'for', 'pre'],
    clan: 'Toreador',
    sect: 'Camarilla',
    hasBlood: true,
    typeClass: 'vampire',
    ...opts,
  };
}

/** A face-down card the viewer does NOT control — renders as a card back. */
export function faceDownBack(opts: CardOpts = {}): CardSnapshot {
  return {
    id: opts.id ?? nextId('fd'),
    visible: false,
    counters: 0,
    faceDown: true,
    ...opts,
  };
}

/** A hidden pile card (library / opponent hand) — the `*********` placeholder. */
export function hiddenCard(opts: CardOpts = {}): CardSnapshot {
  return { id: opts.id ?? nextId('h'), visible: false, counters: 0, ...opts };
}

/** A minion the viewer controls that is face-down but still legible to them. */
export function faceDownOwn(name: string, opts: CardOpts = {}): CardSnapshot {
  return minion(name, { faceDown: true, ...opts });
}

// Common attachments, for MinionTile / AttachedCards / Card stories.
export const gun = (opts: CardOpts = {}) =>
  card('.44 Magnum', { typeClass: 'equipment', cardId: '100001', ...opts });
export const retainer = (opts: CardOpts = {}) =>
  card('Raven Spy', { typeClass: 'retainer', counters: 1, cardId: '100002', ...opts });
export const bloodStack = (n: number, opts: CardOpts = {}) =>
  card('blood', { typeClass: 'blood', counters: n, cardId: '', name: undefined, ...opts });

/** Play-card-modal modes, for HAND-region CardSnapshot enrichment. */
export function mode(text: string, opts: Partial<CardMode> = {}): CardMode {
  return { disciplines: null, text, target: null, ...opts };
}

/** Enrich a hand card with the play-modal fields GameSnapshotFactory adds. */
export function handCard(name: string, opts: CardOpts = {}): CardSnapshot {
  return card(name, {
    typeClass: 'action',
    modes: [mode('Play for the base effect.')],
    multiMode: false,
    doNotReplace: false,
    cost: '1 pool',
    ...opts,
  });
}

// ── Regions ────────────────────────────────────────────────────────────────

const REGION_META: Record<string, { commandKey: string; label: string; simple: boolean }> = {
  READY: { commandKey: 'ready', label: 'Ready Region', simple: false },
  TORPOR: { commandKey: 'torpor', label: 'Torpor', simple: false },
  UNCONTROLLED: { commandKey: 'inactive', label: 'Uncontrolled Region', simple: false },
  RESEARCH: { commandKey: 'research', label: 'Research', simple: true },
  ASH_HEAP: { commandKey: 'ashheap', label: 'Ash Heap', simple: true },
  REMOVED_FROM_GAME: { commandKey: 'rfg', label: 'Removed from Game', simple: true },
  LIBRARY: { commandKey: 'library', label: 'Library', simple: true },
  CRYPT: { commandKey: 'crypt', label: 'Crypt', simple: false },
  HAND: { commandKey: 'hand', label: 'Hand', simple: true },
};

export function region(type: keyof typeof REGION_META, cards: CardSnapshot[], opts: Partial<RegionSnapshot> = {}): RegionSnapshot {
  const meta = REGION_META[type];
  return {
    type,
    commandKey: meta.commandKey,
    label: meta.label,
    simple: meta.simple,
    openHand: false,
    hiddenHand: false,
    cards,
    ...opts,
  };
}

// ── Players ────────────────────────────────────────────────────────────────

export function player(name: string, opts: Partial<PlayerSnapshot> = {}): PlayerSnapshot {
  return {
    name,
    pool: 30,
    victoryPoints: 0,
    active: false,
    edge: false,
    pinged: false,
    predator: null,
    prey: null,
    lastActionAt: null,
    exitKind: null,
    exitVpRecipient: null,
    regions: opts.regions ?? [
      region('READY', [minion('Muaziz, Archon of Ulugh Beg'), minion('Antara')]),
      region('TORPOR', []),
      region('UNCONTROLLED', [hiddenCard(), hiddenCard()]),
      region('RESEARCH', []),
      region('ASH_HEAP', [card('Govern the Unaligned'), card('Deflection')]),
      region('LIBRARY', Array.from({ length: 40 }, () => hiddenCard())),
      region('CRYPT', Array.from({ length: 8 }, () => hiddenCard())),
      region('HAND', [hiddenCard(), hiddenCard(), hiddenCard(), hiddenCard(), hiddenCard(), hiddenCard(), hiddenCard()]),
    ],
    ...opts,
  };
}

/** The viewer's own seat — an open hand of real, enriched cards. */
export function selfPlayer(name = 'Player1', opts: Partial<PlayerSnapshot> = {}): PlayerSnapshot {
  return player(name, {
    regions: [
      region('READY', [
        minion('Lucita, Anarch Sympathizer', { counters: 5, locked: true }),
        minion('Cristo', { counters: 2, cards: [gun(), retainer()] }),
      ]),
      region('TORPOR', [minion('Nakhthorheb', { counters: 0 })]),
      region('UNCONTROLLED', [minion('Anvil', { counters: 1, faceDown: false })]),
      region('RESEARCH', []),
      region('ASH_HEAP', [card('Govern the Unaligned'), card('Villein'), card('Deflection')]),
      region('LIBRARY', Array.from({ length: 32 }, () => hiddenCard())),
      region('CRYPT', Array.from({ length: 6 }, () => hiddenCard())),
      region(
        'HAND',
        [
          handCard('Govern the Unaligned', { typeClass: 'action', modes: [mode('Bleed for 3.'), mode('Move 3 blood to a younger vampire.', { disciplines: ['DOM'] })], multiMode: false }),
          handCard('Deflection', { typeClass: 'reaction', cost: '1 blood' }),
          handCard('Villein', { typeClass: 'master', cost: '0 pool' }),
          handCard('.44 Magnum', { typeClass: 'equipment', cost: '2 pool' }),
        ],
        { openHand: true },
      ),
    ],
    ...opts,
  });
}

// ── Game snapshot ──────────────────────────────────────────────────────────

export function chatLine(source: string, message: string, opts: Partial<ChatData> = {}): ChatData {
  const now = new Date();
  return {
    timestamp: `${now.getDate()}-${now.toLocaleString('en', { month: 'short' })} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`,
    postedAt: now.toISOString(),
    message,
    source,
    kind: 'talk',
    ...opts,
  };
}

export const sampleChat: ChatData[] = [
  chatLine('SYSTEM', 'Start of Player1 turn 3.', { kind: 'system', source: 'SYSTEM' }),
  chatLine('SYSTEM', 'Start of Master phase.', { kind: 'phase', source: 'SYSTEM' }),
  chatLine('Player1', 'plays [card:100380:Villein] on [card:200812:Lucita, Anarch Sympathizer]', { kind: 'move' }),
  chatLine('Player1', 'Anyone respond to the Villein?'),
  chatLine('Player2', 'no'),
  chatLine('Player1', 'locks [card:200812:Lucita, Anarch Sympathizer] and bleeds [d] for 2', { kind: 'move' }),
  chatLine('Player2', 'takes 2 pool'),
];

export function pendingAction(opts: Partial<PendingAction> = {}): PendingAction {
  return {
    id: 'pa1',
    actor: 'Player1',
    actingCardId: null,
    type: 'BLEED',
    label: 'bleed',
    targetPlayer: 'Player2',
    targetCardId: null,
    amount: 2,
    note: null,
    declaredAt: new Date().toISOString(),
    awaiting: ['Player2'],
    passed: [],
    ...opts,
  };
}

// ── Card-action context ────────────────────────────────────────────────────

import type { TableCardContext } from '../cardCommands';

/** The TableCardContext CardContextMenu / CardActionSheet act on. */
export function tableCtx(cardSnap: CardSnapshot, opts: Partial<TableCardContext> = {}): TableCardContext {
  return {
    controller: 'Player1 (Toreador Grand Ball)',
    controllerPool: 12,
    regionType: 'READY',
    regionCommandKey: 'ready',
    coordinate: '1',
    card: cardSnap,
    isChild: false,
    controlledByViewer: true,
    ...opts,
  };
}

export interface GameOpts extends Partial<GameSnapshot> {
  /** Number of seats; ignored when `players` is given explicitly. */
  seats?: number;
}

export function gameSnapshot(opts: GameOpts = {}): GameSnapshot {
  const seats = opts.seats ?? 3;
  const names = Array.from({ length: seats }, (_, i) => `Player${i + 1}`);
  const players =
    opts.players ??
    names.map((n, i) =>
      i === 0
        ? selfPlayer(n, { active: true, prey: names[1], predator: names[seats - 1] })
        : player(n, {
            pool: 20 - i * 3,
            prey: names[(i + 1) % seats],
            predator: names[(i - 1 + seats) % seats],
          }),
    );
  return {
    id: 'g1',
    name: 'Friday Night Standard',
    players,
    seating: opts.seating ?? names,
    currentPlayer: opts.currentPlayer ?? names[0],
    edgePlayer: opts.edgePlayer ?? 'no one',
    turn: '3',
    turnLabel: opts.turnLabel ?? `${names[0]} 3`,
    phase: opts.phase ?? 'Master',
    phases: opts.phases ?? ['Master', 'Minion', 'Influence', 'Discard'],
    turns: opts.turns ?? ['Player1 1', 'Player2 1', 'Player3 1', 'Player1 2', 'Player2 2', 'Player3 2', 'Player1 3'],
    pingOptions: names,
    player: opts.player ?? true,
    admin: opts.admin ?? false,
    judge: opts.judge ?? false,
    globalNotes: opts.globalNotes ?? 'Anthelios in play — Player2.',
    privateNotes: opts.privateNotes ?? 'Hold Deflection for the big bleed.',
    edgeColor: opts.edgeColor ?? '#8b1a1a',
    edgeTextColor: opts.edgeTextColor ?? 'white',
    status: opts.status ?? null,
    stamp: opts.stamp ?? 42,
    rejected: opts.rejected,
    judgeRequest: opts.judgeRequest ?? null,
    pendingAction: opts.pendingAction ?? null,
    chat: opts.chat ?? sampleChat,
    commandErrors: opts.commandErrors ?? [],
  };
}
