import { memo, useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import type { PendingAction, PlayerSnapshot } from '../../api/types';

// Context-aware quick-command strip that sits directly above the command
// <input> in the Act cell (was the "Phase 5 combat/referendum palette" — the
// engine has no combat or referendum verbs, so every button here just submits
// a real generic command via onCommand, exactly as if it were typed).
//
// Rows shown depend on game.phase + game.pendingAction:
//   - always: unlock all · edge · burn edge · draw
//   - your Influence turn: + draw crypt
//   - pendingAction RUSH/RESCUE (combat): acting-minion blood ± · lock/unlock ·
//     burn edge · press (−1 pool) · declare rescue
//   - pendingAction POLITICAL (referendum): pass · resolve · resolve cancel,
//     plus a read-only vote tally summed from each seat's crypt `votes`.

const BTN =
  'rounded border px-2 py-1 text-xs transition-colors disabled:opacity-40';
const NEUTRAL = `${BTN} border-line-accent text-ink-secondary hover:bg-hover`;
const GOLD = `${BTN} border-gold/50 text-gold hover:bg-gold/10`;
const RED = `${BTN} border-blood/40 text-blood hover:bg-blood/10`;
const GREEN = `${BTN} border-online/40 text-online hover:bg-online/10`;
const GROUP = 'flex flex-wrap items-center gap-1';
const TAG = 'text-[0.6rem] font-bold uppercase tracking-wide text-ink-muted';

function firstName(name: string): string {
  return name.split(' ')[0];
}

// Numeric votes annotated on this seat's in-play minions (READY + TORPOR).
// A non-numeric annotation (e.g. "P" — conditional / press) is surfaced as a
// count but never summed, matching how a player eyeballs a referendum.
function seatVotes(player: PlayerSnapshot): { fixed: number; conditional: number } {
  let fixed = 0;
  let conditional = 0;
  for (const region of player.regions) {
    if (region.type !== 'READY' && region.type !== 'TORPOR') continue;
    for (const card of region.cards) {
      const raw = (card.votes ?? '').trim();
      if (!raw) continue;
      const n = Number(raw);
      if (Number.isFinite(n)) fixed += n;
      else conditional += 1;
    }
  }
  return { fixed, conditional };
}

export const ActQuickBar = memo(function ActQuickBar({
  phase,
  isMyTurn,
  pending,
  me,
  players,
  onCommand,
}: {
  phase: string;
  isMyTurn: boolean;
  pending: PendingAction | null | undefined;
  me: PlayerSnapshot | null | undefined;
  players: PlayerSnapshot[];
  onCommand: (command: string) => void;
}) {
  const [open, setOpen] = useState(true);
  if (!me) return null;

  const meFirst = firstName(me.name);
  const combat = pending?.type === 'RUSH' || pending?.type === 'RESCUE';
  const referendum = pending?.type === 'POLITICAL';
  const influenceTurn = isMyTurn && phase === 'Influence';

  // Resolve the acting minion's board position so the combat blood / lock
  // buttons target it precisely; omit those buttons if it isn't on my board.
  const ready = me.regions.find((r) => r.type === 'READY');
  const actingIdx =
    combat && pending?.actingCardId && ready
      ? ready.cards.findIndex((c) => c.id === pending.actingCardId)
      : -1;
  const actingCoord = actingIdx >= 0 ? String(actingIdx + 1) : null;
  const actingName = actingCoord ? ready!.cards[actingIdx].name ?? 'acting minion' : null;

  const tally = referendum
    ? players
        .filter((p) => p.pool >= 1)
        .map((p) => ({ name: p.name, ...seatVotes(p) }))
    : [];
  const tallyTotal = tally.reduce((sum, t) => sum + t.fixed, 0);

  return (
    <div className="rounded border border-line bg-surface/40">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="flex w-full items-center gap-1.5 px-2 py-1 text-left text-[0.65rem] font-bold uppercase tracking-wide text-ink-muted hover:text-ink"
      >
        {open ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
        Quick actions
        {combat && <span className="rounded bg-blood/15 px-1 text-blood">combat</span>}
        {referendum && <span className="rounded bg-gold/15 px-1 text-gold">referendum</span>}
      </button>

      {open && (
        <div className="flex flex-col gap-1.5 border-t border-line px-2 py-1.5">
          <div className={GROUP}>
            <button type="button" className={NEUTRAL} onClick={() => onCommand('unlock')}>
              Unlock all
            </button>
            <button type="button" className={NEUTRAL} onClick={() => onCommand('edge')} title="Take / keep the Edge">
              Edge
            </button>
            <button type="button" className={RED} onClick={() => onCommand('edge burn')}>
              Burn edge
            </button>
            <button type="button" className={NEUTRAL} onClick={() => onCommand('draw')}>
              Draw
            </button>
            {influenceTurn && (
              <button type="button" className={NEUTRAL} onClick={() => onCommand('draw crypt')}>
                Draw crypt
              </button>
            )}
          </div>

          {combat && (
            <div className={GROUP}>
              <span className={TAG}>Combat</span>
              {actingCoord && (
                <>
                  <button
                    type="button"
                    className={RED}
                    title={`Remove 1 blood from ${actingName}`}
                    onClick={() => onCommand(`blood ${meFirst} ready ${actingCoord} -1`)}
                  >
                    −1 blood
                  </button>
                  <button
                    type="button"
                    className={GREEN}
                    title={`Add 1 blood to ${actingName}`}
                    onClick={() => onCommand(`blood ${meFirst} ready ${actingCoord} +1`)}
                  >
                    +1 blood
                  </button>
                  <button
                    type="button"
                    className={NEUTRAL}
                    onClick={() => onCommand(`lock ${meFirst} ready ${actingCoord}`)}
                  >
                    Lock
                  </button>
                  <button
                    type="button"
                    className={NEUTRAL}
                    onClick={() => onCommand(`unlock ${meFirst} ready ${actingCoord}`)}
                  >
                    Unlock
                  </button>
                </>
              )}
              <button
                type="button"
                className={RED}
                title="Press: burn 1 pool"
                onClick={() => onCommand('pool -1')}
              >
                Press (−1 pool)
              </button>
              <button type="button" className={NEUTRAL} onClick={() => onCommand('declare rescue')}>
                Declare rescue
              </button>
            </div>
          )}

          {referendum && (
            <>
              <div className={GROUP}>
                <span className={TAG}>Referendum</span>
                <button type="button" className={NEUTRAL} onClick={() => onCommand('pass')}>
                  Pass
                </button>
                <button type="button" className={GOLD} onClick={() => onCommand('resolve')}>
                  Resolve
                </button>
                <button type="button" className={NEUTRAL} onClick={() => onCommand('resolve cancel')}>
                  Resolve (cancel)
                </button>
              </div>
              <div className="rounded border border-line/60 bg-base/40 px-2 py-1 text-[0.7rem]">
                <div className="mb-0.5 flex items-center justify-between">
                  <span className={TAG}>Vote tally</span>
                  <span className="font-semibold tabular-nums text-ink">{tallyTotal} total</span>
                </div>
                <ul className="flex flex-col gap-0.5">
                  {tally.map((t) => (
                    <li key={t.name} className="flex items-center justify-between gap-2">
                      <span className="truncate text-ink-secondary">{t.name}</span>
                      <span className="shrink-0 tabular-nums text-ink">
                        {t.fixed}
                        {t.conditional > 0 && <span className="text-ink-muted"> +{t.conditional}P</span>}
                      </span>
                    </li>
                  ))}
                </ul>
                <p className="mt-0.5 text-[0.6rem] text-ink-muted">
                  Summed from crypt <code>votes</code> annotations; “P” = conditional, not counted.
                </p>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
});
