package net.deckserver.dwr.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GameModel implements Comparable {

    private static Logger logger = LoggerFactory.getLogger(GameModel.class);

    private String name;
    private Map<String, GameView> views = new HashMap<>();

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

    public String getName() {
        return name;
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
            boolean pingChanged = false;
            boolean phaseChanged = false;
            boolean chatChanged = false;
            boolean globalChanged = false;
            boolean privateNotesChanged = false;
            boolean turnChanged = false;
            int idx = game.getActions(game.getCurrentTurn()).length;
            if (ping != null) {
                if (admin.pingPlayer(ping, name)) {
                    pingChanged = true;
                    status.append("Ping sent to ").append(ping);
                }
                else status.append("Player is already pinged");
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
                if (command != null) {
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
                    status.append(commander.doMessage(player, chat));
                    chat = null;
                    chatChanged = true;
                }
                admin.clearPing(player, name);
            }
            if (game.getActivePlayer().equals(player) && "Yes".equalsIgnoreCase(endTurn)) {
                game.newTurn();
                resetChats();
                idx = 0; // reset the current action index for the new turn.
                String email = admin.getEmail(game.getActivePlayer());
                turnChanged = stateChanged = phaseChanged = pingChanged = true;
            }
            addChats(idx);
            if (stateChanged || phaseChanged || chatChanged || globalChanged) {
                admin.saveGameState(game);
            }
            doReload(stateChanged, phaseChanged, pingChanged, globalChanged, turnChanged, chatChanged, privateNotesChanged);
        }
        return status.toString();
    }

    public void firstPing() {
        JolAdmin admin = JolAdmin.getInstance();
        JolGame game = admin.getGame(name);
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

    private void doReload(boolean stateChanged, boolean phaseChanged, boolean pingChanged, boolean globalChanged, boolean turnChanged, boolean chatChanged, boolean privateNotesChanged) {
        for (String key : (new ArrayList<>(views.keySet()))) {
            GameView view = views.get(key);
            if (stateChanged) view.stateChanged();
            if (phaseChanged) view.phaseChanged();
            if (globalChanged) view.globalChanged();
            if (privateNotesChanged) view.privateNotesChanged();
            if (turnChanged) view.turnChanged();
        }
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

    public int compareTo(Object arg0) {
        return -name.compareToIgnoreCase(((GameModel) arg0).getName());
    }

}
