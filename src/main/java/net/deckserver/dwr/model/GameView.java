package net.deckserver.dwr.model;

import net.deckserver.dwr.bean.GameBean;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.storage.json.cards.RegionType;
import org.directwebremoting.WebContextFactory;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class GameView {

    private static final Logger logger = getLogger(GameView.class);
    private final String gameName;
    private final String playerName;
    private final Collection<String> chats = new ArrayList<>();
    private final Collection<String> collapsed = new HashSet<>();
    private boolean stateChanged = true;
    private boolean phaseChanged = true;
    private boolean globalNotesChanged = true;
    private boolean privateNotesChanged = true;
    private boolean turnChanged = true;
    private boolean resetChat = true;
    private boolean isPlayer = false;
    private boolean isAdmin = false;
    private boolean isJudge = false;

    public GameView(String gameName, String playerName) {
        this.gameName = gameName;
        this.playerName = playerName;
        JolAdmin admin = JolAdmin.INSTANCE;
        JolGame game = admin.getGame(gameName);
        GameAction[] actions = game.getActions(game.getCurrentTurn());
        for (GameAction action : actions) {
            addChat(action.getText());
        }
        List<String> players = game.getPlayers();
        if (players.contains(this.playerName)) {
            isPlayer = true;
        }
        for (int i = 0; i < players.size(); ) {
            boolean ousted = game.getPool(players.get(i)) < 1;
            i++;
            collapsed.add(i + "-" + RegionType.ASH_HEAP);
            collapsed.add(i + "-" + RegionType.REMOVED_FROM_GAME);
            collapsed.add(i + "-" + RegionType.LIBRARY);
            collapsed.add(i + "-" + RegionType.HAND);
            collapsed.add(i + "-" + RegionType.CRYPT);
            if (ousted) {
                collapsed.add(i + "-" + RegionType.TORPOR);
                collapsed.add(i + "-" + RegionType.RESEARCH);
                collapsed.add(i + "-" + RegionType.READY);
                collapsed.add(i + "-" + RegionType.UNCONTROLLED);
            }
        }
        if (!isPlayer && (admin.getOwner(gameName).equals(playerName))) {
            isAdmin = true;
        }
        if (!isPlayer && (admin.isJudge(playerName))) {
            isJudge = true;
        }
    }

    public boolean isCollapsed(String region) {
        return collapsed.contains(region);
    }

    public synchronized GameBean create() {
        JolAdmin admin = JolAdmin.INSTANCE;
        HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
        JolGame game = admin.getGame(gameName);

        if (isPlayer) {
            admin.recordPlayerAccess(playerName, gameName);
        }

        int refresh = admin.getRefreshInterval(gameName);

        List<String> ping;
        List<String> pinged;
        String hand = null;
        String globalNotes = null;
        String privateNotes = null;
        String label;
        List<String> turn = new ArrayList<>();
        List<String> turns = new ArrayList<>();
        String state = null;
        List<String> phases = new ArrayList<>();
        String currentPlayer = game.getActivePlayer();

        ping = game.getPingList();
        pinged = admin.getPings(gameName);

        if (isPlayer && stateChanged) {
            try {
                request.setAttribute("game", game);
                request.setAttribute("player", playerName);
                request.setAttribute("viewer", playerName);
                hand = WebContextFactory.get().forwardToString("/WEB-INF/jsps/game/hand.jsp");
            } catch (Exception e) {
                logger.error("Error retrieving hand", e);
                hand = "Error retrieving hand.";
            }
        }

        if (globalNotesChanged) {
            globalNotes = game.getGlobalText();
        }

        if (privateNotesChanged) {
            privateNotes = game.getPrivateNotes(playerName);
        }

        label = game.getCurrentTurn() + " - " + game.getPhase();
        String phase = game.getPhase();

        if (!chats.isEmpty()) {
            turn.addAll(chats);
            chats.clear();
        }

        if (turnChanged) {
            resetChat = true;
            List<String> gameTurns = game.getTurns();
            for (int j = gameTurns.size(); j > 0; )
                turns.add(gameTurns.get(--j));
        }

        if (stateChanged) {
            try {
                request.setAttribute("game", game);
                request.setAttribute("viewer", playerName);
                state = WebContextFactory.get().forwardToString("/WEB-INF/jsps/game/state.jsp");
            } catch (Exception e) {
                logger.error("Error retrieving state:", e);
                hand = "Error retrieving state.";
            }
        }

        if (phaseChanged) {
            boolean show = false;
            for (int i = 0; i < JolGame.TURN_PHASES.length; i++) {
                if (phase.equals(JolGame.TURN_PHASES[i]))
                    show = true;
                if (show)
                    phases.add(JolGame.TURN_PHASES[i]);
            }
        }

        boolean chatReset = resetChat;
        boolean tc = turnChanged;
        clearAccess();
        String stamp = JolAdmin.getDate();
        int logLength = game.getActions().length;
        return new GameBean(isPlayer, isAdmin, isJudge, refresh, hand, globalNotes, privateNotes, label, phase,
                chatReset, tc, turn, turns, state, phases, ping, pinged, stamp, gameName, logLength, currentPlayer);
    }

    public synchronized void clearAccess() {
        globalNotesChanged = phaseChanged = turnChanged = stateChanged = privateNotesChanged = resetChat = false;
    }

    public synchronized void globalChanged() {
        globalNotesChanged = true;
    }

    public synchronized void privateNotesChanged() {
        privateNotesChanged = true;
    }

    public synchronized void phaseChanged() {
        phaseChanged = true;
    }

    public synchronized void stateChanged() {
        stateChanged = true;
    }

    public void toggleCollapsed(String id) {
        if (collapsed.contains(id))
            collapsed.remove(id);
        else
            collapsed.add(id);
    }

    public void turnChanged() {
        turnChanged = true;
    }

    public void addChat(String chat) {
        chats.add(chat);
    }

    public void reset(boolean reload) {
        clearAccess();
        if (reload) addChats(0);
    }

    public void addChats(int idx) {
        JolGame game = JolAdmin.INSTANCE.getGame(gameName);
        GameAction[] actions = game.getActions(game.getCurrentTurn());
        for (int i = idx; i < actions.length; i++) {
            chats.add(actions[i].getText());
        }
    }

    public void reset() {
        reset(true);
        //Force the client to refresh all game data
        resetChat = true;
        globalNotesChanged = phaseChanged = stateChanged = turnChanged = privateNotesChanged = true;
    }

}
