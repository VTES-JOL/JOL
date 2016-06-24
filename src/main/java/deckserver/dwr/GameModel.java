package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.SummaryBean;
import deckserver.util.AdminFactory;
import deckserver.JolAdminFactory;
import deckserver.JolGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ltd.getahead.dwr.WebContextFactory;
import deckserver.client.DoCommand;

import javax.servlet.ServletContext;
import java.text.DateFormat;
import java.util.*;

public class GameModel implements Comparable {

    public static final long TIMEOUT_INTERVAL = 600000;
    private static Logger logger = LoggerFactory.getLogger(GameModel.class);

    private String globalOwner = null;
    private String name;
    private long timestamp;
    private Map<String, GameView> views = new HashMap<String, GameView>();
    private SummaryBean sumbean;

    public GameModel(String name) {
        this.name = name;
        if (isActive()) JolAdminFactory.INSTANCE.getGame(name);  // make sure its loaded
        timestamp = JolAdminFactory.INSTANCE.getGameTimeStamp(name).getTime();
        regen();
    }

    public void unsetGlobalOwner() {
        globalOwner = null;
    }

    public synchronized boolean isGlobalEditable(String player) {
        return globalOwner == null || globalOwner.equals(player);
    }

    public String getGlobalOwner() {
        return globalOwner;
    }

    public synchronized void setGlobalOwner(String globalOwner) {
        if (globalOwner == null)
            this.globalOwner = globalOwner;
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
        if (!getPlayers().contains(player) && !admin.getOwner(name).equals(player) && !admin.isSuperUser(player)) {
            return "Not authorized";
        }
        JolGame game = admin.getGame(name);
        StringBuffer status = new StringBuffer();
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
                game.newTurn();
                resetChats();
                idx = 0; // reset the current action index for the new turn.
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
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        JolGame game = admin.getGame(name);
        String email = admin.getEmail(game.getActivePlayer());
        game.setPingTag(game.getActivePlayer());
    }

    private void addChats(int idx) {
        for (Iterator<GameView> i = views.values().iterator(); i.hasNext(); ) {
            i.next().addChats(idx);
        }
    }

    private void resetChats() {
        for (Iterator<GameView> i = views.values().iterator(); i.hasNext(); ) {
            i.next().reset(false);
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
        for (Iterator<String> i = (new ArrayList<String>(views.keySet())).iterator(); i.hasNext(); ) {
            String key = i.next();
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
