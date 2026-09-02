package net.deckserver.rest.bean;

import net.deckserver.storage.json.game.JudgeRequestData;

import java.util.List;

/**
 * The judges page payload: the outstanding queue (oldest first) and the ruling
 * history (most recent first).
 */
public record JudgeQueueBean(List<Entry> open, List<Entry> history) {

    public record Entry(
            long id,
            String gameId,
            String gameName,
            String tournamentName,
            boolean tournament,
            String requester,
            String category,
            String status,
            String createdAt,
            /** Parsed token form — rendered with the same card-link component as game chat. */
            String details,
            /**
             * Whether this judge may rule: they are not a seated player in that game,
             * and — until tournament→judge assignment exists — the game is not a
             * tournament game.
             */
            boolean canRule,
            String resolvedBy,
            String resolvedAt,
            String resolution
    ) {
        public static Entry of(JudgeRequestData d, boolean canRule) {
            return new Entry(
                    d.getId(),
                    d.getGameId(),
                    d.getGameName(),
                    d.getTournamentName(),
                    d.isTournament(),
                    d.getRequestedBy(),
                    d.getCategory().name(),
                    d.getStatus().name(),
                    d.getCreatedAt() != null ? d.getCreatedAt().toString() : null,
                    d.getParsedDetails(),
                    canRule,
                    d.getResolvedBy(),
                    d.getResolvedAt() != null ? d.getResolvedAt().toString() : null,
                    d.getResolutionParsed());
        }
    }
}
