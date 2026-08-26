import { useRef, useState } from 'react';
import type { DeckInfoBean } from '../../api/types';
import { useSimpleTooltips } from '../../hooks/useSimpleTooltips';
import { confirmDialog } from '../../components/dialog';

export function DeckListPanel({
  decks,
  tags,
  deckFilter,
  onFilterChange,
  onSelect,
  onDelete,
  onNew,
}: {
  decks: DeckInfoBean[];
  tags: string[];
  deckFilter: string;
  onFilterChange: (tag: string) => void;
  onSelect: (deckName: string) => void;
  onDelete: (deckName: string) => void;
  onNew: () => void;
}) {
  const [textFilter, setTextFilter] = useState('');
  const listRef = useRef<HTMLDivElement>(null);
  useSimpleTooltips(listRef, [decks]);

  const filtered = decks.filter((d) => {
    const text = textFilter.toLowerCase();
    if (!text) return true;
    return d.name.toLowerCase().includes(text) || (d.comments ?? '').toLowerCase().includes(text);
  });

  return (
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">Decks</span>
        <button className="btn btn-sm btn-outline-secondary" onClick={onNew}>
          New <i className="bi-bookmark-plus" />
        </button>
      </div>
      <div className="card-body p-2 d-flex flex-column flex-fill min-h-0">
        <input
          type="text"
          className="form-control form-control-sm rounded-pill mb-1"
          placeholder="Filter by name or comment..."
          value={textFilter}
          onChange={(e) => setTextFilter(e.target.value)}
        />
        <select
          className="form-select form-select-sm mb-2"
          value={deckFilter}
          onChange={(e) => onFilterChange(e.target.value)}
        >
          <option value="">All</option>
          {tags.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
        <div className="flex-fill min-h-0" style={{ overflowY: 'auto', overflowX: 'clip' }} ref={listRef}>
          <div className="list-group list-group-flush">
            {filtered.map((d) => (
              <div
                key={d.name}
                className="list-group-item list-group-item-action py-1 px-2"
                style={{ cursor: 'pointer' }}
                onClick={() => onSelect(d.name)}
              >
                <div className="d-flex justify-content-between align-items-center">
                  <span className="d-flex align-items-center gap-1">
                    {d.deckFormat === 'LEGACY' && (
                      <i
                        className="bi bi-exclamation-triangle-fill text-warning"
                        data-tippy-content="Legacy format: not all features are available. Resave the deck to upgrade."
                      />
                    )}
                    <span className="deck-name-link">{d.name}</span>
                  </span>
                  <button
                    className="btn btn-sm btn-outline-secondary p-1"
                    style={{ fontSize: '0.6rem' }}
                    onClick={async (e) => {
                      e.stopPropagation();
                      if (await confirmDialog('Delete deck?', { danger: true })) onDelete(d.name);
                    }}
                  >
                    <i className="bi-trash" />
                  </button>
                </div>
                {d.comments && <div className="text-muted small text-truncate">{d.comments}</div>}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
