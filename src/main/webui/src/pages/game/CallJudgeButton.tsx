import { useState } from 'react';
import { Gavel } from 'lucide-react';
import type { GameSnapshot } from '../../api/types';
import { JudgeRequestModal } from './JudgeRequestModal';

// The "Call Judge" / "Judge Called" control + its request modal, together. Lives
// in the HUD's right-hand cluster next to Notes / History (Main.dc.html), not in
// the Commands panel. Shown to seated players, judges, and anyone who can see an
// open request.
export function CallJudgeButton({
  gameId,
  game,
  onUpdated,
  submitting,
  guard,
}: {
  gameId: string;
  game: GameSnapshot;
  onUpdated: (updated: GameSnapshot) => void;
  submitting: boolean;
  guard: <T>(run: () => Promise<T>) => Promise<T | undefined>;
}) {
  const [open, setOpen] = useState(false);
  const request = game.judgeRequest;

  if (!game.player && !game.judge && !request) return null;

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        title={request ? 'A judge has been called — view the request' : 'Request a judge come to the table'}
        className={`inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs ${
          request
            ? 'border-blood/50 bg-blood/15 text-blood animate-pulse'
            : 'border-line-accent text-ink-secondary hover:bg-hover'
        }`}
      >
        <Gavel size={11} />
        {request ? 'Judge Called' : 'Call Judge'}
      </button>
      {open && (
        <JudgeRequestModal
          gameId={gameId}
          request={request}
          onUpdated={onUpdated}
          onClose={() => setOpen(false)}
          submitting={submitting}
          guard={guard}
        />
      )}
    </>
  );
}
