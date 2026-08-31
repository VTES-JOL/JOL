import { useEffect, useRef, useState } from 'react';
import { AlertCircle, CheckCircle } from 'lucide-react';
import type { ImportPreview } from '../../api/types';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Modal } from '../../components/ui/Modal';
import { deckApi } from './deckApi';

/**
 * Paste a KRCG JSON export or a plain JOL deck list, preview what resolves,
 * then create the deck. Ported from jol-quarkus; Tailwind `jt:` -prefixed.
 */
interface Props {
  onImport: (name: string, entries: Array<{ cardId: string; count: number }>, comments: string | null) => Promise<void>;
  onClose: () => void;
  /** Override for tests/stories; defaults to the real preview endpoint. */
  onPreview?: (text: string) => Promise<ImportPreview>;
}

export function DeckImportModal({ onImport, onClose, onPreview = deckApi.previewImport }: Props) {
  const [text, setText] = useState('');
  const [deckName, setDeckName] = useState('');
  const [comments, setComments] = useState('');
  const [preview, setPreview] = useState<ImportPreview | null>(null);
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [createError, setCreateError] = useState<string | null>(null);

  const debounceRef = useRef<ReturnType<typeof setTimeout>>(undefined);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const deckNameRef = useRef(deckName);
  const commentsRef = useRef(comments);
  const onPreviewRef = useRef(onPreview);
  useEffect(() => {
    deckNameRef.current = deckName;
  }, [deckName]);
  useEffect(() => {
    commentsRef.current = comments;
  }, [comments]);
  useEffect(() => {
    onPreviewRef.current = onPreview;
  });

  useEffect(() => {
    textareaRef.current?.focus();
  }, []);

  useEffect(() => {
    clearTimeout(debounceRef.current);
    if (!text.trim()) {
      setPreview(null);
      setError(null);
      return;
    }
    debounceRef.current = setTimeout(async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await onPreviewRef.current(text);
        setPreview(result);
        if (result.deckName && !deckNameRef.current.trim()) setDeckName(result.deckName);
        if (result.deckDescription && !commentsRef.current.trim()) setComments(result.deckDescription);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'Preview failed');
        setPreview(null);
      } finally {
        setLoading(false);
      }
    }, 500);
    return () => clearTimeout(debounceRef.current);
  }, [text]);

  async function handleCreate() {
    if (!preview || preview.resolved.length === 0 || creating) return;
    setCreating(true);
    setCreateError(null);
    try {
      const entries = preview.resolved.map((r) => ({ cardId: r.card.id, count: r.count }));
      await onImport(deckName.trim() || 'Imported Deck', entries, comments.trim() || null);
    } catch (e) {
      setCreateError(e instanceof Error ? e.message : 'Import failed');
      setCreating(false);
    }
  }

  const hasResolved = (preview?.resolved.length ?? 0) > 0;

  return (
    <Modal
      onClose={onClose}
      title="Import Deck"
      footer={
        <>
          {createError && (
            <p className="jt:mr-auto jt:text-xs jt:text-blood-soft jt:flex jt:items-center jt:gap-1">
              <AlertCircle className="jt:w-3.5 jt:h-3.5 jt:shrink-0" />
              {createError}
            </p>
          )}
          <Button variant="secondary" size="sm" onClick={onClose} disabled={creating}>
            Cancel
          </Button>
          <Button variant="primary" size="sm" loading={creating} disabled={!hasResolved} onClick={handleCreate}>
            {creating ? 'Creating…' : 'Create Deck'}
          </Button>
        </>
      }
    >
      <div>
        <label className="jt:block jt:text-xs jt:text-ink-muted jt:mb-1">Paste KRCG JSON or JOL deck list</label>
        <textarea
          ref={textareaRef}
          value={text}
          onChange={(e) => setText(e.target.value)}
          rows={7}
          spellCheck={false}
          placeholder={'Paste deck list here…\n\nKRCG JSON: {"crypt": {…}, "library": {…}}\nJOL text:  2 Pentex Subversion\n           1 Govern the Unaligned'}
          className="jt:w-full jt:rounded jt:border jt:border-line-accent jt:bg-panel/30 jt:px-3 jt:py-2 jt:text-xs jt:text-ink jt:placeholder:text-ink-muted jt:outline-none jt:focus:border-accent/60 jt:resize-none jt:font-mono"
        />
      </div>

      {(loading || preview) && (
        <div className="jt:flex jt:items-center jt:gap-2 jt:text-xs jt:text-ink-muted">
          {loading ? (
            <span className="jt:animate-pulse">Parsing…</span>
          ) : preview ? (
            <>
              <span className="jt:px-1.5 jt:py-0.5 jt:rounded jt:bg-accent/20 jt:text-accent-soft jt:font-mono jt:text-[11px] jt:uppercase jt:tracking-wider">
                {preview.format}
              </span>
              <span>
                {preview.resolved.length} card{preview.resolved.length !== 1 ? 's' : ''} resolved
                {preview.errors.length > 0 &&
                  `, ${preview.errors.length} error${preview.errors.length !== 1 ? 's' : ''}`}
              </span>
            </>
          ) : null}
        </div>
      )}

      {error && (
        <p className="jt:text-xs jt:text-blood-soft jt:flex jt:items-center jt:gap-1.5">
          <AlertCircle className="jt:w-3.5 jt:h-3.5 jt:shrink-0" />
          {error}
        </p>
      )}

      {preview && (preview.resolved.length > 0 || preview.errors.length > 0) && (
        <div className="jt:rounded jt:border jt:border-line/50 jt:overflow-y-auto jt:max-h-48 jt:text-xs">
          {preview.errors.map((e, i) => (
            <div
              key={`e${i}`}
              className="jt:flex jt:items-start jt:gap-1.5 jt:px-3 jt:py-1.5 jt:border-b jt:border-line/30 jt:text-blood-soft"
            >
              <AlertCircle className="jt:w-3 jt:h-3 jt:shrink-0 jt:mt-0.5" />
              <span className="jt:font-mono jt:truncate">{e.line}</span>
              <span className="jt:ml-auto jt:shrink-0 jt:text-ink-muted">{e.reason}</span>
            </div>
          ))}
          {preview.resolved.map((r, i) => (
            <div
              key={`r${i}`}
              className="jt:flex jt:items-center jt:gap-1.5 jt:px-3 jt:py-1.5 jt:border-b jt:border-line/30 jt:last:border-b-0 jt:text-ink-muted"
            >
              <CheckCircle className="jt:w-3 jt:h-3 jt:shrink-0 jt:text-online/70" />
              <span className="jt:tabular-nums jt:w-5 jt:text-right jt:text-ink">{r.count}×</span>
              <span className="jt:text-ink jt:truncate">{r.card.name}</span>
              <span className="jt:ml-auto jt:shrink-0 jt:text-[11px]">
                {r.card.crypt ? 'crypt' : r.card.types.join(', ')}
              </span>
            </div>
          ))}
        </div>
      )}

      {hasResolved && (
        <>
          <Input
            size="sm"
            label="Deck name"
            value={deckName}
            placeholder="Imported Deck"
            onChange={(e) => setDeckName(e.target.value)}
          />
          <div>
            <label className="jt:block jt:text-xs jt:text-ink-muted jt:mb-1">Comments</label>
            <textarea
              value={comments}
              placeholder="Optional deck notes…"
              rows={3}
              onChange={(e) => setComments(e.target.value)}
              className="jt:w-full jt:rounded jt:border jt:border-line-accent jt:bg-panel/30 jt:px-3 jt:py-2 jt:text-xs jt:text-ink jt:placeholder:text-ink-muted jt:outline-none jt:focus:border-accent/60 jt:resize-none"
            />
          </div>
        </>
      )}
    </Modal>
  );
}
