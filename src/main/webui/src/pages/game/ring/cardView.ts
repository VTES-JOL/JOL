import type { CardSnapshot } from '../../../api/types';

// What a board card *means*, derived once from a CardSnapshot so every renderer
// (the overview MiniCard, the Triad / Seat detail card, later a text row) agrees
// on the rules and only differs in how much it chooses to show. Pure — no React.

export type CardKind = 'vampire' | 'ally' | 'location' | 'other';
export type AttachmentKind = 'equipment' | 'retainer' | 'other';

export interface CardView {
  id: string;
  /** Full card name; '' when the viewer cannot see it. */
  name: string;
  /** Name before the first comma ("Muaziz, Archon of Ulugh Beg" → "Muaziz"). */
  shortName: string;
  kind: CardKind;
  /** Identity not visible to this viewer (opponent's face-down card). */
  hidden: boolean;
  faceDown: boolean;
  locked: boolean;
  contested: boolean;
  torpor: boolean;
  /** counters — blood on a vampire, life on an ally. */
  counters: number;
  /** Vampire capacity; 0 when unknown / not a vampire. */
  capacity: number;
  /** Title tag ("P", "Pg", "B" …) parsed from the card label, if any. */
  titleTag: string | null;
  attachments: AttachmentKind[];
  /** The underlying snapshot — kept for click / menu wiring, never for rules. */
  source: CardSnapshot;
}

const TITLE_TAGS: Record<string, string> = {
  prince: 'P',
  primogen: 'Pg',
  baron: 'B',
  justicar: 'J',
  'inner circle': 'IC',
  cardinal: 'C',
  bishop: 'Bp',
  archbishop: 'Ab',
  regent: 'R',
  magaji: 'M',
  priscus: 'Ps',
  kholo: 'K',
};

export function titleTagFor(label?: string | null): string | null {
  if (!label) return null;
  return TITLE_TAGS[label.trim().toLowerCase()] ?? null;
}

export function kindOf(card: CardSnapshot): CardKind {
  const t = (card.typeClass ?? '').toLowerCase();
  if (t.includes('ally') || (card.hasLife && !card.hasBlood)) return 'ally';
  if (card.minion || card.hasBlood || t === 'vampire' || t === 'imbued') return 'vampire';
  if (t.includes('master') || t.includes('location')) return 'location';
  return 'other';
}

function attachmentKind(card: CardSnapshot): AttachmentKind {
  const t = (card.typeClass ?? '').toLowerCase();
  if (t.includes('equipment')) return 'equipment';
  if (t.includes('retainer')) return 'retainer';
  return 'other';
}

export function shortNameOf(name: string): string {
  return name.split(',')[0].trim();
}

export function cardView(card: CardSnapshot, opts: { torpor?: boolean } = {}): CardView {
  const hidden = !card.visible;
  const name = hidden ? '' : (card.name ?? '');
  return {
    id: card.id,
    name,
    shortName: shortNameOf(name),
    kind: hidden ? 'other' : kindOf(card),
    hidden,
    faceDown: !!card.faceDown,
    locked: !!card.locked,
    contested: !!card.contested,
    torpor: !!opts.torpor,
    counters: card.counters ?? 0,
    capacity: card.capacity ?? 0,
    titleTag: titleTagFor(card.label),
    attachments: (card.cards ?? []).map(attachmentKind),
    source: card,
  };
}

/** Screen-reader / tooltip text — the same facts the glyph shows, in words. */
export function cardLabel(v: CardView): string {
  if (v.hidden) return 'Hidden card';
  const bits = [v.name || 'Card'];
  if (v.kind === 'vampire') bits.push(v.capacity > 0 ? `blood ${v.counters} of ${v.capacity}` : `blood ${v.counters}`);
  if (v.kind === 'ally') bits.push(`ally, life ${v.counters}`);
  if (v.kind === 'location') bits.push('location');
  if (v.titleTag) bits.push(`title ${v.titleTag}`);
  if (v.locked) bits.push('locked');
  if (v.contested) bits.push('contested');
  if (v.torpor) bits.push('in torpor');
  if (v.faceDown) bits.push('face down');
  if (v.attachments.length) bits.push(`${v.attachments.length} attached`);
  return bits.join(', ');
}

/** Fill fraction 0..1 for a vampire's blood gauge. */
export function bloodFraction(v: Pick<CardView, 'counters' | 'capacity'>): number {
  if (v.capacity <= 0) return 0;
  return Math.max(0, Math.min(1, v.counters / v.capacity));
}
