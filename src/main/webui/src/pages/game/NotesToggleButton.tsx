import { NotebookPen } from 'lucide-react';
import type { NotesIndicator } from './useNotesIndicator';

// The "Notes" control that opens NotesDeckDrawer. Lives in the Game Chat /
// History panel header next to the panel-swap toggle — more discoverable than
// an edge handle. Same pill styling as GamePanel's toggle button.
export function NotesToggleButton({
  indicator,
  onClick,
}: {
  indicator: NotesIndicator;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      title={
        indicator === 'update'
          ? 'Notes & deck — global notes changed'
          : 'Notes & deck'
      }
      className="relative inline-flex items-center gap-1.5 rounded-full border border-line-accent bg-surface px-2.5 py-1 text-xs text-ink-secondary hover:bg-hover shadow-sm"
    >
      <NotebookPen size={13} />
      Notes
      {indicator === 'update' && (
        <span className="absolute -right-0.5 -top-0.5 h-2 w-2 rounded-full bg-accent ring-2 ring-surface animate-pulse" />
      )}
      {indicator === 'content' && (
        <span className="absolute -right-0.5 -top-0.5 h-2 w-2 rounded-full bg-ink-muted ring-2 ring-surface" />
      )}
    </button>
  );
}
