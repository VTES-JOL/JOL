package net.deckserver.game.cards;

import java.time.Instant;

/**
 * Snapshot of the current {@link CardRegistry} load — returned by the admin
 * reload / status endpoints.
 */
public record RegistryStatus(
        int cardCount,
        int cryptCount,
        int libraryCount,
        int lookupKeyCount,
        Instant loadedAt,
        String sourceDir
) {
}
