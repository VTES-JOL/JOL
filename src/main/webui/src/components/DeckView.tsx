import { useMemo, useRef } from 'react';
import { ChevronDown } from 'lucide-react';
import type { CardDetail, Deck } from '../api/types';
import { useCardTooltips } from '../hooks/useCardTooltips';
import { groupEntries } from '../pages/deck/deckKit';
import { enrichEntries, entriesFromDeck } from '../pages/deck/deckEntries';
import { DeckCardRow } from '../pages/deck/DeckCardRow';
import { TypeIcon } from './ui/icons';

/**
 * Read-only deck view — the deck editor's grouped, collapsible card list
 * (type sections, `path · clan · name · cost … disciplines · count` rows, all
 * with icons) minus the add/remove controls. Shared by the lobby registration
 * preview, the in-game deck panel and the tournament registration preview.
 *
 * Renders at its natural height with every group open; the caller owns any
 * scroll container. Card names are `a.card-name` links, so a `useCardTooltips`
 * host (mounted here) gives them the same hover-image preview they have
 * everywhere else in the app.
 */
export function DeckView({ deck, details }: { deck: Deck; details?: Record<string, CardDetail> }) {
  const ref = useRef<HTMLDivElement>(null);
  const detailMap = useMemo(() => new Map(Object.entries(details ?? {})), [details]);
  const groups = useMemo(
    () => groupEntries(enrichEntries(entriesFromDeck(deck), detailMap)),
    [deck, detailMap],
  );
  useCardTooltips(ref, [deck, detailMap]);

  return (
    <div ref={ref} className="flex flex-col text-ink-secondary">
      {groups.map((group) => (
        <details key={group.key} open className="group border-b border-line/50 last:border-b-0">
          <summary className="flex items-center gap-2 px-3 py-2 cursor-pointer list-none select-none bg-panel/30 hover:bg-hover/40 transition-colors">
            <ChevronDown className="w-3 h-3 text-ink-muted shrink-0 -rotate-90 transition-transform group-open:rotate-0" />
            <span className="text-xs font-semibold text-ink">{group.key}</span>
            {group.key !== 'Crypt' && (
              <span className="flex items-center gap-0.5 shrink-0">
                {group.key.split('/').map((t) => (
                  <TypeIcon key={t} type={t} size={16} />
                ))}
              </span>
            )}
            <span className="ml-auto inline-flex items-center px-1.5 py-0.5 rounded-full bg-hover border border-line/60 text-[11px] font-semibold tabular-nums text-ink-secondary leading-none">
              {group.total}
            </span>
          </summary>
          <div>
            {group.entries.map((entry) => (
              <DeckCardRow key={entry.cardId} entry={entry} detail={detailMap.get(entry.cardId)} linkCards />
            ))}
          </div>
        </details>
      ))}
    </div>
  );
}
