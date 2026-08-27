import { useEffect, useState } from 'react';
import type { ExtendedDeck } from '../../api/types';
import { alertDialog } from '../../components/dialog';

export function DeckEditor({
  selectedDeck,
  contents,
  tags,
  deckFilter,
  onSave,
  onValidate,
  onCancel,
}: {
  selectedDeck: ExtendedDeck | null;
  contents: string | null;
  tags: string[];
  deckFilter: string;
  onSave: (name: string, contents: string, comment: string) => void;
  onValidate: (name: string, contents: string, format: string) => void;
  onCancel: () => void;
}) {
  const [name, setName] = useState(selectedDeck?.deck.name ?? '');
  const [text, setText] = useState(contents ?? '');
  const [comment, setComment] = useState(selectedDeck?.deck.comments ?? '');
  // Same default source as the list's own tag filter — matches ds.js's
  // callbackShowDecks, which pre-selects both dropdowns from data.deckFilter.
  const [format, setFormat] = useState(tags.includes(deckFilter) ? deckFilter : (tags[0] ?? ''));

  useEffect(() => {
    setName(selectedDeck?.deck.name ?? '');
    setText(contents ?? '');
    setComment(selectedDeck?.deck.comments ?? '');
    setFormat(tags.includes(deckFilter) ? deckFilter : (tags[0] ?? ''));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedDeck, contents]);

  const save = async () => {
    if (!name.trim()) {
      await alertDialog('Please enter a name for the deck');
      return;
    }
    onSave(name, text, comment);
  };

  return (
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">Edit Deck</span>
        <span className="d-flex gap-1">
          <button className="btn btn-sm btn-outline-secondary" onClick={save}>
            Save <i className="bi-floppy" />
          </button>
          <button className="btn btn-sm btn-outline-secondary" onClick={onCancel}>
            Cancel
          </button>
        </span>
      </div>
      <div className="card-body p-2 flex-fill d-flex flex-column overflow-auto px-3 min-h-0">
        <div className="row mb-2 flex-shrink-0">
          <label className="col-form-label col-2">Name</label>
          <div className="col-10">
            <input
              className="form-control form-control-sm w-100"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>
        </div>
        <div className="row mb-2 flex-shrink-0">
          <label className="col-form-label col-2">Format</label>
          <div className="col-7">
            <select className="form-select form-select-sm" value={format} onChange={(e) => setFormat(e.target.value)}>
              {tags.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>
          <div className="col-3">
            <button className="btn btn-outline-secondary btn-sm w-100" onClick={() => onValidate(name, text, format)}>
              Validate
            </button>
          </div>
        </div>
        <div className="deck-editor-fields">
          <div className="deck-editor-contents">
            <label className="form-label small text-muted mb-1">Contents</label>
            <textarea
              className="form-control form-control-sm"
              style={{ resize: 'none' }}
              value={text}
              onChange={(e) => setText(e.target.value)}
            />
          </div>
          <div className="deck-editor-errors">
            <label className="form-label small text-muted mb-1">Errors</label>
            <div className="errors-box text-danger small border rounded p-2 bg-body">
              {(selectedDeck?.errors ?? []).map((err, i) => (
                <div key={i}>{err}</div>
              ))}
            </div>
          </div>
        </div>
        <label className="form-label small text-muted mb-1 flex-shrink-0">Comment</label>
        <textarea
          className="form-control form-control-sm flex-shrink-0"
          style={{ resize: 'none' }}
          placeholder="Deck comment..."
          rows={3}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
        />
      </div>
    </div>
  );
}
