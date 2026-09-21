import type { PlayerSnapshot, RegionSnapshot } from '../../../api/types';
import { cardView, type CardKind, type CardView } from './cardView';

// PlayerSnapshot → what the ring draws for one seat. Pure. Both Wedge and Panels
// layouts (and later Triad / Seat) read this same model.

export interface MarkerView {
  id: string;
  /** '' when the viewer cannot see who it is. */
  name: string;
  hidden: boolean;
  /** Blood on the uncontrolled vampire. */
  counters: number;
  /** 0 when unknown (a hidden card carries no capacity). */
  capacity: number;
}

export interface RingSeatModel {
  name: string;
  /** Index in table (seating) order. */
  seatIndex: number;
  color: string;
  pool: number;
  victoryPoints: number;
  edge: boolean;
  active: boolean;
  ousted: boolean;
  /** Server-derived neighbours (ousted seats skipped); null when the server did not send them. */
  prey: string | null;
  predator: string | null;
  /** "Ousted" / "Withdrew" / "Out" — how the seat left, when it has. */
  exitLabel: string;
  /** Cards in hand (hidden placeholders count). */
  handCount: number;
  /** Names of hand cards this viewer can read (own hand, or a judge's view). */
  handNames: string[];
  /** Vampires, then allies, then locations / other. */
  ready: CardView[];
  torpor: CardView[];
  uncontrolled: MarkerView[];
}

export function markerLabel(m: MarkerView): string {
  const who = m.hidden || !m.name ? 'Uncontrolled minion' : `${m.name}, uncontrolled`;
  return m.capacity > 0 ? `${who}, blood ${m.counters} of ${m.capacity}` : `${who}, blood ${m.counters}`;
}

/** Seat colours in table order. Placeholder palette until seat tokens land in theme.css. */
export const SEAT_COLORS = ['#7b98d4', '#d1855f', '#cf9f3f', '#7bab6e', '#8f7fc2', '#5fb3b3'] as const;

const KIND_ORDER: Record<CardKind, number> = { vampire: 0, ally: 1, location: 2, other: 3 };

const cardsOf = (p: PlayerSnapshot, type: string): RegionSnapshot['cards'] =>
  p.regions.find((r) => r.type === type)?.cards ?? [];

export function ringSeatModel(p: PlayerSnapshot, seatIndex: number): RingSeatModel {
  const ready = cardsOf(p, 'READY')
    .map((c) => cardView(c))
    .sort((a, b) => KIND_ORDER[a.kind] - KIND_ORDER[b.kind]); // stable: keeps board order within a kind
  const torpor = cardsOf(p, 'TORPOR').map((c) => cardView(c, { torpor: true }));
  const uncontrolled = cardsOf(p, 'UNCONTROLLED').map<MarkerView>((c) => ({
    id: c.id,
    name: c.visible ? (c.name ?? '') : '',
    hidden: !c.visible,
    counters: c.counters ?? 0,
    capacity: c.capacity ?? 0,
  }));
  return {
    name: p.name,
    seatIndex,
    color: SEAT_COLORS[seatIndex % SEAT_COLORS.length],
    pool: p.pool,
    victoryPoints: p.victoryPoints,
    edge: p.edge,
    active: p.active,
    ousted: p.pool < 1 || !!p.exitKind,
    prey: p.prey ?? null,
    predator: p.predator ?? null,
    exitLabel: p.exitKind === 'WITHDRAW' ? 'Withdrew' : p.exitKind === 'OUST' ? 'Ousted' : 'Out',
    handCount: cardsOf(p, 'HAND').length,
    handNames: cardsOf(p, 'HAND').filter((c) => c.visible && c.name).map((c) => c.name!),
    ready,
    torpor,
    uncontrolled,
  };
}

export interface RingModel {
  seats: RingSeatModel[];
  /** Seat drawn at the bottom of the ring. */
  anchorIndex: number;
}

/**
 * Order players by `seating` and pick the bottom seat: the viewer's own; else
 * `focusName`; else the edge holder; else the first seat.
 */
export function buildRingModel(
  players: PlayerSnapshot[],
  seating: string[],
  opts: { viewerName?: string | null; focusName?: string | null } = {},
): RingModel {
  const byName = new Map(players.map((p) => [p.name, p]));
  const ordered = (seating.length ? seating : players.map((p) => p.name))
    .map((n) => byName.get(n))
    .filter((p): p is PlayerSnapshot => !!p);
  const seats = ordered.map((p, i) => ringSeatModel(p, i));
  const find = (name?: string | null) => (name ? seats.findIndex((s) => s.name === name) : -1);
  const edge = seats.findIndex((s) => s.edge);
  const anchorIndex = [find(opts.viewerName), find(opts.focusName), edge, 0].find((i) => i >= 0) ?? 0;
  return { seats, anchorIndex };
}
