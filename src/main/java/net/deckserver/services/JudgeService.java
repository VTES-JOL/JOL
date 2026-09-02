package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import net.deckserver.game.enums.JudgeRequestCategory;
import net.deckserver.jpa.repository.JudgeRequestRepository;
import net.deckserver.storage.json.game.JudgeRequestData;

import java.util.ArrayList;
import java.util.List;

/**
 * "Call a judge" requests raised from inside a game. No in-memory cache — reads
 * go straight to JPA on every call (low frequency, like {@link DeckService}), so
 * writes use {@code jpaWriteAlways} and must persist even in test mode.
 *
 * <p>Details text is stored twice: {@code rawDetails} exactly as the player
 * typed it (shown back for editing) and {@code parsedDetails} run through
 * {@link ParserService#parseGameChat} so {@code [Card Name]} markup resolves to
 * the same tokens the game chat client already renders.
 *
 * <p>System chat lines and WebSocket notifications for these transitions are the
 * caller's responsibility (see {@code GameActionResource}) — this service only
 * owns the {@code judge_request} table.
 */
@Singleton
@Startup
public class JudgeService extends PersistedService {

    private static final int HISTORY_LIMIT = 200;

    private static final JudgeRequestRepository repository = new JudgeRequestRepository();

    JudgeService() {
        super("JudgeService", 0); // write-through, no scheduled persistence
    }

    private static JudgeService instance() {
        return resolve(JudgeService.class, JudgeService::new);
    }

    public static PersistedService getInstance() {
        return instance();
    }

    private static String parse(String text) {
        return ParserService.parseGameChat(ParserService.sanitizeText(text));
    }

    /**
     * Raise a new request. Throws {@link IllegalStateException} if the game
     * already has an OPEN one (also enforced by a partial unique index).
     */
    public static JudgeRequestData createRequest(String gameId, String gameName, String tournamentName,
                                                 String requestedBy, JudgeRequestCategory category, String rawDetails) {
        if (instance().jpaRead(em -> repository.findOpenForGame(em, gameId)) != null) {
            throw new IllegalStateException("A judge has already been called for this game");
        }
        String parsed = parse(rawDetails);
        JudgeRequestData[] holder = new JudgeRequestData[1];
        boolean ok = instance().jpaWriteAlways(em ->
                holder[0] = repository.insert(em, gameId, gameName, tournamentName, requestedBy, category, rawDetails, parsed));
        if (!ok || holder[0] == null) {
            throw new IllegalStateException("Failed to raise judge request");
        }
        return holder[0];
    }

    /** Edit an OPEN request's details/category. Returns the updated row, or null if it is no longer OPEN. */
    public static JudgeRequestData editRequest(long id, JudgeRequestCategory category, String rawDetails) {
        String parsed = parse(rawDetails);
        int[] updated = new int[1];
        instance().jpaWriteAlways(em -> updated[0] = repository.updateDetails(em, id, category, rawDetails, parsed));
        return updated[0] > 0 ? getById(id) : null;
    }

    /** Retract an OPEN request. Returns false if it is no longer OPEN. */
    public static boolean retractRequest(long id) {
        int[] updated = new int[1];
        instance().jpaWriteAlways(em -> updated[0] = repository.retract(em, id));
        return updated[0] > 0;
    }

    /**
     * Resolve an OPEN request with the judge's notes. Returns the resolved row,
     * or null if it was already resolved or retracted (first-to-resolve wins).
     */
    public static JudgeRequestData resolveRequest(long id, String judge, String rawNotes) {
        String parsed = rawNotes == null || rawNotes.isBlank() ? null : parse(rawNotes);
        int[] updated = new int[1];
        instance().jpaWriteAlways(em -> updated[0] = repository.resolve(em, id, judge, rawNotes, parsed));
        return updated[0] > 0 ? getById(id) : null;
    }

    public static JudgeRequestData getById(long id) {
        return instance().jpaRead(em -> repository.findById(em, id));
    }

    public static JudgeRequestData getOpenForGame(String gameId) {
        return instance().jpaRead(em -> repository.findOpenForGame(em, gameId));
    }

    public static List<JudgeRequestData> listOpen() {
        List<JudgeRequestData> result = instance().jpaRead(repository::listOpen);
        return result != null ? result : new ArrayList<>();
    }

    public static List<JudgeRequestData> listResolved() {
        List<JudgeRequestData> result = instance().jpaRead(em -> repository.listResolved(em, HISTORY_LIMIT));
        return result != null ? result : new ArrayList<>();
    }

    public static long countOpen() {
        Long count = instance().jpaRead(repository::countOpen);
        return count != null ? count : 0L;
    }

    @Override
    protected void persist() {
        // Write-through — nothing to flush.
    }

    @Override
    protected void load() {
        // No in-memory state — reads hit JPA directly.
    }
}
