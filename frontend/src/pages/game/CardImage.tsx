import { useEffect, useState } from 'react';
import { getBaseUrl } from '../../api/config';

// Same static asset useCardTooltips already fetches for card-name hover
// previews (${baseUrl}/[secured/]images/{cardId}) — reused here so the
// card-modal.jsp-derived action/play modals show the card's art instead of
// text-and-buttons only.
export function CardImage({ cardId, secured, name }: { cardId: string; secured: boolean; name: string }) {
  const [src, setSrc] = useState<string | null>(null);

  useEffect(() => {
    setSrc(null);
    getBaseUrl().then((baseUrl) => setSrc(`${baseUrl}/${secured ? 'secured/' : ''}images/${cardId}`));
  }, [cardId, secured]);

  if (!cardId) return null;

  return (
    <div className="d-flex justify-content-center mb-2">
      {src ? (
        <img src={src} alt={name} width={200} height={286} className="rounded shadow-sm" />
      ) : (
        <div className="bg-body-secondary rounded" style={{ width: 200, height: 286 }} />
      )}
    </div>
  );
}
