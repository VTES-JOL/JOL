import { useCallback, useEffect, useRef, useState } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import type { CardDetail } from '../../api/types';
import { type CardGroup, type DeckEntry, groupEntries } from './deckKit';
import { TypeIcon } from '../../components/ui/icons';
import { DeckCardRow } from './DeckCardRow';

/**
 * The grouped, collapsible card list of the deck editor. Ported from
 * jol-quarkus; Tailwind `jt:` -prefixed. Groups open by default and auto-open
 * when a new group first appears. Handlers are optional (Phase 2 read-only).
 */
interface Props {
  entries: DeckEntry[];
  detailMap?: Map<string, CardDetail>;
  entriesLoading?: boolean;
  onIncrement?: (cardId: string) => void;
  onDecrement?: (cardId: string) => void;
}

function GroupSection({
  group,
  isOpen,
  onToggle,
  detailMap,
  onIncrement,
  onDecrement,
}: {
  group: CardGroup;
  isOpen: boolean;
  onToggle: (key: string) => void;
  detailMap?: Map<string, CardDetail>;
  onIncrement?: (cardId: string) => void;
  onDecrement?: (cardId: string) => void;
}) {
  return (
    <div>
      <button
        onClick={() => onToggle(group.key)}
        className="jt:w-full jt:flex jt:items-center jt:gap-2 jt:pl-3 jt:pr-4 jt:py-2 jt:border-b jt:border-line/50 jt:sticky jt:top-0 jt:z-10 jt:bg-panel/30 jt:hover:bg-hover/40 jt:transition-colors"
      >
        {isOpen ? (
          <ChevronDown className="jt:w-3 jt:h-3 jt:text-ink-muted jt:shrink-0" />
        ) : (
          <ChevronRight className="jt:w-3 jt:h-3 jt:text-ink-muted jt:shrink-0" />
        )}
        <span className="jt:text-xs jt:font-semibold jt:text-ink">{group.key}</span>
        {group.key !== 'Crypt' && (
          <span className="jt:flex jt:items-center jt:gap-0.5 jt:shrink-0">
            {group.key.split('/').map((t) => (
              <TypeIcon key={t} type={t} size={16} />
            ))}
          </span>
        )}
        <span className="jt:ml-auto jt:inline-flex jt:items-center jt:px-1.5 jt:py-0.5 jt:rounded-full jt:bg-hover jt:border jt:border-line/60 jt:text-[11px] jt:font-semibold jt:tabular-nums jt:text-ink-secondary jt:leading-none">
          {group.total}
        </span>
      </button>
      {isOpen &&
        group.entries.map((entry) => (
          <DeckCardRow
            key={entry.cardId}
            entry={entry}
            detail={detailMap?.get(entry.cardId)}
            onIncrement={onIncrement ? () => onIncrement(entry.cardId) : undefined}
            onDecrement={onDecrement ? () => onDecrement(entry.cardId) : undefined}
          />
        ))}
    </div>
  );
}

export function DeckCardList({ entries, detailMap, entriesLoading, onIncrement, onDecrement }: Props) {
  const [openGroups, setOpenGroups] = useState<Set<string>>(new Set());
  const initialized = useRef(false);
  const seen = useRef<Set<string>>(new Set());

  useEffect(() => {
    if (entries.length === 0) return;
    const keys = groupEntries(entries).map((g) => g.key);
    if (!initialized.current) {
      initialized.current = true;
      keys.forEach((k) => seen.current.add(k));
      setOpenGroups(new Set(keys));
      return;
    }
    const fresh = keys.filter((k) => !seen.current.has(k));
    if (fresh.length === 0) return;
    fresh.forEach((k) => seen.current.add(k));
    setOpenGroups((prev) => {
      const next = new Set(prev);
      fresh.forEach((k) => next.add(k));
      return next;
    });
  }, [entries]);

  const toggleGroup = useCallback((key: string) => {
    setOpenGroups((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  }, []);

  if (entries.length === 0) {
    return (
      <div className="jt:flex-1 jt:flex jt:items-center jt:justify-center jt:p-8 jt:text-sm jt:text-ink-muted">
        {entriesLoading ? 'Loading…' : 'Search above to add cards.'}
      </div>
    );
  }

  return (
    <div className="jt:flex-1 jt:min-h-0 jt:overflow-y-auto">
      {groupEntries(entries).map((group) => (
        <GroupSection
          key={group.key}
          group={group}
          isOpen={openGroups.has(group.key)}
          onToggle={toggleGroup}
          detailMap={detailMap}
          onIncrement={onIncrement}
          onDecrement={onDecrement}
        />
      ))}
    </div>
  );
}
