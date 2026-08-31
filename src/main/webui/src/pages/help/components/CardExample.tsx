import type { ReactNode } from 'react';
import type { CardSnapshot } from '../../../api/types';
import { Card } from '../../game/Card';
import { childrenOfType } from './mdxChildren';

export interface CardExampleProps {
  name: string;
  cardId?: string;
  clan?: string;
  capacity?: number;
  counters?: number;
  disciplines?: string; // comma-separated short codes, e.g. "pot,for,cel,PRE"
  locked?: boolean;
  contested?: boolean;
  advanced?: boolean;
  infernal?: boolean;
  votes?: string;
  label?: string;
  path?: string;
  sect?: string;
  region?: string;
  // Counter color: vampires show blood as red, allies/retainers/imbued show
  // life as green (see pages/game/Card.tsx's COUNTER_STYLE) — defaults to
  // "blood" whenever a capacity is given (i.e. it's a vampire).
  counterType?: 'blood' | 'life';
  children?: ReactNode;
}

export function buildCardSnapshot(props: CardExampleProps, id: string): CardSnapshot {
  const nested = childrenOfType(props.children, CardExample).map((child, i) => buildCardSnapshot(child.props, `${id}.${i + 1}`));

  return {
    id,
    visible: true,
    counters: props.counters ?? 0,
    cardId: props.cardId,
    name: props.name,
    advanced: props.advanced,
    disciplines: props.disciplines
      ? props.disciplines
          .split(',')
          .map((d) => d.trim())
          .filter(Boolean)
      : undefined,
    capacity: props.capacity,
    votes: props.votes,
    contested: props.contested,
    locked: props.locked,
    infernal: props.infernal,
    clan: props.clan,
    sect: props.sect,
    path: props.path,
    label: props.label,
    hasBlood: props.counterType ? props.counterType === 'blood' : props.capacity !== undefined,
    hasLife: props.counterType === 'life',
    cards: nested.length > 0 ? nested : undefined,
  };
}

// A single table card built from flat, HTML-style attributes instead of a
// CardSnapshot object, so it can be written directly in Help MDX content by
// a non-developer. Renders through the real `pages/game/Card.tsx` component
// so it's pixel-identical to what players see at the table. Nest CardExample
// elements as children to show equipment/allies/blood stacked on a vampire —
// see RegionExample for the common case of several cards in one region.
export function CardExample(props: CardExampleProps) {
  const snapshot = buildCardSnapshot(props, 'example');
  return (
    <ol className="region list-group list-group-flush list-group-numbered jt:my-3" style={{ maxWidth: '24rem' }}>
      <Card card={snapshot} region={props.region ?? 'READY'} shadow coordinate="1" />
    </ol>
  );
}
