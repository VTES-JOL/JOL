import { useState } from 'react';
import { createPortal } from 'react-dom';
import { Check, Minus, X } from 'lucide-react';
import type { DeckValidity } from '../../api/types';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';

/**
 * STANDARD / DUEL / V5 validity chips, driven straight off the deck page's
 * `formatValidity` map (no extra fetch). An invalid chip is clickable and
 * opens a modal listing the recorded errors. Ported from jol-quarkus;
 * Tailwind `jt:` -prefixed.
 */
const FORMATS = [
  { key: 'STANDARD', label: 'Standard' },
  { key: 'DUEL', label: 'Duel' },
  { key: 'V5', label: 'V5' },
] as const;

interface Props {
  validity: Record<string, DeckValidity>;
  className?: string;
}

export function FormatValidityBadges({ validity, className = '' }: Props) {
  const [modal, setModal] = useState<{ label: string; errors: string[] } | null>(null);

  return (
    <div className={className}>
      <div className="jt:flex jt:items-center jt:gap-1.5">
        {FORMATS.map(({ key, label }) => {
          const v = validity[key];
          const status = v?.valid; // true | false | undefined
          const isInvalid = status === false;
          return (
            <button
              key={key}
              type="button"
              onClick={() => isInvalid && setModal({ label, errors: v.errors })}
              aria-disabled={!isInvalid}
              title={isInvalid ? `${label}: invalid — click for details` : undefined}
              className={[
                'jt:flex jt:items-center jt:gap-1 jt:px-1.5 jt:py-0.5 jt:rounded jt:text-[11px] jt:font-medium jt:transition-colors',
                status === true
                  ? 'jt:bg-online/15 jt:text-online jt:cursor-default'
                  : isInvalid
                    ? 'jt:bg-blood/15 jt:text-blood-soft jt:hover:bg-blood/25 jt:cursor-pointer'
                    : 'jt:bg-hover jt:text-ink-muted jt:cursor-default',
              ].join(' ')}
            >
              {status === true ? (
                <Check className="jt:w-2.5 jt:h-2.5" />
              ) : isInvalid ? (
                <X className="jt:w-2.5 jt:h-2.5" />
              ) : (
                <Minus className="jt:w-2.5 jt:h-2.5" />
              )}
              {label}
            </button>
          );
        })}
      </div>

      {modal &&
        createPortal(
          <div
            className="jt-scope jt:fixed jt:inset-0 jt:z-50 jt:flex jt:items-center jt:justify-center jt:p-4 jt:bg-black/60 jt:backdrop-blur-sm"
            onClick={() => setModal(null)}
          >
            <div
              className="jt:relative jt:flex jt:flex-col jt:w-full jt:max-w-sm jt:rounded-lg jt:border jt:border-line-accent jt:bg-surface jt:shadow-xl"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="jt:flex jt:items-center jt:justify-between jt:px-4 jt:py-3 jt:border-b jt:border-line/75 jt:bg-panel/45">
                <div className="jt:flex jt:items-center jt:gap-2">
                  <h2 className="jt:text-sm jt:font-medium jt:text-ink jt:tracking-wide">{modal.label}</h2>
                  <Badge variant="blood">
                    <X className="jt:w-2.5 jt:h-2.5 jt:inline jt:mr-0.5" />
                    Invalid
                  </Badge>
                </div>
                <button
                  onClick={() => setModal(null)}
                  className="jt:p-1 jt:rounded jt:hover:bg-hover jt:transition-colors jt:cursor-pointer"
                >
                  <X className="jt:w-4 jt:h-4 jt:text-ink-muted" />
                </button>
              </div>

              <div className="jt:px-4 jt:py-3 jt:min-h-[60px]">
                {modal.errors.length === 0 ? (
                  <p className="jt:text-xs jt:text-ink-muted">No details recorded.</p>
                ) : (
                  <ul className="jt:space-y-1.5">
                    {modal.errors.map((err, i) => (
                      <li key={i} className="jt:flex jt:items-start jt:gap-2 jt:text-xs jt:text-blood-soft">
                        <span className="jt:mt-px jt:shrink-0 jt:text-blood/60">·</span>
                        <span>{err}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div className="jt:flex jt:justify-end jt:px-4 jt:py-3 jt:border-t jt:border-line/75">
                <Button variant="secondary" size="sm" onClick={() => setModal(null)}>
                  Close
                </Button>
              </div>
            </div>
          </div>,
          document.body,
        )}
    </div>
  );
}
