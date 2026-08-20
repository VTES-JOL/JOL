import { getBaseUrl } from '../../api/config';
import type { CardDefinition } from '../../api/types';

// Mirrors showPlayCardModal()/showCardModal()'s `$.get(BASE_URL + '/json/' +
// cardId)` — a static asset (see serveCardAssets.ts / useCardTooltips), not
// part of this app's own REST API, so it's fetched directly rather than
// through api/client.ts.
export function fetchCardDefinition(cardId: string, secured: boolean): Promise<CardDefinition> {
  return getBaseUrl().then((baseUrl) =>
    fetch(`${baseUrl}/${secured ? 'secured/' : ''}json/${cardId}`).then((res) => {
      if (!res.ok) throw new Error(`Failed to load card definition for ${cardId}`);
      return res.json() as Promise<CardDefinition>;
    }),
  );
}
