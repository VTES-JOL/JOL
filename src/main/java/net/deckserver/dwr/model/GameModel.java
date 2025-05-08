package net.deckserver.dwr.model;

import lombok.Getter;
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
    private final Map<String, GameView> views = new HashMap<>();

    public GameModel(String name) {
        this.name = name;
    }

    public synchronized String chat(String player, String chat) {
        JolAdmin admin = JolAdmin.INSTANCE;
        JolGame game = admin.getGame(name);
        boolean isJudge = admin.isJudge(player) && !getPlayers().contains(player);
        if (!getPlayers().contains(player) && !isJudge) {
            return "Not authorized";
        }
        DoCommand commander = new DoCommand(game, this);
        int idx = game.getActions(game.getCurrentTurn()).length;
        String status = commander.doMessage(player, chat, isJudge);
        addChats(idx);
        admin.saveGameState(game);
        return status;
    }

    public synchronized String submit(String player, String phase, String command, String chat,
                                      String ping, String endTurn) {
        JolAdmin admin = JolAdmin.INSTANCE;
        // Only players and judges can issue commands.  A judge can't be a player
        boolean isJudge = admin.isJudge(player) && !getPlayers().contains(player);
        if (!getPlayers().contains(player) && !isJudge) {
            return "Not authorized";
        }
        JolGame game = admin.getGame(name);
        StringBuilder status = new StringBuilder();
        if (player != null) {
            boolean stateChanged = false;
            boolean phaseChanged = false;
            boolean chatChanged = false;
            boolean turnChanged = false;
            int idx = game.getActions(game.getCurrentTurn()).length;
            if (ping != null) {
                if (admin.pingPlayer(ping, name)) {
                    status.append("Ping sent to ").append(ping);
                    stateChanged = true;
                } else status.append("Player is already pinged");
            }
            if (phase != null &&
                    game.getActivePlayer().equals(player)
                    && !game.getPhase().equals(phase)) {
                game.setPhase(phase);
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
                    status.append(commander.doMessage(player, chat, isJudge));
                    chatChanged = true;
                }
                OffsetDateTime timestamp = OffsetDateTime.now();
                METRICS.info(new ObjectArrayMessage(timestamp.getYear(), timestamp.getMonthValue(), timestamp.getDayOfMonth(), timestamp.getHour(), player, game.getName(), didCommand, didChat));
                admin.clearPing(player, name);
            }
            if (game.getActivePlayer().equals(player) && "Yes".equalsIgnoreCase(endTurn)) {
                game.newTurn();
                resetChats();
                reloadNotes();
                idx = 0; // reset the current action index for the new turn.
                turnChanged = stateChanged = phaseChanged = true;
            }
            addChats(idx);
            if (stateChanged || phaseChanged || chatChanged) {
                admin.saveGameState(game);
            }
            doReload(stateChanged, phaseChanged, turnChanged);
        }
        return status.toString();
    }

    public GameView getView(String player) {
        if (!views.containsKey(player)) {
            synchronized (this) {
                views.put(player, new GameView(name, player));
            }
        }
        return views.get(player);
    }

    public void resetView(String player) {
        views.remove(player);
    }

    public Set<String> getPlayers() {
        return JolAdmin.INSTANCE.getPlayers(name);
    }

    public int compareTo(GameModel arg0) {
        return -name.compareToIgnoreCase(arg0.getName());
    }

    public void updateGlobalNotes(String notes) {
        JolAdmin admin = JolAdmin.INSTANCE;
        JolGame game = admin.getGame(name);
        if (!game.getGlobalText().equals(notes)) {
            game.setGlobalText(notes);
            reloadNotes();
            admin.saveGameState(game);
        }
    }

    public void updatePrivateNotes(String player, String notes) {
        JolAdmin admin = JolAdmin.INSTANCE;
        JolGame game = admin.getGame(name);
        if (!notes.equals(game.getPrivateNotes(player))) {
            game.setPrivateNotes(player, notes);
            views.get(player).privateNotesChanged();
            admin.saveGameState(game, true);
        }
    }

    private void addChats(int idx) {
        for (GameView gameView : views.values()) {
            gameView.addChats(idx);
        }
    }

    private void resetChats() {
        for (GameView gameView : views.values()) {
            gameView.reset(false);
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
        }
    }

}
