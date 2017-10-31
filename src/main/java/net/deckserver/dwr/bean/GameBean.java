package net.deckserver.dwr.bean;

import java.util.Arrays;

public class GameBean {

    private int refresh = -1;

    private String hand = null;

    private String global = null;

    private String text = null;

    private String label = null;

    private String[] turn = null;

    private String[] turns = null;

    private String[] pingkeys = null;

    private String[] pingvalues = null;

    private String state = null;

    private String[] phases = null;

    private String[] collapsed = null;

    private boolean resetChat;

    private boolean turnChanged;

    private boolean player;

    private boolean admin;

    private boolean judge;

    private String stamp;

    public GameBean(boolean isPlayer, boolean isAdmin, boolean isJudge, int refresh, String hand, String global, String text,
                    String label, boolean resetChat, boolean turnChanged, String[] turn, String[] turns, String state, String[] phases,
                    String[] pingkeys, String[] pingvalues, String[] collapsed, String stamp) {
        this(pingkeys, pingvalues, refresh);
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
    }

    public GameBean(String[] pingkeys, String[] pingvalues, int refresh) {
        this.pingkeys = pingkeys;
        this.pingvalues = pingvalues;
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

    public String[] getPingkeys() {
        return pingkeys;
    }

    public String[] getPingvalues() {
        return pingvalues;
    }

    public String getStamp() {
        return stamp;
    }

    @Override
    public String toString() {
        return "GameBean{" +
                "refresh=" + refresh +
                ", text='" + text + '\'' +
                ", label='" + label + '\'' +
                ", turn=" + Arrays.toString(turn) +
                ", turns=" + Arrays.toString(turns) +
                ", pingkeys=" + Arrays.toString(pingkeys) +
                ", pingvalues=" + Arrays.toString(pingvalues) +
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
