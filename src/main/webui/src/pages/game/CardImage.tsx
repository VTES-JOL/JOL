import { useEffect, useState } from 'react';
import { getBaseUrl } from '../../api/config';

// Same static asset useCardTooltips already fetches for card-name hover
// previews (${baseUrl}/[secured/]images/{cardId}) — reused here so the
// card-modal.jsp-derived action/play modals show the card's art instead of
// text-and-buttons only.
export function CardImage({ cardId, secured, name }: { cardId: string; secured: boolean; name: string }) {
  const [src, setSrc] = useState<string | null>(null);
  // Distinct from `src === null` (still loading) — set only once the <img>
  // itself reports a failed load, e.g. a card id missing from the local
  // static/ mirror in dev (see serveCardAssets.ts).
  const [broken, setBroken] = useState(false);

  useEffect(() => {
    setSrc(null);
    setBroken(false);
    getBaseUrl().then((baseUrl) => setSrc(`${baseUrl}/${secured ? 'secured/' : ''}images/${cardId}`));
  }, [cardId, secured]);

  if (!cardId) return null;

  return (
    <div className="d-flex justify-content-center mb-2">
      {src && !broken ? (
        <img src={src} alt={name} width={200} height={286} className="rounded shadow-sm" onError={() => setBroken(true)} />
      ) : (
        <div
          className="bg-body-secondary rounded d-flex flex-column align-items-center justify-content-center gap-2 text-body-secondary"
          style={{ width: 200, height: 286 }}
        >
          {broken && (
            <>
              <i className="bi bi-image fs-1" />
              <span className="px-2 text-center small">{name || 'Image unavailable'}</span>
            </>
          )}
        </div>
      )}
    </div>
  );
}
