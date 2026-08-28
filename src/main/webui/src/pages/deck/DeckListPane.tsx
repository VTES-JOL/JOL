import { useState } from 'react';
import { ChevronDown, Layers, Search } from 'lucide-react';
import type { DeckInfoBean } from '../../api/types';
import { Panel } from '../../components/ui/Panel';
import { Button } from '../../components/ui/Button';
import { EmptyState } from '../../components/ui/EmptyState';
import { DeckListItem } from './DeckListItem';

/**
 * Left pane of the deck workbench: the deck list with a name filter and a
 * format-tag dropdown. Ported/adapted from jol-quarkus's DeckListPanel
 * (the advanced filter modal lands in a later phase). Tailwind `jt:` -prefixed.
 */
interface Props {
  decks: DeckInfoBean[];
  tags: string[];
  selectedId: string | null;
  formatFilter: string;
  onFormatFilterChange: (tag: string) => void;
  onSelect: (deck: DeckInfoBean) => void;
  onNew: () => void;
  onImport: () => void;
}

export function DeckListPane({
  decks,
  tags,
  selectedId,
  formatFilter,
  onFormatFilterChange,
  onSelect,
  onNew,
  onImport,
}: Props) {
  const [nameFilter, setNameFilter] = useState('');

  const visible = nameFilter.trim()
    ? decks.filter((d) => d.name.toLowerCase().includes(nameFilter.toLowerCase()))
    : decks;

  return (
    <Panel
      title="My Decks"
      right={
        <div className="jt:flex jt:items-center jt:gap-1">
          <Button variant="ghost" size="sm" onClick={onImport}>
            Import
          </Button>
          <Button variant="accent-ghost" size="sm" onClick={onNew}>
            + New
          </Button>
        </div>
      }
    >
      <div className="jt:flex jt:items-center jt:gap-1.5 jt:px-3 jt:py-1.5 jt:border-b jt:border-line/50">
        <Search className="jt:w-3 jt:h-3 jt:shrink-0 jt:text-ink-muted" />
        <input
          type="text"
          placeholder="Filter by name…"
          value={nameFilter}
          onChange={(e) => setNameFilter(e.target.value)}
          className="jt:w-full jt:bg-transparent jt:text-xs jt:text-ink jt:placeholder:text-ink-muted jt:outline-none"
        />
      </div>

      <div className="jt:px-3 jt:py-1.5 jt:border-b jt:border-line/50">
        <div className="jt:relative">
          <select
            value={formatFilter}
            onChange={(e) => onFormatFilterChange(e.target.value)}
            className="jt:w-full jt:bg-panel/40 jt:border jt:border-line-accent jt:rounded jt:text-xs jt:text-ink-secondary jt:pl-2 jt:pr-7 jt:py-1"
          >
            <option value="">All formats</option>
            {tags.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
          <ChevronDown className="jt:pointer-events-none jt:absolute jt:right-2 jt:top-1/2 jt:-translate-y-1/2 jt:w-3 jt:h-3 jt:text-ink-muted" />
        </div>
      </div>

      {decks.length === 0 ? (
        <EmptyState
          icon={Layers}
          title="No decks yet."
          action={
            <Button variant="accent-ghost" size="sm" onClick={onNew}>
              Create your first deck →
            </Button>
          }
        />
      ) : visible.length === 0 ? (
        <EmptyState title={`No decks match "${nameFilter}".`} />
      ) : (
        <div className="jt:overflow-y-auto jt:flex-1 jt:min-h-0">
          {visible.map((deck) => (
            <DeckListItem
              key={deck.deckId}
              deck={deck}
              selected={selectedId === deck.deckId}
              onClick={() => onSelect(deck)}
            />
          ))}
        </div>
      )}
    </Panel>
  );
}
