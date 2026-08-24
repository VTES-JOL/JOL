import type { ExtendedDeck } from '../../api/types';
import { DeckPreview } from '../../components/DeckPreview';

export function DeckPreviewPanel({ selectedDeck, onEdit }: { selectedDeck: ExtendedDeck | null; onEdit: () => void }) {
  const hasErrors = (selectedDeck?.errors.length ?? 0) > 0;

  return (
    <div className="card shadow flex-fill d-flex flex-column min-h-0">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">{selectedDeck?.deck.name || 'Preview'}</span>
        <span className="d-flex align-items-center gap-2">
          <span className="text-muted small">{selectedDeck?.stats.summary}</span>
          <button className="btn btn-sm btn-outline-secondary" onClick={onEdit}>
            Edit <i className="bi-pencil" />
          </button>
        </span>
      </div>
      <div className="card-body p-2 flex-fill d-flex flex-column overflow-auto min-h-0">
        <div className="flex-fill px-1 min-h-0 d-flex flex-column">
          {selectedDeck ? (
            <DeckPreview deck={selectedDeck.deck} />
          ) : (
            <div className="flex-fill d-flex flex-column align-items-center justify-content-center text-muted">
              <i className="bi bi-collection fs-1 mb-2" />
              <span>Select a deck to preview</span>
            </div>
          )}
        </div>
        {selectedDeck && (
          <div className="mt-2">
            <div>
              {hasErrors ? (
                <span className="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">
                  <i className="bi bi-exclamation-triangle me-1" />
                  Invalid
                </span>
              ) : (
                <span className="badge bg-success-subtle text-success-emphasis border border-success-subtle">
                  <i className="bi bi-check-circle me-1" />
                  Valid
                </span>
              )}
            </div>
            <div className="small text-danger mt-1">
              {selectedDeck.errors.map((err, i) => (
                <div key={i}>{err}</div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
