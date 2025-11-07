package net.deckserver.dwr.model;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.GameBean;
import net.deckserver.game.enums.Phase;
import net.deckserver.game.enums.RegionType;
import net.deckserver.services.ChatService;
import net.deckserver.storage.json.game.ChatData;
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
    private final String id;
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

    public GameView(JolGame game, String gameName, String playerName) {
        this.gameName = gameName;
        this.playerName = playerName;
        this.id = game.id();
        ChatService.getChats(id).forEach(this::addChat);
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
        if (!isPlayer && (JolAdmin.getOwner(gameName).equals(playerName))) {
            isAdmin = true;
        }
        if (!isPlayer && (JolAdmin.isJudge(playerName))) {
            isJudge = true;
        }
    }

    public boolean isCollapsed(String region) {
        return collapsed.contains(region);
    }

    public  GameBean create() {
        HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
        JolGame game = JolAdmin.getGame(gameName);

        if (isPlayer) {
            JolAdmin.recordPlayerAccess(playerName, gameName);
        }

        int refresh = JolAdmin.getRefreshInterval(gameName);

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

        ping = JolAdmin.getPingList(gameName);
        pinged = JolAdmin.getPings(gameName);

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

        label = game.getTurnLabel() + " - " + game.getPhase();
        Phase phase = game.getPhase();

        if (!chats.isEmpty()) {
            turn.addAll(chats);
            chats.clear();
        }

        if (turnChanged) {
            resetChat = true;
            turns = ChatService.getTurns(id);
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
            for (Phase p : Phase.values()) {
                if (phase.equals(p))
                    show = true;
                if (show)
                    phases.add(p.getDescription());
            }
        }

        boolean chatReset = resetChat;
        boolean tc = turnChanged;
        clearAccess();
        String stamp = JolAdmin.getDate();
        int logLength = ChatService.getTurn(id, game.getTurnLabel()).size();
        return new GameBean(isPlayer, isAdmin, isJudge, refresh, hand, globalNotes, privateNotes, label, phase.getDescription(),
                chatReset, tc, turn, turns, state, phases, ping, pinged, stamp, gameName, logLength, currentPlayer);
    }

    public  void clearAccess() {
        globalNotesChanged = phaseChanged = turnChanged = stateChanged = privateNotesChanged = resetChat = false;
    }

    public  void globalChanged() {
        globalNotesChanged = true;
    }

    public  void privateNotesChanged() {
        privateNotesChanged = true;
    }

    public  void phaseChanged() {
        phaseChanged = true;
    }

    public  void stateChanged() {
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

    public void reset() {
        clearAccess();
        //Force the client to refresh all game data
        resetChat = true;
        globalNotesChanged = phaseChanged = stateChanged = turnChanged = privateNotesChanged = true;
    }

    public void addChat(ChatData chat) {
        String formattedMessage = String.format("%s||%s||%s", chat.getTimestamp(), chat.getSource(), chat.getMessage());
        chats.add(formattedMessage);
    }
}
