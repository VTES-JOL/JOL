import { type ReactNode } from 'react';
import type { PendingAction } from '../../api/types';

// The response-window banner (rules R1 / D9). One strip under the HUD row,
// three faces depending on who's looking:
//   • a seat that still owes a response  → urgent, [Respond] [Pass]
//   • the actor                          → accent, [Resolve] (+ [Take Edge] on a cleared bleed)
//   • everyone else                      → a quiet informational line
// Never a hard block — endTurn closes the window with a warning (CommandForm).
export function PendingActionBar({
  pending,
  viewerName,
  onCommand,
  onRespond,
}: {
  pending: PendingAction;
  viewerName: string | null;
  onCommand: (command: string) => void;
  onRespond: () => void;
}) {
  const isActor = !!viewerName && viewerName === pending.actor;
  const owesResponse = !!viewerName && pending.awaiting.includes(viewerName);
  const hasPassed = !!viewerName && pending.passed.includes(viewerName);

  const vs = pending.targetPlayer ? ` vs ${pending.targetPlayer}` : '';
  const forAmt = pending.amount > 1 ? ` for ${pending.amount}` : '';

  if (owesResponse) {
    const directedAtMe = pending.type === 'BLEED' && pending.targetPlayer === viewerName;
    const text = directedAtMe
      ? `${pending.actor} is bleeding you${forAmt}`
      : `${pending.actor} declared a ${pending.label}${vs}`;
    return (
      <Strip tone="urgent">
        <span className="flex-1 font-medium">▸ {text}</span>
        <button type="button" onClick={onRespond} className={BTN.primary}>
          Respond
        </button>
        <button type="button" onClick={() => onCommand('pass')} className={BTN.ghost}>
          Pass
        </button>
      </Strip>
    );
  }

  if (isActor) {
    const status = pending.awaiting.length
      ? `${pending.awaiting.length} to respond`
      : 'all passed';
    const canTakeEdge = pending.type === 'BLEED' && pending.awaiting.length === 0;
    return (
      <Strip tone="actor">
        <span className="flex-1">
          ▸ Your {pending.label}
          {vs}
          {forAmt} · <span className="text-ink-muted">{status}</span>
        </span>
        {canTakeEdge && (
          <button type="button" onClick={() => onCommand(`edge ${pending.actor}`)} className={BTN.ghost}>
            Take Edge
          </button>
        )}
        <button type="button" onClick={() => onCommand('resolve')} className={BTN.primary}>
          Resolve
        </button>
      </Strip>
    );
  }

  return (
    <Strip tone="quiet">
      <span className="flex-1">
        ▸ {pending.actor} declared a {pending.label}
        {vs}
        {hasPassed && <span className="text-ink-muted"> · you passed</span>}
      </span>
    </Strip>
  );
}

const BTN = {
  primary:
    'shrink-0 rounded border border-current px-2 py-0.5 text-xs font-semibold hover:bg-current/10 focus-visible:outline focus-visible:outline-1',
  ghost: 'shrink-0 rounded px-2 py-0.5 text-xs font-medium opacity-80 hover:opacity-100 hover:underline',
};

function Strip({ tone, children }: { tone: 'urgent' | 'actor' | 'quiet'; children: ReactNode }) {
  const cls =
    tone === 'urgent'
      ? 'border-blood/40 bg-blood/15 text-blood'
      : tone === 'actor'
        ? 'border-accent/40 bg-accent/10 text-ink'
        : 'border-line bg-panel/60 text-ink-muted';
  return (
    <div className={`flex items-center gap-2 border-t px-3 py-1 text-xs ${cls}`}>{children}</div>
  );
}
