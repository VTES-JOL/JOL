package net.deckserver.dwr.model;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.enums.Phase;
import net.deckserver.services.ChatService;
import net.deckserver.services.RegistrationService;
import net.deckserver.storage.json.game.ChatData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.message.ObjectArrayMessage;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameModel implements Comparable<GameModel> {

    private static final Logger METRICS = LogManager.getLogger("net.deckserver.metrics");
    private static final Logger COMMANDS = LogManager.getLogger("net.deckserver.commands");

    @Getter
    private final String name;
    private final JolGame game;
    private final Map<String, GameView> views = new HashMap<>();

    public GameModel(JolGame game) {
        this.name = game.getName();
        this.game = game;
        ChatService.subscribe(game.id(), this);
    }

    public synchronized void endTurn(String player) {
        JolGame game = JolAdmin.getGame(name);
        if (game.getActivePlayer().equals(player)) {
            game.newTurn();
            reloadNotes();
            JolAdmin.saveGameState(game);
            doReload(true, true, true);
        }
    }

    public synchronized String submit(String player, String phase, String command, String chat, String ping) {
        // Only players and judges can issue commands.  A judge can't be a player
        boolean isJudge = JolAdmin.isJudge(player) && !getPlayers().contains(player);
        if (!getPlayers().contains(player) && !isJudge) {
            return "Not authorized";
        }
        JolGame game = JolAdmin.getGame(name);
        StringBuilder status = new StringBuilder();
        if (player != null) {
            boolean stateChanged = false;
            boolean phaseChanged = false;
            boolean chatChanged = false;
            boolean turnChanged = false;
            if (ping != null) {
                boolean pingSuccessful = JolAdmin.pingPlayer(ping, name);
                if (!pingSuccessful) {
                    status.append("Player is already pinged");
                }
            }
            if (phase != null &&
                    game.getActivePlayer().equals(player)
                    && !game.getPhase().equals(Phase.of(phase))) {
                game.setPhase(Phase.of(phase));
                phaseChanged = true;
            }
            if (command != null || chat != null) {
                DoCommand commander = new DoCommand(game, this);
                boolean didCommand = false;
                boolean didChat = false;
                if (command != null) {
                    didCommand = true;
                    String[] commands = command.split(";");
                    ThreadContext.put("DYNAMIC_LOG", name);
                    for (String cmd : commands) {
                        try {
                            commander.doCommand(player, cmd);
                            COMMANDS.info("[{}] {}", player, cmd);
                        } catch (CommandException e) {
                            COMMANDS.error("[{}] {}", player, cmd);
                            status.append(e.getMessage());
                        }
                    }
                    stateChanged = true;
                }
                if (chat != null) {
                    didChat = true;
                    commander.doMessage(player, chat, isJudge);
                    chatChanged = true;
                }
                OffsetDateTime timestamp = OffsetDateTime.now();
                METRICS.info(new ObjectArrayMessage(timestamp.getYear(), timestamp.getMonthValue(), timestamp.getDayOfMonth(), timestamp.getHour(), player, game.getName(), didCommand, didChat));
                JolAdmin.clearPing(player, name);
            }
            if (stateChanged || phaseChanged || chatChanged) {
                JolAdmin.saveGameState(game);
            }
            doReload(stateChanged, phaseChanged, turnChanged);
        }
        return status.toString();
    }

    public GameView getView(String player) {
        if (!views.containsKey(player)) {
            synchronized (this) {
                views.put(player, new GameView(game, name, player));
            }
        }
        return views.get(player);
    }

    public void resetView(String player) {
        views.remove(player);
    }

    public synchronized Set<String> getPlayers() {
        return RegistrationService.getPlayers(name);
    }

    public int compareTo(GameModel arg0) {
        return -name.compareToIgnoreCase(arg0.getName());
    }

    public synchronized void updateGlobalNotes(String notes) {
        JolGame game = JolAdmin.getGame(name);
        if (!notes.equals(game.getGlobalText())) {
            game.setGlobalText(notes);
            reloadNotes();
            JolAdmin.saveGameState(game);
        }
    }

    public synchronized void updatePrivateNotes(String player, String notes) {
        JolGame game = JolAdmin.getGame(name);
        if (!notes.equals(game.getPrivateNotes(player))) {
            game.setPrivateNotes(player, notes);
            views.get(player).privateNotesChanged();
            JolAdmin.saveGameState(game, true);
        }
    }

    public void addChat(ChatData chat) {
        for (GameView gameView : views.values()) {
            gameView.addChat(chat);
        }
    }

    public void clearChats() {
        for (GameView gameView : views.values()) {
            gameView.clearAccess();
        }
    }

    private void doReload(boolean stateChanged, boolean phaseChanged, boolean turnChanged) {
        for (String key : (new ArrayList<>(views.keySet()))) {
            GameView view = views.get(key);
            if (stateChanged) view.stateChanged();
            if (phaseChanged) view.phaseChanged();
            if (turnChanged) view.turnChanged();
        }
    }

    private void reloadNotes() {
        for (String key : (new ArrayList<>(views.keySet()))) {
            GameView view = views.get(key);
            view.globalChanged();
            view.privateNotesChanged();
        }
    }

}
