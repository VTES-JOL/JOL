import { useEffect, useState } from 'react';
import { Image as ImageIcon } from 'lucide-react';
import { getBaseUrl } from '../../api/config';
import type { CardSnapshot } from '../../api/types';
import { useTextMode } from './textMode';
import { Clan } from './Clan';
import { Sect } from './Sect';
import { Path } from './Path';

// Same static asset useCardTooltips already fetches for card-name hover
// previews (${baseUrl}/[secured/]images/{cardId}) — reused here so the
// card-modal.jsp-derived action/play modals show the card's art instead of
// text-and-buttons only.
//
// §6c text mode — when the player turned image tooltips off (or is on a
// touch viewport), skip the fetch entirely and render the identity fields
// off the snapshot instead. Every field is already on CardSnapshot.
export function CardImage({
  cardId,
  secured,
  name,
  card,
}: {
  cardId: string;
  secured: boolean;
  name: string;
  card?: CardSnapshot;
}) {
  const textMode = useTextMode();
  const [src, setSrc] = useState<string | null>(null);
  // Distinct from `src === null` (still loading) — set only once the <img>
  // itself reports a failed load, e.g. a card id missing from the local
  // static/ mirror in dev (see serveCardAssets.ts).
  const [broken, setBroken] = useState(false);

  useEffect(() => {
    if (textMode) return;
    setSrc(null);
    setBroken(false);
    getBaseUrl().then((baseUrl) => setSrc(`${baseUrl}/${secured ? 'secured/' : ''}images/${cardId}`));
  }, [cardId, secured, textMode]);

  if (!cardId) return null;

  if (textMode) {
    return <CardTextSummary name={name} card={card} />;
  }

  return (
    <div className="flex justify-center mb-2">
      {src && !broken ? (
        <img src={src} alt={name} width={200} height={286} className="rounded shadow-sm" onError={() => setBroken(true)} />
      ) : (
        <div
          className="bg-panel rounded flex flex-col items-center justify-center gap-2 text-ink-muted"
          style={{ width: 200, height: 286 }}
        >
          {broken && (
            <>
              <ImageIcon size={40} />
              <span className="px-2 text-center text-sm">{name || 'Image unavailable'}</span>
            </>
          )}
        </div>
      )}
    </div>
  );
}

function CardTextSummary({ name, card }: { name: string; card?: CardSnapshot }) {
  const counterText =
    card && ((card.counters ?? 0) > 0 || (card.capacity ?? 0) > 0)
      ? `${card.counters}${(card.capacity ?? 0) > 0 ? ` / ${card.capacity}` : ''}`
      : null;
  return (
    <div className="mb-2 rounded border border-line bg-panel p-3">
      <div className="flex items-baseline gap-2">
        <span className="font-semibold text-ink">{name}</span>
        {card?.advanced && <i className="icon adv" />}
        {counterText && (
          <span className="ml-auto rounded-full bg-blood px-2 py-0.5 text-xs font-medium text-white tabular-nums">
            {counterText}
          </span>
        )}
      </div>
      {card && (card.disciplines?.length ?? 0) > 0 && (
        <div className="mt-1.5 flex items-center gap-1">
          {card.disciplines!.map((disc) => (
            <span key={disc} className={`icon ${disc}`} />
          ))}
        </div>
      )}
      {card && (card.clan || card.sect || card.path || card.votes) && (
        <div className="mt-1.5 flex items-center gap-2 text-xs text-ink-muted">
          <Path value={card.path} />
          <Sect value={card.sect} />
          <Clan value={card.clan} />
          {card.votes && card.votes !== '0' && <span>{card.votes} votes</span>}
        </div>
      )}
    </div>
  );
}
