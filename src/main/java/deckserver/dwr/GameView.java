package deckserver.dwr;

import deckserver.client.JolAdmin;
import deckserver.client.JolGame;
import deckserver.dwr.bean.GameBean;
import deckserver.game.turn.GameAction;
import deckserver.util.HandParams;
import deckserver.util.RefreshInterval;
import org.directwebremoting.WebContextFactory;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import static org.slf4j.LoggerFactory.getLogger;

public class GameView {

    private static final Logger logger = getLogger(GameView.class);
    boolean stateChanged = true;
    boolean phaseChanged = true;
    boolean pingChanged = true;
    boolean globalChanged = true;
    boolean turnChanged = true;
    boolean resetChat = true;
    private String name;
    private String player;
    private boolean isPlayer = false;
    private boolean isAdmin = false;
    private Collection<String> chats = new ArrayList<>();
    private Collection<String> collapsed = new HashSet<>();

    public GameView(String name, String player) {
        this.name = name;
        this.player = player;
        init();
    }

    private void init() {
        JolAdmin admin = JolAdmin.INSTANCE;
        JolGame game = admin.getGame(name);
        GameAction[] actions = game.getActions(game.getCurrentTurn());
        for (GameAction action : actions) {
            addChat(action.getText());
        }
        String[] players = game.getPlayers();
        for (int i = 0; i < players.length; ) {
            boolean active = players[i].equals(player);
            if (active)
                isPlayer = true;
            boolean ousted = game.getPool(players[i]) < 1;
            i++;
            collapsed.add("t" + i);
            collapsed.add("a" + i);
            if (ousted) {
                collapsed.add("r" + i);
            }
            if (!active || ousted) {
                collapsed.add("i" + i);
            }
        }
        if (!isPlayer && (admin.getOwner(name).equals(player))) {
            isAdmin = true;
        }
    }

    public synchronized GameBean create() {
        JolAdmin admin = JolAdmin.INSTANCE;
        HttpServletRequest request = WebContextFactory.get()
                .getHttpServletRequest();
        JolGame game = admin.getGame(name);

        if (isPlayer) {
            admin.recordAccess(name, player);
        }

        int refresh = -1;

        if (player != null && admin.doInteractive(player)) {
            Date stamp = admin.getGameTimeStamp(name);
            refresh = RefreshInterval.calc(stamp);
        }

        String[] pingvalues = null;
        String[] pingkeys = null;
        String hand = null;
        String global = null;
        String text = null;
        String label = null;
        String[] turn = null;
        String[] turns = null;
        String state = null;
        String[] phases = null;
        String[] collapsed = null;

        if (pingChanged && (isPlayer || isAdmin)) {
            pingvalues = game.getPlayers();
            pingkeys = new String[pingvalues.length];
            for (int i = 0; i < pingvalues.length; i++) {
                pingkeys[i] = pingvalues[i] + "("
                        + game.getPingTag(pingvalues[i]) + ")";
            }
        }

        if (isPlayer && stateChanged) {
            try {
                HandParams h = new HandParams(game, player, "Hand", JolGame.HAND);
                request.setAttribute("hparams", h);
                request.setAttribute("game", game);
                hand = WebContextFactory.get().forwardToString(
                        "/WEB-INF/jsps/hand.jsp");
            } catch (Exception e) {
                logger.error("Error retrieving hand {}", e);
                hand = "Error retrieving hand.";
            }
        }

        if (globalChanged) {
            global = game.getGlobalText();
            if (isPlayer)
                text = game.getPlayerText(player);
        }

        if (phaseChanged) {
            label = game.getCurrentTurn() + " " + game.getPhase();
        }

        if (chats.size() > 0) {
            turn = chats.toArray(new String[0]);
        }

        if (turnChanged) {
            resetChat = true;
            String[] tmpturns = game.getTurns();
            turns = new String[tmpturns.length];
            for (int i = 0, j = turns.length; i < turns.length; i++)
                turns[i] = tmpturns[--j];
        }

        if (stateChanged) {
            try {
                request.setAttribute("game", game);
                state = WebContextFactory.get().forwardToString(
                        "/WEB-INF/jsps/state.jsp");
            } catch (Exception e) {
                logger.error("Error retrieving state {}", e);
                hand = "Error retrieving state.";
            }
        }

        // pending use phaseChanged here?
        if (isPlayer && game.getActivePlayer().equals(player)) {
            boolean show = false;
            Collection<String> c = new ArrayList<>();
            String phase = game.getPhase();
            for (int i = 0; i < JolGame.TURN_PHASES.length; i++) {
                if (phase.equals(JolGame.TURN_PHASES[i]))
                    show = true;
                if (show)
                    c.add(JolGame.TURN_PHASES[i]);
            }
            phases = c.toArray(new String[0]);
        }

        if (stateChanged) {
            collapsed = getCollapsed();
        }

        boolean chatReset = resetChat;
        boolean tc = turnChanged;
        clearAccess();
        String stamp = Utils.getDate();
        return new GameBean(isPlayer, isAdmin, refresh, hand, global, text, label,
                chatReset, tc, turn, turns, state, phases, pingkeys, pingvalues,
                collapsed, stamp);
    }

    public synchronized void clearAccess() {
        globalChanged = phaseChanged = stateChanged = pingChanged = turnChanged = false;
        chats.clear();
        resetChat = false;
    }

    public synchronized void globalChanged() {
        globalChanged = true;
    }

    public synchronized void phaseChanged() {
        phaseChanged = true;
    }

    public synchronized void stateChanged() {
        stateChanged = true;
    }

    public synchronized void pingChanged() {
        pingChanged = true;
    }

    public String[] getCollapsed() {
        return collapsed.toArray(new String[0]);
    }

    public void toggleCollapsed(String id) {
        if (collapsed.contains(id))
            collapsed.remove(id);
        else
            collapsed.add(id);
    }

    public long getTimestamp() {
        if (isPlayer) {
            return JolAdmin.INSTANCE.getAccess(name, player).getTime();
        } else {
            return (new Date()).getTime();
        }
    }

    public void turnChanged() {
        turnChanged = true;
    }

    public boolean isChanged() {
        return globalChanged || phaseChanged || stateChanged || turnChanged
                || chats.size() > 0;
    }

    public void addChat(String chat) {
        chats.add(chat);
    }

    public void reset(boolean reload) {
        clearAccess();
        if (reload) addChats(0);
    }

    public void addChats(int idx) {
        JolGame game = JolAdmin.INSTANCE.getGame(name);
        GameAction[] actions = game.getActions(game.getCurrentTurn());
        for (int i = idx; i < actions.length; i++)
            chats.add(actions[i].getText());
    }

    public String getPlayer() {
        return player;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public void reset() {
        reset(true);
    }

}
