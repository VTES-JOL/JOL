package net.deckserver.dwr.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameBean {

    private int refresh = -1;

    private String hand = null;

    private String global = null;

    private String text = null;

    private String label = null;

    private String[] turn = null;

    private String[] turns = null;

    private List<String> ping;

    private List<String> pinged;

    private String state = null;

    private String[] phases = null;

    private String[] collapsed = null;

    private boolean resetChat;

    private boolean turnChanged;

    private boolean player;

    private boolean admin;

    private boolean judge;

    private String stamp;

    private String name;

    public GameBean(boolean isPlayer, boolean isAdmin, boolean isJudge, int refresh, String hand, String global, String text,
                    String label, boolean resetChat, boolean turnChanged, String[] turn, String[] turns, String state, String[] phases,
                    List<String> ping, String[] collapsed, String stamp, List<String> pinged, String name) {
        this(ping, refresh);
        this.player = isPlayer;
        this.admin = isAdmin;
        this.judge = isJudge;
        this.hand = hand;
        this.global = global;
        this.text = text;
        this.label = label;
        this.resetChat = resetChat;
        this.turnChanged = turnChanged;
        this.turn = turn;
        this.turns = turns;
        this.state = state;
        this.phases = phases;
        this.collapsed = collapsed;
        this.stamp = stamp;
        this.pinged = pinged;
        this.name = name;
    }

    public GameBean(List<String> ping, int refresh) {
        this.ping = ping;
        this.refresh = refresh;
    }

    public String[] getCollapsed() {
        return collapsed;
    }

    public String getHand() {
        return hand;
    }

    public String getGlobal() {
        return global;
    }

    public String[] getPhases() {
        return phases;
    }

    public int getRefresh() {
        return refresh;
    }

    public String getState() {
        return state;
    }

    public String getText() {
        return text;
    }

    public String[] getTurns() {
        return turns;
    }

    public String getLabel() {
        return label;
    }

    public boolean getTurnChanged() {
        return turnChanged;
    }

    public String[] getTurn() {
        return turn;
    }

    public boolean isResetChat() {
        return resetChat;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isPlayer() {
        return player;
    }

    public boolean isJudge() {
        return judge;
    }

    public String getStamp() {
        return stamp;
    }

    public List<String> getPing() {
        return ping;
    }

    public List<String> getPinged() {
        return pinged;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "GameBean{" +
                "refresh=" + refresh +
                ", text='" + text + '\'' +
                ", label='" + label + '\'' +
                ", turn=" + Arrays.toString(turn) +
                ", turns=" + Arrays.toString(turns) +
                ", phases=" + Arrays.toString(phases) +
                ", collapsed=" + Arrays.toString(collapsed) +
                ", admin=" + admin +
                ", resetChat=" + resetChat +
                ", turnChanged=" + turnChanged +
                ", player=" + player +
                ", stamp='" + stamp + '\'' +
                '}';
    }
}
