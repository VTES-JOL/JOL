package net.deckserver.dwr.bean;

import lombok.Getter;

import java.util.List;

@Getter
public class GameBean {

    private final List<String> ping;
    private int refresh = -1;
    private String hand;
    private String globalNotes;
    private String privateNotes;
    private String label;
    private String phase;
    private String[] turn;
    private String[] turns;
    private String state;
    private String[] phases;
    private boolean resetChat;
    private boolean turnChanged;
    private boolean player;
    private boolean admin;
    private boolean judge;
    private String stamp;
    private String name;
    private int logLength;
    private String currentPlayer;

    public GameBean(boolean isPlayer, boolean isAdmin, boolean isJudge, int refresh, String hand, String globalNotes, String privateNotes,
                    String label, String phase, boolean resetChat, boolean turnChanged, String[] turn, String[] turns, String state, String[] phases,
                    List<String> ping, String stamp, String name, int logLength, String currentPlayer) {
        this(ping, refresh);
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

    public GameBean(List<String> ping, int refresh) {
        this.ping = ping;
        this.refresh = refresh;
    }
}
