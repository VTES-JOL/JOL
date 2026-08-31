import { useCallback, useEffect, useRef, useState } from 'react';
import { Pencil } from 'lucide-react';
import type { CardDetail, DeckValidity } from '../../api/types';
import type { DeckEntry } from './deckKit';
import { useDebouncedCallback } from '../../hooks/useDebouncedCallback';
import { Panel } from '../../components/ui/Panel';
import { DeckSearchBar } from './DeckSearchBar';
import { DeckComments } from './DeckComments';
import { DeckCardList } from './DeckCardList';
import { DeckHeaderControls } from './DeckHeaderControls';
import { DeckStatusBar } from './DeckStatusBar';
import { enrichEntries, entriesToContents } from './deckEntries';

type SaveStatus = 'idle' | 'saving' | 'saved' | 'error';

/**
 * The editable middle pane. Owns the working `DeckEntry[]` + comment as local
 * state seeded once from props — the parent remounts this via `key={deckId}`
 * on deck switch, so no effect-driven re-seeding is needed. Card and comment
 * edits schedule a debounced save; a pending save is flushed on unmount (i.e.
 * when the user picks another deck). `onEntriesChange` mirrors the working
 * entries up for the analytics pane.
 */
interface Props {
  deckName: string;
  initialEntries: DeckEntry[];
  initialComment: string;
  detailMap: Map<string, CardDetail>;
  formatValidity: Record<string, DeckValidity>;
  onAddCardDetail: (card: CardDetail) => void;
  onEntriesChange?: (entries: DeckEntry[]) => void;
  onSave: (contents: string, comment: string) => Promise<void>;
  onRename: (newName: string, contents: string, comment: string) => void;
  onDelete: () => void;
  onSearch: (query: string) => Promise<CardDetail[]>;
}

const SAVE_DEBOUNCE_MS = 1500;

