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
 * Tailwind Tailwind-based.
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
      <div className="flex items-center gap-1.5">
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
                'flex items-center gap-1 px-1.5 py-0.5 rounded text-[11px] font-medium transition-colors',
                status === true
                  ? 'bg-online/15 text-online cursor-default'
                  : isInvalid
                    ? 'bg-blood/15 text-blood-soft hover:bg-blood/25 cursor-pointer'
                    : 'bg-hover text-ink-muted cursor-default',
              ].join(' ')}
            >
              {status === true ? (
                <Check className="w-2.5 h-2.5" />
              ) : isInvalid ? (
                <X className="w-2.5 h-2.5" />
              ) : (
                <Minus className="w-2.5 h-2.5" />
              )}
              {label}
            </button>
          );
        })}
      </div>

      {modal &&
        createPortal(
          <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
            onClick={() => setModal(null)}
          >
            <div
              className="relative flex flex-col w-full max-w-sm rounded-lg border border-line-accent bg-surface shadow-xl"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between px-4 py-3 border-b border-line/75 bg-panel/45">
                <div className="flex items-center gap-2">
                  <h2 className="text-sm font-medium text-ink tracking-wide">{modal.label}</h2>
                  <Badge variant="blood">
                    <X className="w-2.5 h-2.5 inline mr-0.5" />
                    Invalid
                  </Badge>
                </div>
                <button
                  onClick={() => setModal(null)}
                  className="p-1 rounded hover:bg-hover transition-colors cursor-pointer"
                >
                  <X className="w-4 h-4 text-ink-muted" />
                </button>
              </div>

              <div className="px-4 py-3 min-h-[60px]">
                {modal.errors.length === 0 ? (
                  <p className="text-xs text-ink-muted">No details recorded.</p>
                ) : (
                  <ul className="space-y-1.5">
                    {modal.errors.map((err, i) => (
                      <li key={i} className="flex items-start gap-2 text-xs text-blood-soft">
                        <span className="mt-px shrink-0 text-blood/60">·</span>
                        <span>{err}</span>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div className="flex justify-end px-4 py-3 border-t border-line/75">
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
