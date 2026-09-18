import { Fragment } from 'react';
import { PHASES } from './phases';

// All five phases render always — past dimmed, current lit, future quiet — so
// the turn's shape is visible at a glance; `selectablePhases` only gates which
// you can jump to. When it isn't the viewer's turn the whole stepper is muted
// (rules): a non-active player is watching someone else's progression, not
// driving one.
export function PhaseStepper({
  current,
  selectablePhases,
  canSelect,
  active,
  onSelect,
}: {
  current: string;
  /** From GameSnapshot.phases — current-and-forward on your turn, else just current. */
  selectablePhases: string[];
  canSelect: boolean;
  /** Is it the viewer's turn — drives the muted treatment. */
  active: boolean;
  onSelect: (phase: string) => void;
}) {
  const currentIndex = PHASES.indexOf(current as (typeof PHASES)[number]);

  return (
    <div
      role="group"
      aria-label="Turn phase"
      aria-disabled={!canSelect}
      // F17: the "not your turn" cue used to be a flat opacity-55 wash over
      // the whole group, which stacked on top of --jt-ink-muted (already
      // tuned to just clear AA on its own — see tailwind.css) and dropped
      // past-phase text below it. The muted/secondary text tokens are
      // contrast-safe as-is, so dim only the border (decorative) instead of
      // the text; the current-phase pill stays full accent/white either way.
      className={`inline-flex items-center rounded-full border px-1 py-0.5 text-xs ${
        active ? 'border-line' : 'border-line/60'
      }`}
    >
      {PHASES.map((phase, i) => {
        const isCurrent = i === currentIndex;
        const isPast = currentIndex >= 0 && i < currentIndex;
        const selectable = canSelect && selectablePhases.includes(phase);
        return (
          <Fragment key={phase}>
            {i > 0 && <span className="px-0.5 text-ink-muted">›</span>}
            <button
              type="button"
              disabled={!selectable}
              aria-current={isCurrent ? 'step' : undefined}
              onClick={() => onSelect(phase)}
              className={`rounded-full px-1.5 py-0.5 transition-colors md:px-2 ${
                isCurrent
                  ? 'bg-accent font-semibold text-white'
                  : isPast
                    ? 'text-ink-muted'
                    : 'text-ink-secondary'
              } ${selectable && !isCurrent ? 'hover:bg-hover cursor-pointer' : ''} ${
                !selectable ? 'cursor-default' : ''
              }`}
            >
              {/* NF3 (D32): 3-letter labels below md so the HUD turn row never
                  wraps on a phone; full names from md up. The current phase
                  keeps its full name at every width so the readout stays
                  unambiguous. */}
              {isCurrent ? (
                <span>{phase}</span>
              ) : (
                <>
                  <span className="md:hidden">{phase.slice(0, 3)}</span>
                  <span className="hidden md:inline">{phase}</span>
                </>
              )}
            </button>
          </Fragment>
        );
      })}
    </div>
  );
}
