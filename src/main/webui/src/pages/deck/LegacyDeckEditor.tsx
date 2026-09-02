import { useCallback, useState } from 'react';
import { AlertTriangle } from 'lucide-react';
import { Panel } from '../../components/ui/Panel';
import { DeckHeaderControls } from './DeckHeaderControls';

type SaveStatus = 'idle' | 'saving' | 'saved' | 'error';

/**
 * Raw-text editor shown instead of the structured {@link DeckEditorPane} when a
 * loaded deck still has unresolved card lines (`selectedDeck.errors`) — a
 * LEGACY deck whose card names predate the current database, or a deck broken
 * by a card-data change.
 *
 * The structured editor would silently drop those lines (and any inline notes)
 * on its debounced autosave, so here we show the stored text verbatim, list
 * what didn't parse, and only write on an explicit Save — via the
 * `/decks/player/legacy` endpoint, which keeps storing raw text until every
 * line resolves and only then converts the deck. A clean save flips it back to
 * the structured editor (DeckPage keys this component on the error state).
 *
 * Rename is intentionally unavailable here — convert the deck first.
 */
interface Props {
  deckName: string;
  initialContents: string;
  errors: string[];
  onSave: (contents: string) => Promise<void>;
  onDelete: () => void;
}

export function LegacyDeckEditor({ deckName, initialContents, errors, onSave, onDelete }: Props) {
  const [contents, setContents] = useState(initialContents);
  const [status, setStatus] = useState<SaveStatus>('idle');

  const dirty = contents !== initialContents;

  const save = useCallback(async () => {
    setStatus('saving');
    try {
      await onSave(contents);
      setStatus('saved');
    } catch {
      setStatus('error');
    }
  }, [contents, onSave]);

  const titleSlot = (
    <span className="tracking-wide text-ink flex items-center gap-1.5">
      {deckName}
      <span className="text-[10px] uppercase tracking-wider px-1 py-0.5 rounded bg-amber-500/15 text-amber-500">
        Legacy
      </span>
    </span>
  );

  return (
    <Panel title={titleSlot} right={<DeckHeaderControls status={status} onRetry={save} onDelete={onDelete} />}>
      <div className="flex items-start gap-2 px-3 py-2 border-b border-line/50 bg-amber-500/5">
        <AlertTriangle className="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
        <p className="text-xs text-ink-secondary leading-relaxed">
          This deck is stored in its original text form and hasn&apos;t been converted to the current card
          format.{' '}
          {errors.length > 0
            ? `${errors.length} line${errors.length === 1 ? '' : 's'} below didn't match a known card`
            : 'Everything parsed'}{' '}
          — edit the text and <strong>Save</strong> to convert it. It can&apos;t be registered to a game until
          it converts cleanly.
        </p>
      </div>

      {errors.length > 0 && (
        <div className="px-3 py-2 border-b border-line/50">
          <div className="text-[11px] uppercase tracking-wider text-ink-muted mb-1">Unrecognised lines</div>
          <ul className="text-xs text-blood-soft font-mono space-y-0.5 max-h-32 overflow-y-auto">
            {errors.map((line, i) => (
              <li key={i}>{line || <span className="text-ink-muted italic">(blank)</span>}</li>
            ))}
          </ul>
        </div>
      )}

      <div className="flex-1 min-h-0 flex flex-col p-3 gap-2 overflow-y-auto">
        <label className="text-[11px] uppercase tracking-wider text-ink-muted">Deck list</label>
        <textarea
          value={contents}
          onChange={(e) => {
            setContents(e.target.value);
            setStatus('idle');
          }}
          spellCheck={false}
          rows={18}
          className="w-full flex-1 min-h-[16rem] bg-surface border border-line rounded px-2 py-1.5 text-xs font-mono text-ink outline-none focus:border-line-accent resize-y leading-relaxed"
        />

        <div className="flex items-center gap-2 pt-1">
          <button
            onClick={save}
            disabled={!dirty || status === 'saving'}
            className="text-xs px-3 py-1.5 rounded bg-line-accent/20 text-ink hover:bg-line-accent/30 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
          >
            {status === 'saving' ? 'Saving…' : 'Save'}
          </button>
          {status === 'saved' && <span className="text-[11px] text-ink-muted">Saved</span>}
          {status === 'error' && <span className="text-[11px] text-blood-soft">Save failed</span>}
        </div>
      </div>
    </Panel>
  );
}
