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
 * (the advanced filter modal lands in a later phase). Tailwind Tailwind-based.
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
      // The MasterDetailView tab strip already labels this pane on mobile;
      // keep the header row (it holds Import / + New) but drop the duplicate
      // title, and keep the buttons right-aligned without it.
      titleClassName="hidden lg:block"
      headerClassName="max-lg:justify-end"
      right={
        <div className="flex items-center gap-1">
          <Button variant="ghost" size="sm" onClick={onImport}>
            Import
          </Button>
          <Button variant="accent-ghost" size="sm" onClick={onNew}>
            + New
          </Button>
        </div>
      }
    >
      <div className="flex items-center gap-1.5 px-3 py-1.5 border-b border-line/50">
        <Search className="w-3 h-3 shrink-0 text-ink-muted" />
        <input
          type="text"
          placeholder="Filter by name…"
          value={nameFilter}
          onChange={(e) => setNameFilter(e.target.value)}
          className="w-full bg-transparent text-xs text-ink placeholder:text-ink-muted outline-none"
        />
      </div>

      <div className="px-3 py-1.5 border-b border-line/50">
        <div className="relative">
          <select
            value={formatFilter}
            onChange={(e) => onFormatFilterChange(e.target.value)}
            className="w-full bg-panel/40 border border-line-accent rounded text-xs text-ink-secondary pl-2 pr-7 py-1"
          >
            <option value="">All formats</option>
            {tags.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
          <ChevronDown className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 w-3 h-3 text-ink-muted" />
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
        <div className="overflow-y-auto flex-1 min-h-0">
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
