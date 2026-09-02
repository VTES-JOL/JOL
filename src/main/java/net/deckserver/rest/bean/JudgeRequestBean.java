package net.deckserver.rest.bean;

import net.deckserver.storage.json.game.JudgeRequestData;

/**
 * The single OPEN judge request for a game, as carried on {@link GameSnapshot}.
 * Viewer-aware: {@code rawDetails} and the {@code can*} flags depend on who is
 * looking.
 */
public record JudgeRequestBean(
        long id,
        String requester,
        String category,
        String createdAt,
        String updatedAt,
        /** Parsed token form ([card:id:name] …) — rendered by every viewer. */
        String details,
        /** Verbatim text the requester typed — only populated for the requester (edit pre-fill), else null. */
        String rawDetails,
        String status,
        boolean canEdit,
        boolean canRetract,
        boolean canResolve
) {
    public static JudgeRequestBean of(JudgeRequestData d, String viewer, boolean viewerCanResolve) {
        boolean isRequester = d.getRequestedBy().equals(viewer);
        return new JudgeRequestBean(
                d.getId(),
                d.getRequestedBy(),
                d.getCategory().name(),
                d.getCreatedAt() != null ? d.getCreatedAt().toString() : null,
                d.getUpdatedAt() != null ? d.getUpdatedAt().toString() : null,
                d.getParsedDetails(),
                isRequester ? d.getRawDetails() : null,
                d.getStatus().name(),
                isRequester,
                isRequester,
                viewerCanResolve);
    }
}
