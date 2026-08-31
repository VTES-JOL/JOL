import { useCallback, useEffect, useRef, useState } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';
import type { CardDetail } from '../../api/types';
import { type CardGroup, type DeckEntry, groupEntries } from './deckKit';
import { TypeIcon } from '../../components/ui/icons';
import { DeckCardRow } from './DeckCardRow';

/**
 * The grouped, collapsible card list of the deck editor. Ported from
 * jol-quarkus; Tailwind Tailwind-based. Groups open by default and auto-open
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
        className="w-full flex items-center gap-2 pl-3 pr-4 py-2 border-b border-line/50 sticky top-0 z-10 bg-panel/30 hover:bg-hover/40 transition-colors"
      >
        {isOpen ? (
          <ChevronDown className="w-3 h-3 text-ink-muted shrink-0" />
        ) : (
          <ChevronRight className="w-3 h-3 text-ink-muted shrink-0" />
        )}
        <span className="text-xs font-semibold text-ink">{group.key}</span>
        {group.key !== 'Crypt' && (
          <span className="flex items-center gap-0.5 shrink-0">
            {group.key.split('/').map((t) => (
              <TypeIcon key={t} type={t} size={16} />
            ))}
          </span>
        )}
        <span className="ml-auto inline-flex items-center px-1.5 py-0.5 rounded-full bg-hover border border-line/60 text-[11px] font-semibold tabular-nums text-ink-secondary leading-none">
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
      <div className="flex-1 flex items-center justify-center p-8 text-sm text-ink-muted">
        {entriesLoading ? 'Loading…' : 'Search above to add cards.'}
      </div>
    );
  }

  return (
    <div className="flex-1 min-h-0 overflow-y-auto">
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
