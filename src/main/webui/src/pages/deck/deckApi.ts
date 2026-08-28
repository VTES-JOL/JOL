// Deck-editor API calls, split out from the generic api/client.ts wrapper
// (per CLAUDE.md's "page-specific API module" convention). Backs the new
// Tailwind deck editor being migrated onto /jol/deck.
import { api } from '../../api/client';
import type { CardDetail, DeckPage, ImportPreview } from '../../api/types';

export const deckApi = {
  /** Up to 5 best card-name matches. Empty query short-circuits to []. */
  cardAutocomplete: (query: string): Promise<CardDetail[]> =>
    query.trim() ? api.get<CardDetail[]>(`/cards/autocomplete?q=${encodeURIComponent(query)}`) : Promise.resolve([]),

  /** Batch card details by id — used to enrich a deck's entries on load. */
  cardDetails: (ids: string[]): Promise<CardDetail[]> =>
    ids.length ? api.get<CardDetail[]>(`/cards/details?ids=${ids.join(',')}`) : Promise.resolve([]),

  /** Parse a pasted deck list (KRCG JSON or JOL text) into a resolved preview. */
  previewImport: (text: string): Promise<ImportPreview> => api.postText<ImportPreview>('/cards/preview', text),

  /** Create a deck from a confirmed import preview. */
  importDeck: (name: string, entries: Array<{ cardId: string; count: number }>, comment: string | null): Promise<DeckPage> =>
    api.post<DeckPage>('/decks/player/import', { name, comment, entries }),
};
