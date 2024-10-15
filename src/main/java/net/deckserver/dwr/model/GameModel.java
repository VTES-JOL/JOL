package net.deckserver.dwr.model;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectArrayMessage;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameModel implements Comparable<GameModel> {

    private static final Logger logger = LogManager.getLogger();
    private static final Logger metrics = LogManager.getLogger("net.deckserver.metrics");

    @Getter
    private final String name;
    private final Map<String, GameView> views = new HashMap<>();

    public GameModel(String name) {
        this.name = name;
        if (isActive()) JolAdmin.getInstance().getGame(name);  // make sure its loaded
    }

    public boolean isOpen() {
        return JolAdmin.getInstance().isStarting(name);
    }

    public boolean isActive() {
        return JolAdmin.getInstance().isActive(name);
    }

    public String getOwner() {
        return JolAdmin.getInstance().getOwner(name);
    }

    public synchronized String chat(String player, String chat) {
        JolAdmin admin = JolAdmin.getInstance();
        JolGame game = admin.getGame(name);
        if (getPlayers().contains(player) || !admin.isJudge(player)) {
            return "Not authorized";
        }
        DoCommand commander = new DoCommand(game);
        int idx = game.getActions(game.getCurrentTurn()).length;
        String status = commander.doMessage(player, chat);
        addChats(idx);
        admin.saveGameState(game);
        return status;
    }

    public synchronized String submit(String player, String phase, String command, String chat,
                                      String ping, String endTurn, String global, String text) {
        JolAdmin admin = JolAdmin.getInstance();
        if (!getPlayers().contains(player) && !admin.getOwner(name).equals(player)) {
            return "Not authorized";
        }
        JolGame game = admin.getGame(name);
        StringBuilder status = new StringBuilder();
        if (player != null) {
            boolean stateChanged = false;
            boolean phaseChanged = false;
            boolean chatChanged = false;
            boolean globalChanged = false;
            boolean privateNotesChanged = false;
            boolean turnChanged = false;
            int idx = game.getActions(game.getCurrentTurn()).length;
            if (ping != null) {
                if (admin.pingPlayer(ping, name)) {
                    status.append("Ping sent to ").append(ping);
                } else status.append("Player is already pinged");
            }
            if (phase != null &&
                    game.getActivePlayer().equals(player)
                    && !game.getPhase().equals(phase)) {
                game.setPhase(phase);
                phaseChanged = true;
            }
            if (global != null) {
                if (global.length() > 800)
                    global = global.substring(0, 799);
                if (!global.equals(game.getGlobalText())) {
                    game.setGlobalText(global);
                    globalChanged = true;
                }
            }
            if (text != null) {
                if (text.length() > 800)
                    text = text.substring(0, 799);
                if (!text.equals(game.getPlayerText(player))) {
                    game.setPlayerText(player, text);
                    privateNotesChanged = true;
                }
            }
            if (command != null || chat != null) {
                DoCommand commander = new DoCommand(game);
                boolean didCommand = false;
                boolean didChat = false;
                if (command != null) {
                    didCommand = true;
                    String[] commands = command.split(";");
                    for (String cmd : commands) {
                        try {
                            String[] cmdTokens = cmd.trim().split("[\\s\n\r\f\t]");
                            status.append(commander.doCommand(player, cmdTokens)).append("<br/>");
                        } catch (Exception e) {
                            logger.error("Error with command {} for player {} and game {}", cmd, player, name);
                            status.append(e.getMessage());
                        }
                    }
                    stateChanged = true;
                }
                if (chat != null) {
                    didChat = true;
                    status.append(commander.doMessage(player, chat));
                    chatChanged = true;
                }
                OffsetDateTime timestamp = OffsetDateTime.now();
                metrics.info(new ObjectArrayMessage(timestamp.getYear(), timestamp.getMonthValue(), timestamp.getDayOfMonth(), timestamp.getHour(), player, game.getName(), didCommand, didChat));
                admin.clearPing(player, name);
            }
            if (game.getActivePlayer().equals(player) && "Yes".equalsIgnoreCase(endTurn)) {
                game.newTurn();
                resetChats();
                idx = 0; // reset the current action index for the new turn.
                turnChanged = stateChanged = phaseChanged = true;
            }
            addChats(idx);
            if (stateChanged || phaseChanged || chatChanged || globalChanged) {
                admin.saveGameState(game);
            }
            doReload(stateChanged, phaseChanged, globalChanged, turnChanged, privateNotesChanged);
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
        return JolAdmin.getInstance().getPlayers(name);
    }

    public int compareTo(GameModel arg0) {
        return -name.compareToIgnoreCase(arg0.getName());
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

    private void doReload(boolean stateChanged, boolean phaseChanged, boolean globalChanged, boolean turnChanged, boolean privateNotesChanged) {
        for (String key : (new ArrayList<>(views.keySet()))) {
            GameView view = views.get(key);
            if (stateChanged) view.stateChanged();
            if (phaseChanged) view.phaseChanged();
            if (globalChanged) view.globalChanged();
            if (privateNotesChanged) view.privateNotesChanged();
            if (turnChanged) view.turnChanged();
        }
    }

}
