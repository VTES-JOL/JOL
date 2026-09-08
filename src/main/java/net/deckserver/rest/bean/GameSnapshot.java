package net.deckserver.rest.bean;

import lombok.Builder;
import lombok.Getter;
import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.CommandErrorData;

import java.util.List;

/**
 * Structural, viewer-aware replacement for GameBean's HTML-string hand/state
 * fields — the React game page's equivalent of state.jsp's whole render tree.
 * Built fresh on every request (see GameSnapshotFactory) rather than using
 * GameView's dirty-flag partial-update tracking: that tracking exists to avoid
 * re-running expensive JSP rendering, which doesn't apply to cheap JSON
 * serialization, so this intentionally always returns the full current state.
 */
@Getter
@Builder
public class GameSnapshot {
    private final String id;
    private final String name;
    private final List<PlayerSnapshot> players;
    /** Player names in seating order — drives the per-actor accent colour in the chat log. */
    private final List<String> seating;
    private final String currentPlayer;
    private final String edgePlayer;
    private final String turn;
    private final String turnLabel;
    private final String phase;
    private final List<String> phases;
    private final List<String> turns;
    private final List<String> pingOptions;
    private final boolean player;
    private final boolean admin;
    private final boolean judge;
    private final String globalNotes;
    private final String privateNotes;
    private final String edgeColor;
    private final String edgeTextColor;
    /** Human-readable result message for a submit / end-turn (may be null / empty). */
    private final String status;
    /**
     * True when the engine refused to apply this submit / end-turn (a command
     * threw, not the caller's turn, not authorised). A duplicate-submit hit is
     * NOT a rejection. Always false on a plain {@code /view} GET and the judge
     * endpoints.
     */
    private final boolean rejected;
    /**
     * Per-game monotonic snapshot version (see {@code JolAdmin.getGameStamp}).
     * Bumps on every change a refetch would show. The WS game-update frame
     * carries the same value under {@code "stamp"} so a client already holding
     * this version can skip the refetch. Was an ISO timestamp string before
     * Cycle 3 — now a long.
     */
    private final long stamp;
    /** The single OPEN "call a judge" request for this game, or null if none. Viewer-aware. */
    private final JudgeRequestBean judgeRequest;
    /** The one open action / response window (rules R1), or null when nothing is declared. */
    private final PendingActionBean pendingAction;
    /**
     * The current turn's game-chat lines — the same list {@code GET
     * /game/{id}/history?turn=<turnLabel>} would return for this viewer
     * (judge-only fields stripped for seated players / spectators). Carried on
     * the snapshot so the chat log updates in the same round trip as the board
     * instead of the client making a second request after every command.
     */
    private final List<ChatData> chat;
    /**
     * Failed command attempts for the current turn — populated only for a judge
     * watching a game they are not seated in (an empty list otherwise), mirroring
     * {@code GET /game/{id}/command-errors}.
     */
    private final List<CommandErrorData> commandErrors;
}
