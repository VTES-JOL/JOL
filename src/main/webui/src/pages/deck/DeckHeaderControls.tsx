import { useState } from 'react';
import { RotateCcw, Trash2 } from 'lucide-react';

/**
 * Editor header right-slot: save-status text (with retry on failure) and a
 * delete control with an inline Yes/No confirm. Ported from jol-quarkus;
 * Tailwind Tailwind-based.
 */
type SaveStatus = 'idle' | 'saving' | 'saved' | 'error';

interface Props {
  status: SaveStatus;
  onRetry?: () => void;
  onDelete?: () => void;
}

const LABEL: Record<Exclude<SaveStatus, 'idle' | 'error'>, string> = {
  saving: 'Saving…',
  saved: 'Saved',
};

export function DeckHeaderControls({ status, onRetry, onDelete }: Props) {
  const [confirmDel, setConfirmDel] = useState(false);

  return (
    <div className="flex items-center gap-2">
      {status === 'error' ? (
        <>
          <span className="text-[11px] text-blood-soft">Save failed</span>
          {onRetry && (
            <button
              onClick={onRetry}
              title="Retry save"
              className="p-1 rounded hover:bg-blood/10 text-blood-soft hover:text-blood transition-colors cursor-pointer"
            >
              <RotateCcw className="w-3 h-3" />
            </button>
          )}
        </>
      ) : status === 'saving' || status === 'saved' ? (
        <span className="text-[11px] text-ink-muted">{LABEL[status]}</span>
      ) : null}

      {onDelete &&
        (confirmDel ? (
          <div className="flex items-center gap-1">
            <span className="text-[11px] text-blood-soft">Delete?</span>
            <button
              onClick={() => {
                setConfirmDel(false);
                onDelete();
              }}
              className="text-[11px] px-1.5 py-0.5 rounded bg-blood/15 text-blood-soft hover:bg-blood/25 transition-colors cursor-pointer"
            >
              Yes
            </button>
            <button
              onClick={() => setConfirmDel(false)}
              className="text-[11px] px-1.5 py-0.5 rounded hover:bg-hover text-ink-muted transition-colors cursor-pointer"
            >
              No
            </button>
          </div>
        ) : (
          <button
            onClick={() => setConfirmDel(true)}
            title="Delete deck"
            className="p-1.5 rounded hover:bg-blood/10 text-ink-muted hover:text-blood transition-colors cursor-pointer"
          >
            <Trash2 className="w-3.5 h-3.5" />
          </button>
        ))}
    </div>
  );
}
