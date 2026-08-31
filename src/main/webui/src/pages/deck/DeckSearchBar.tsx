import { useCallback, useEffect, useRef, useState } from 'react';
import { Search } from 'lucide-react';
import type { CardDetail } from '../../api/types';

/**
 * Debounced card-name search box with a keyboard-navigable suggestion list.
 * Ported from the jol-quarkus rewrite; Tailwind Tailwind-based. Feeds the
 * structured deck editor — selecting a result adds the card to the deck.
 */
interface Props {
  onSearch: (query: string) => Promise<CardDetail[]>;
  onAddCard: (result: CardDetail) => void;
}

function cryptHint(r: CardDetail): string {
  const parts: string[] = [];
  if (r.group && r.group !== 'ANY') parts.push(`G${r.group}`);
  if (r.advanced) parts.push('ADV');
  return parts.length > 0 ? `Crypt · ${parts.join(' ')}` : 'Crypt';
}

export function DeckSearchBar({ onSearch, onAddCard }: Props) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<CardDetail[]>([]);
  const [loading, setLoading] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);

  const debounceRef = useRef<ReturnType<typeof setTimeout>>(undefined);
  const onSearchRef = useRef(onSearch);
  const searchRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    onSearchRef.current = onSearch;
  });

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) {
        setResults([]);
      }
    }
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  const handleQueryChange = useCallback((value: string) => {
    setQuery(value);
    setActiveIndex(0);
    clearTimeout(debounceRef.current);
    if (!value.trim()) {
      setResults([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    debounceRef.current = setTimeout(async () => {
      try {
        const r = await onSearchRef.current(value);
        setResults(r);
        setActiveIndex(0);
      } finally {
        setLoading(false);
      }
    }, 180);
  }, []);

  const selectResult = useCallback(
    (result: CardDetail) => {
      onAddCard(result);
      setQuery('');
      setResults([]);
    },
    [onAddCard],
  );

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (!results.length) return;
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setActiveIndex((i) => Math.min(i + 1, results.length - 1));
      }
      if (e.key === 'ArrowUp') {
        e.preventDefault();
        setActiveIndex((i) => Math.max(i - 1, 0));
      }
      if (e.key === 'Enter') {
        e.preventDefault();
        selectResult(results[activeIndex]);
      }
      if (e.key === 'Escape') {
        setQuery('');
        setResults([]);
      }
    },
    [results, activeIndex, selectResult],
  );

  return (
    <div ref={searchRef} className="relative border-b border-line/50">
      <div className="flex items-center gap-1.5 px-3 py-1.5">
        {loading ? (
          <div className="w-3 h-3 border border-accent/30 border-t-accent rounded-full animate-spin shrink-0" />
        ) : (
          <Search className="w-3 h-3 shrink-0 text-ink-muted" />
        )}
        <input
          type="text"
          placeholder="Add card…"
          value={query}
          onChange={(e) => handleQueryChange(e.target.value)}
          onKeyDown={handleKeyDown}
          className="w-full bg-transparent text-xs text-ink placeholder:text-ink-muted outline-none"
        />
      </div>

      {results.length > 0 && (
        <ul
          role="listbox"
          className="absolute top-full left-0 right-0 z-20 bg-panel border border-line/60 border-t-0 rounded-b shadow-lg overflow-hidden"
        >
          {results.map((r, i) => (
            <li
              key={r.id}
              role="option"
              aria-selected={i === activeIndex}
              onMouseDown={(e) => {
                e.preventDefault();
                selectResult(r);
              }}
              onMouseEnter={() => setActiveIndex(i)}
              className={[
                'flex items-center justify-between gap-2 px-3 py-1.5 text-xs cursor-pointer transition-colors',
                i === activeIndex ? 'bg-accent/10 text-ink' : 'text-ink-secondary hover:bg-hover/50',
              ].join(' ')}
            >
              <span className="truncate">{r.name}</span>
              <span className="text-[11px] text-ink-muted shrink-0">
                {r.crypt ? cryptHint(r) : r.types.join('/')}
              </span>
            </li>
          ))}
          <li className="px-3 py-1 text-[11px] text-ink-muted border-t border-line/40 select-none">
            ↑↓ navigate · Enter select · Esc dismiss
          </li>
        </ul>
      )}

      {query.trim() && !loading && results.length === 0 && (
        <div className="absolute top-full left-0 right-0 z-20 bg-panel border border-line/60 border-t-0 rounded-b shadow-lg px-3 py-2 text-xs text-ink-muted">
          No cards found matching "{query}".
        </div>
      )}
    </div>
  );
}
