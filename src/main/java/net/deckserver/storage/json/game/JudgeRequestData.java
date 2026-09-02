package net.deckserver.storage.json.game;

import lombok.Data;
import net.deckserver.game.enums.JudgeRequestCategory;
import net.deckserver.game.enums.JudgeRequestStatus;

import java.time.OffsetDateTime;

/**
 * Domain view of a {@code judge_request} row — what {@code JudgeRequestRepository}
 * returns and {@code JudgeService} works with. Parallel to {@link CommandErrorData}:
 * a plain carrier, no persistence annotations.
 */
@Data
public class JudgeRequestData {

    private Long id;
    private String gameId;
    private String gameName;
    private String tournamentName;
    private String requestedBy;
    private JudgeRequestCategory category;
    private JudgeRequestStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    /** Exactly what the player typed ("[Card Name]" markup intact) — shown back to the requester for editing. */
    private String rawDetails;
    /** {@code ParserService.parseGameChat} token form — what every viewer renders. */
    private String parsedDetails;
    private String resolvedBy;
    private OffsetDateTime resolvedAt;
    private String resolutionRaw;
    private String resolutionParsed;

    public boolean isTournament() {
        return tournamentName != null && !tournamentName.isBlank();
    }
}
