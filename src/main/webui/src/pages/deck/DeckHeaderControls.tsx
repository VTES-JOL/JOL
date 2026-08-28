import { useState } from 'react';
import { RotateCcw, Trash2 } from 'lucide-react';

/**
 * Editor header right-slot: save-status text (with retry on failure) and a
 * delete control with an inline Yes/No confirm. Ported from jol-quarkus;
 * Tailwind `jt:` -prefixed.
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
    <div className="jt:flex jt:items-center jt:gap-2">
      {status === 'error' ? (
        <>
          <span className="jt:text-[11px] jt:text-blood-soft">Save failed</span>
          {onRetry && (
            <button
              onClick={onRetry}
              title="Retry save"
              className="jt:p-1 jt:rounded jt:hover:bg-blood/10 jt:text-blood-soft jt:hover:text-blood jt:transition-colors jt:cursor-pointer"
            >
              <RotateCcw className="jt:w-3 jt:h-3" />
            </button>
          )}
        </>
      ) : status === 'saving' || status === 'saved' ? (
        <span className="jt:text-[11px] jt:text-ink-muted">{LABEL[status]}</span>
      ) : null}

      {onDelete &&
        (confirmDel ? (
          <div className="jt:flex jt:items-center jt:gap-1">
            <span className="jt:text-[11px] jt:text-blood-soft">Delete?</span>
            <button
              onClick={() => {
                setConfirmDel(false);
                onDelete();
              }}
              className="jt:text-[11px] jt:px-1.5 jt:py-0.5 jt:rounded jt:bg-blood/15 jt:text-blood-soft jt:hover:bg-blood/25 jt:transition-colors jt:cursor-pointer"
            >
              Yes
            </button>
            <button
              onClick={() => setConfirmDel(false)}
              className="jt:text-[11px] jt:px-1.5 jt:py-0.5 jt:rounded jt:hover:bg-hover jt:text-ink-muted jt:transition-colors jt:cursor-pointer"
            >
              No
            </button>
          </div>
        ) : (
          <button
            onClick={() => setConfirmDel(true)}
            title="Delete deck"
            className="jt:p-1.5 jt:rounded jt:hover:bg-blood/10 jt:text-ink-muted jt:hover:text-blood jt:transition-colors jt:cursor-pointer"
          >
            <Trash2 className="jt:w-3.5 jt:h-3.5" />
          </button>
        ))}
    </div>
  );
}
