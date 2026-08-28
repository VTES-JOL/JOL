import { useCallback, useEffect, useRef, useState } from 'react';
import { Search } from 'lucide-react';
import type { CardDetail } from '../../api/types';

/**
 * Debounced card-name search box with a keyboard-navigable suggestion list.
 * Ported from the jol-quarkus rewrite; Tailwind `jt:` -prefixed. Feeds the
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
    <div ref={searchRef} className="jt:relative jt:border-b jt:border-line/50">
      <div className="jt:flex jt:items-center jt:gap-1.5 jt:px-3 jt:py-1.5">
        {loading ? (
          <div className="jt:w-3 jt:h-3 jt:border jt:border-accent/30 jt:border-t-accent jt:rounded-full jt:animate-spin jt:shrink-0" />
        ) : (
          <Search className="jt:w-3 jt:h-3 jt:shrink-0 jt:text-ink-muted" />
        )}
        <input
          type="text"
          placeholder="Add card…"
          value={query}
          onChange={(e) => handleQueryChange(e.target.value)}
          onKeyDown={handleKeyDown}
          className="jt:w-full jt:bg-transparent jt:text-xs jt:text-ink jt:placeholder:text-ink-muted jt:outline-none"
        />
      </div>

      {results.length > 0 && (
        <ul
          role="listbox"
          className="jt:absolute jt:top-full jt:left-0 jt:right-0 jt:z-20 jt:bg-panel jt:border jt:border-line/60 jt:border-t-0 jt:rounded-b jt:shadow-lg jt:overflow-hidden"
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
                'jt:flex jt:items-center jt:justify-between jt:gap-2 jt:px-3 jt:py-1.5 jt:text-xs jt:cursor-pointer jt:transition-colors',
                i === activeIndex ? 'jt:bg-accent/10 jt:text-ink' : 'jt:text-ink-secondary jt:hover:bg-hover/50',
              ].join(' ')}
            >
              <span className="jt:truncate">{r.name}</span>
              <span className="jt:text-[11px] jt:text-ink-muted jt:shrink-0">
                {r.crypt ? cryptHint(r) : r.types.join('/')}
              </span>
            </li>
          ))}
          <li className="jt:px-3 jt:py-1 jt:text-[11px] jt:text-ink-muted jt:border-t jt:border-line/40 jt:select-none">
            ↑↓ navigate · Enter select · Esc dismiss
          </li>
        </ul>
      )}

      {query.trim() && !loading && results.length === 0 && (
        <div className="jt:absolute jt:top-full jt:left-0 jt:right-0 jt:z-20 jt:bg-panel jt:border jt:border-line/60 jt:border-t-0 jt:rounded-b jt:shadow-lg jt:px-3 jt:py-2 jt:text-xs jt:text-ink-muted">
          No cards found matching "{query}".
        </div>
      )}
    </div>
  );
}
