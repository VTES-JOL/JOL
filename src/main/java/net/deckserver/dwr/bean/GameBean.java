package net.deckserver.dwr.bean;

import lombok.Getter;

import java.util.List;

@Getter
public class GameBean {

    private final List<String> ping;
    private final List<String> pinged;
    private final int refresh;
    private final String hand;
    private final String globalNotes;
    private final String privateNotes;
    private final String label;
    private final String phase;
    private final List<String> turn;
    private final List<String> turns;
    private final String state;
    private final List<String> phases;
    private final boolean resetChat;
    private final boolean turnChanged;
    private final boolean player;
    private final boolean admin;
    private final boolean judge;
    private final String stamp;
    private final String name;
    private final int logLength;
    private final String currentPlayer;

    public GameBean(boolean isPlayer, boolean isAdmin, boolean isJudge, int refresh, String hand, String globalNotes, String privateNotes,
                    String label, String phase, boolean resetChat, boolean turnChanged, List<String> turn, List<String> turns, String state, List<String> phases,
                    List<String> ping, List<String> pinged, String stamp, String name, int logLength, String currentPlayer) {
        this.ping = ping;
        this.pinged = pinged;
        this.refresh = refresh;
        this.player = isPlayer;
        this.admin = isAdmin;
        this.judge = isJudge;
        this.hand = hand;
        this.globalNotes = globalNotes;
        this.privateNotes = privateNotes;
        this.label = label;
        this.phase = phase;
        this.resetChat = resetChat;
        this.turnChanged = turnChanged;
        this.turn = turn;
        this.turns = turns;
        this.state = state;
        this.phases = phases;
        this.stamp = stamp;
        this.name = name;
        this.logLength = logLength;
        this.currentPlayer = currentPlayer;
    }
}
