package net.deckserver.dwr.model;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.bean.SummaryBean;
import org.directwebremoting.WebContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.time.OffsetDateTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class GameModel implements Comparable {

    private static Logger logger = LoggerFactory.getLogger(GameModel.class);

    private String name;
    private Map<String, GameView> views = new HashMap<>();
    private SummaryBean sumbean;

    public GameModel(String name) {
        this.name = name;
        if (isActive()) JolAdmin.getInstance().getGame(name);  // make sure its loaded
        regen();
    }

    public boolean isOpen() {
        return JolAdmin.getInstance().isOpen(name);
    }

    public boolean isActive() {
        return JolAdmin.getInstance().isActive(name);
    }

    public boolean isFinished() {
        return JolAdmin.getInstance().isFinished(name);
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
        admin.saveGame(game);
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
            boolean turnChanged = false;
            int idx = game.getActions(game.getCurrentTurn()).length;
            if (ping != null) {
                admin.pingPlayer(ping, name);
                pingChanged = true;
                status.append("Ping sent to " + ping);
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
                game.setGlobalText(global);
                globalChanged = true;
            }
            if (text != null) {
                if (text.length() > 800)
                    text = text.substring(0, 799);
                game.setPlayerText(player, text);
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
                    status.append(commander.doMessage(player,
                            chat));
                    chat = null;
                    chatChanged = true;
                }
                admin.clearPing(player, name);
            }
            if ((game.getActivePlayer().equals(player) || admin.getOwner(game.getName()).equals(player)) && "Yes".equalsIgnoreCase(endTurn)) {
                game.newTurn();
                resetChats();
                idx = 0; // reset the current action index for the new turn.
                String email = admin.getEmail(game.getActivePlayer());
                try {
                    game.setPingTag(game.getActivePlayer());
                } catch (Error e) {
                    status.append("Turn ping failed.");
                }
                turnChanged = stateChanged = phaseChanged = pingChanged = true;
                regen();
            }
            addChats(idx);
            if (stateChanged || phaseChanged || chatChanged || pingChanged || globalChanged) {
                admin.saveGame(game);
            }
            doReload(stateChanged, phaseChanged, pingChanged, globalChanged, turnChanged);
        }
        return status.toString();
    }

    public void firstPing() {
        JolAdmin admin = JolAdmin.getInstance();
        JolGame game = admin.getGame(name);
        game.setPingTag(game.getActivePlayer());
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

    private void doReload(boolean stateChanged, boolean phaseChanged, boolean pingChanged, boolean globalChanged, boolean turnChanged) {
        for (String key : (new ArrayList<>(views.keySet()))) {
            GameView view = views.get(key);
            //		if(checkViewTime(key, view,timestamp)) continue;
            if (stateChanged) view.stateChanged();
            if (phaseChanged) view.phaseChanged();
            if (pingChanged) view.pingChanged();
            if (globalChanged) view.globalChanged();
            if (turnChanged) view.turnChanged();
            if (stateChanged || phaseChanged || globalChanged || turnChanged) {
                ServletContext ctx = WebContextFactory.get().getServletContext();
                AdminBean abean = AdminBean.INSTANCE;
                abean.notifyAboutGame(name);
            }
        }
    }

    private String[] tokenize(String arg) {
        StringTokenizer tok = new StringTokenizer(arg);
        String[] ret = new String[tok.countTokens() + 3];
        for (int i = 3; i < ret.length; i++)
            ret[i] = tok.nextToken();
        return ret;
    }

    public GameView getView(String player) {
        if (!views.containsKey(player)) {
            synchronized (this) {
                views.put(player, new GameView(name, player));
            }
        }
        return views.get(player);
    }

    public GameView hasView(String player) {
        return views.get(player);
    }

    public GameView[] getViews() {
        return views.values().toArray(new GameView[0]);
    }

    public void resetView(String player) {
        views.remove(player);
    }

    public Collection getPlayers() {
        return Arrays.asList(JolAdmin.getInstance().getPlayers(name));
    }

    public int compareTo(Object arg0) {
        return -name.compareToIgnoreCase(((GameModel) arg0).getName());
    }

    void regen() {
        sumbean = new SummaryBean(this);
    }

    public SummaryBean getSummaryBean() {
        return sumbean;
    }
}
