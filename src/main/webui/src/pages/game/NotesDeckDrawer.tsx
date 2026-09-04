import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import type { GameSnapshot } from '../../api/types';
import { NotesPanel } from './NotesPanel';
import { DeckPanel } from './DeckPanel';

// Right-edge slide-over holding Notes + (for seated players) the registered
// Deck — moved out of the top control strip so the strip is just Hand /
// Commands / Game Chat and the board keeps the width Notes used to occupy.
//
// Opened from the "Notes" button in the Game Chat / History panel header
// (NotesToggleButton), which also carries the has-content / changed-while-
// hidden dot (see useNotesIndicator). This component is the panel only.
type Tab = 'notes' | 'deck';

export function NotesDeckDrawer({
  gameId,
  game,
  open,
  onClose,
}: {
  gameId: string;
  game: GameSnapshot;
  open: boolean;
  onClose: () => void;
}) {
  const [tab, setTab] = useState<Tab>('notes');
  // Mount the panels lazily on first open, then keep them mounted so the
  // Notes⇄Deck switch is instant and DeckPanel's one-shot fetch isn't repeated.
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    if (open) setMounted(true);
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', onKeyDown);
    return () => document.removeEventListener('keydown', onKeyDown);
  }, [open, onClose]);

  // Seated players get the Deck tab; judges / spectators see notes only.
  const showDeckTab = game.player;
  const activeTab: Tab = showDeckTab ? tab : 'notes';

  const tabButton = (value: Tab, label: string) => (
    <button
      type="button"
      onClick={() => setTab(value)}
      className={`rounded px-2.5 py-1 text-xs font-medium ${
        activeTab === value ? 'bg-hover text-ink' : 'text-ink-muted hover:text-ink'
      }`}
      aria-pressed={activeTab === value}
    >
      {label}
    </button>
  );

  return (
    <>
      {/* Scrim — mobile only, so the drawer stays non-blocking on desktop */}
      <div
        className={`absolute inset-0 z-40 bg-black/30 sm:hidden transition-opacity ${
          open ? 'opacity-100' : 'pointer-events-none opacity-0'
        }`}
        onClick={onClose}
        aria-hidden
      />

      {/* Panel */}
      <aside
        role="dialog"
        aria-label="Notes and deck"
        aria-hidden={!open}
        className={`absolute right-0 top-0 bottom-0 z-40 flex w-[min(92vw,340px)] flex-col border-l border-line-accent bg-surface shadow-xl transition-transform ${
          open ? 'translate-x-0' : 'translate-x-full pointer-events-none'
        }`}
      >
        <div className="flex shrink-0 items-center justify-between gap-2 border-b border-line bg-panel/60 px-3 py-1.5">
          <div className="flex items-center gap-1">
            {tabButton('notes', 'Notes')}
            {showDeckTab && tabButton('deck', 'Deck')}
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="rounded p-1 text-ink-muted hover:bg-hover"
          >
            <X size={16} />
          </button>
        </div>
        {mounted && (
          <>
            <div className={activeTab === 'notes' ? 'flex flex-1 min-h-0 flex-col' : 'hidden'}>
              <NotesPanel gameId={gameId} game={game} />
            </div>
            {showDeckTab && (
              <div className={activeTab === 'deck' ? 'flex flex-1 min-h-0 flex-col' : 'hidden'}>
                <DeckPanel gameId={gameId} />
              </div>
            )}
          </>
        )}
      </aside>
    </>
  );
}
