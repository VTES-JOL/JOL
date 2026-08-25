package net.deckserver.rest.bean;

import lombok.Builder;
import lombok.Getter;

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
    private final String status;
    private final String stamp;
}
