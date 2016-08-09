package deckserver.dwr;

import deckserver.client.DoCommand;
import deckserver.client.JolAdminFactory;
import deckserver.client.JolGame;
import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.SummaryBean;
import deckserver.util.AdminFactory;
import deckserver.util.MailUtil;
import org.directwebremoting.WebContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.text.DateFormat;
import java.util.*;

public class GameModel implements Comparable {

    public static final long TIMEOUT_INTERVAL = 600000;
    private static Logger logger = LoggerFactory.getLogger(GameModel.class);

    private String globalOwner = null;
    private String name;
    private long timestamp;
    private Map<String, GameView> views = new HashMap<>();
    private SummaryBean sumbean;

    public GameModel(String name) {
        this.name = name;
        if (isActive()) JolAdminFactory.INSTANCE.getGame(name);  // make sure its loaded
        timestamp = JolAdminFactory.INSTANCE.getGameTimeStamp(name).getTime();
        regen();
    }

    public boolean isOpen() {
        return JolAdminFactory.INSTANCE.isOpen(name);
    }

    public boolean isActive() {
        return JolAdminFactory.INSTANCE.isActive(name);
    }

    public boolean isFinished() {
        return JolAdminFactory.INSTANCE.isFinished(name);
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return JolAdminFactory.INSTANCE.getOwner(name);
    }

    public synchronized String submit(String player, String phase, String command, String chat,
                                      String ping, String endTurn, String global, String text) {
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
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
                String email = admin.getEmail(ping);
                game.setPingTag(ping);
                pingChanged = true;
                status.append(MailUtil.ping(game.getName(), email));
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
                            String[] cmdTokens = cmd.split("[\\s\n\r\f\t]");
                            status.append(commander.doCommand(player, cmdTokens));
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
            }
            if ((game.getActivePlayer().equals(player)
                    || admin.getOwner(game.getName()).equals(player))
                    && "Yes".equalsIgnoreCase(endTurn)) {
                try {
                    MailUtil.sendTurn(game);
                } catch (Error e) {
                    status.append("Turn email failed.");
                }
                game.newTurn();
                resetChats();
                idx = 0; // reset the current action index for the new turn.
                String email = admin.getEmail(game.getActivePlayer());
                try {
                    MailUtil.ping(game.getName(), email);
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

    void firstPing() {
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        JolGame game = admin.getGame(name);
        String email = admin.getEmail(game.getActivePlayer());
        game.setPingTag(game.getActivePlayer());
        MailUtil.ping(game.getName(), email);
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

/*	private boolean checkViewTime(String player, GameView view, long timestamp) {
        if(timestamp - view.getTimestamp() > TIMEOUT_INTERVAL) {
			logger.logger("timestamp " + timestamp + " view " + view.getTimestamp());
			//views.remove(player);
			logger.logger("Removing " + player + " from " + name);
			return true;
		}
		return false;
	} */

    private void doReload(boolean stateChanged, boolean phaseChanged, boolean pingChanged, boolean globalChanged, boolean turnChanged) {
        timestamp = JolAdminFactory.INSTANCE.getGameTimeStamp(name).getTime();
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
                AdminBean abean = AdminFactory.getBean(ctx);
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

    public String getDate() {
        Date d = new Date(timestamp);
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(d);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public GameView[] getViews() {
        return views.values().toArray(new GameView[0]);
    }

    public void resetView(String player) {
        views.remove(player);
    }

    public Collection getPlayers() {
        return Arrays.asList(JolAdminFactory.INSTANCE.getPlayers(name));
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