export function DeckEditorPane({
  deckName,
  initialEntries,
  initialComment,
  detailMap,
  formatValidity,
  onAddCardDetail,
  onEntriesChange,
  onSave,
  onRename,
  onDelete,
  onSearch,
}: Props) {
  const [entries, setEntries] = useState<DeckEntry[]>(initialEntries);
  const [comment, setComment] = useState(initialComment);
  const [saveStatus, setSaveStatus] = useState<SaveStatus>('idle');
  const [editingName, setEditingName] = useState(false);
  const [nameValue, setNameValue] = useState(deckName);

  const entriesRef = useRef(initialEntries);
  const commentRef = useRef(initialComment);
  const dirtyRef = useRef(false);
  const nameInputRef = useRef<HTMLInputElement>(null);
  const onSaveRef = useRef(onSave);
  const onEntriesChangeRef = useRef(onEntriesChange);
  useEffect(() => {
    onSaveRef.current = onSave;
    onEntriesChangeRef.current = onEntriesChange;
  });
  useEffect(() => {
    if (editingName) {
      nameInputRef.current?.focus();
      nameInputRef.current?.select();
    }
  }, [editingName]);

  // Persists the current working state. Runs on the debounce timer, on
  // unmount (deck switch) when a debounced save is still pending — via
  // useDebouncedCallback's flush — and directly from the "retry" control
  // after a failed save (the recovery path if an unmount races a failure).
  // setSaveStatus after an unmount is a harmless no-op under React 19.
  const persist = useCallback(async () => {
    dirtyRef.current = false;
    setSaveStatus('saving');
    try {
      await onSaveRef.current(entriesToContents(entriesRef.current), commentRef.current);
      setSaveStatus('saved');
    } catch {
      dirtyRef.current = true;
      setSaveStatus('error');
    }
  }, []);

  const { call: scheduleDebouncedSave, cancel: cancelDebouncedSave } = useDebouncedCallback(
    () => void persist(),
    SAVE_DEBOUNCE_MS,
  );

  const scheduleSave = useCallback(() => {
    dirtyRef.current = true;
    setSaveStatus('saving');
    scheduleDebouncedSave();
  }, [scheduleDebouncedSave]);

  const retrySave = useCallback(() => {
    cancelDebouncedSave();
    void persist();
  }, [cancelDebouncedSave, persist]);

  const mutate = useCallback(
    (fn: (prev: DeckEntry[]) => DeckEntry[]) => {
      const next = fn(entriesRef.current);
      entriesRef.current = next;
      setEntries(next);
      onEntriesChangeRef.current?.(next);
      scheduleSave();
    },
    [scheduleSave],
  );

  const handleIncrement = useCallback(
    (cardId: string) => mutate((es) => es.map((e) => (e.cardId === cardId ? { ...e, count: e.count + 1 } : e))),
    [mutate],
  );

  const handleDecrement = useCallback(
    (cardId: string) =>
      mutate((es) => {
        const entry = es.find((e) => e.cardId === cardId);
        if (!entry) return es;
        return entry.count <= 1
          ? es.filter((e) => e.cardId !== cardId)
          : es.map((e) => (e.cardId === cardId ? { ...e, count: e.count - 1 } : e));
      }),
    [mutate],
  );

  const handleAddCard = useCallback(
    (card: CardDetail) => {
      onAddCardDetail(card);
      mutate((es) => {
        const existing = es.find((e) => e.cardId === card.id);
        if (existing) return es.map((e) => (e.cardId === card.id ? { ...e, count: e.count + 1 } : e));
        return [
          ...es,
          {
            cardId: card.id,
            name: card.name,
            count: 1,
            isCrypt: card.crypt,
            types: card.types.length ? card.types : card.crypt ? ['Vampire'] : [],
            group: card.group ?? undefined,
            banned: card.banned,
            advanced: card.advanced,
          },
        ];
      });
    },
    [mutate, onAddCardDetail],
  );

  const handleCommentChange = useCallback(
    (next: string) => {
      setComment(next);
      commentRef.current = next;
      scheduleSave();
    },
    [scheduleSave],
  );

  const commitName = useCallback(() => {
    const trimmed = nameValue.trim() || deckName;
    setNameValue(trimmed);
    setEditingName(false);
    if (trimmed !== deckName) {
      onRename(trimmed, entriesToContents(entriesRef.current), commentRef.current);
    }
  }, [nameValue, deckName, onRename]);

  const displayEntries = enrichEntries(entries, detailMap);

  const titleSlot = editingName ? (
    <input
      ref={nameInputRef}
      value={nameValue}
      onChange={(e) => setNameValue(e.target.value)}
      onBlur={commitName}
      onKeyDown={(e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          commitName();
        }
        if (e.key === 'Escape') {
          setNameValue(deckName);
          setEditingName(false);
        }
      }}
      className="jt:bg-transparent jt:text-ink jt:tracking-wide jt:outline-none jt:border-b jt:border-line-accent jt:w-full jt:max-w-[220px]"
    />
  ) : (
    <span
      onClick={() => setEditingName(true)}
      className="jt:tracking-wide jt:text-ink jt:cursor-pointer jt:group jt:flex jt:items-center jt:gap-1.5"
    >
      {nameValue}
      <Pencil className="jt:w-2.5 jt:h-2.5 jt:text-ink-muted jt:opacity-0 jt:group-hover:opacity-100 jt:transition-opacity" />
    </span>
  );

  return (
    <Panel
      title={titleSlot}
      right={<DeckHeaderControls status={saveStatus} onRetry={retrySave} onDelete={onDelete} />}
    >
      <DeckSearchBar onSearch={onSearch} onAddCard={handleAddCard} />
      <DeckStatusBar entries={displayEntries} formatValidity={formatValidity} />
      <DeckComments comments={comment} onCommentsChange={handleCommentChange} />
      <DeckCardList
        entries={displayEntries}
        detailMap={detailMap}
        onIncrement={handleIncrement}
        onDecrement={handleDecrement}
      />
    </Panel>
  );
}
