package deckserver.dwr;

import cards.model.CardEntry;
import cards.model.CardSearch;
import cards.model.CardSet;
import deckserver.dwr.bean.BugDetailBean;
import deckserver.dwr.bean.CardBean;
import deckserver.dwr.bean.DeckEditBean;
import deckserver.rich.AdminBean;
import deckserver.rich.GameModel;
import deckserver.rich.GameView;
import deckserver.rich.PlayerModel;
import deckserver.util.AdminFactory;
import deckserver.util.Logger;
import deckserver.util.MailUtil;
import nbclient.model.GameAction;
import nbclient.vtesmodel.JolAdminFactory;
import webclient.state.InteractiveAdmin;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class DeckserverRemote implements DSRemote {
//	private static Logger log = Logger.getLogger(DeckserverRemote.class);

    static {
        Logger.activateLog("DeckserverRemote");
    }
    ContextProvider provider;

    public DeckserverRemote() {
        this(new DWRContextProvider());
    }

    public DeckserverRemote(ContextProvider p) {
        this.provider = p;
        abean = AdminFactory.getBean(provider.getServletContext());
    }
    private final AdminBean abean;

    public String[] getTypes() {
        return CardEntry.types;
    }

    public Map<String, Object> doPoll() {
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> createGame(String name) {
        PlayerModel player = getPlayer();
        if (player != null && player.isAdmin()) {
            abean.createGame(name, player);
        }
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> endGame(String name) {
        endGameImpl(name);
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> endGames(String[] names) {
        for (int i = 0; i < names.length; i++) {
            endGameImpl(names[i]);
        }
        return UpdateFactory.getUpdate(provider);
    }

    public String inspect(String name) {
        String player = getPlayer().getPlayer();
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        if (!admin.isSuperUser(player)) {
            return "Access denied";
        } else {
            return JolAdminFactory.INSTANCE.dump(name);
        }
    }
    
    public String dosu(String name) {
        String player = getPlayer().getPlayer();
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        if (!admin.isSuperUser(player)) {
            return "Access denied";
        } else {
            HttpServletRequest request = provider.getHttpServletRequest();
            Utils.setPlayer(request,name);
            return "Su to " + name;
        }
    }


    public Map<String, Object> invitePlayer(String game, String name) {
        String player = getPlayer().getPlayer();
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        if (!admin.isAdmin(player)) {
            // TODO throw invalid access error
        } else {
            admin.invitePlayer(game, name);
        }
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> startGame(String game) {
        String player = getPlayer().getPlayer();
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        if (admin.getOwner(game).equals(player) && admin.isOpen(game)) {
            admin.startGame(game);
            MailUtil.sendStartMsg(admin.getGame(game));
            getModel(game).firstPing();
        }
        abean.notifyAboutGame(game);
        return UpdateFactory.getUpdate(provider);
    }
    
    public String unEndGame(String game) {
        String player = getPlayer().getPlayer();
        if(JolAdminFactory.INSTANCE.isSuperUser(player)) {
            abean.unEndGame(game);
            return "Game available in beta again";
        }
        return "Permission denied";
    }

    public Map<String, Object> chat(String txt) {
        HttpServletRequest request = provider.getHttpServletRequest();
        String player = Utils.getPlayer(request);
        abean.chat(Utils.getDate() + player + ": " + txt);
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> init() {
        HttpServletRequest request = provider.getHttpServletRequest();
        String player = Utils.getPlayer(request);
        abean.remove(player);
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> navigate(String target) {
        PlayerModel player = getPlayer();
        if (target != null) {
            if (target.startsWith("g")) {
                player.enterGame(abean, target.substring(1));
            } else {
                player.setView(target);
            }
        }
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> getState(String game, boolean forceLoad) {
        if (forceLoad) {
            getView(game).reset();
        }
        return UpdateFactory.getUpdate(provider);
    }

    public String[] getHistory(String game, String turn) {
        String[] ret = new String[0];
        if (game != null && turn != null) {
            GameAction[] actions = JolAdminFactory.INSTANCE.getGame(game).getActions(turn);
            ret = new String[actions.length];
            for (int i = 0; i < actions.length; i++) {
                ret[i] = actions[i].getText();
            }
        }
        return ret;
    }

    public Map<String, Object> getCardText(String callback, String game, String id) {
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        CardSearch cards = (game == null) ? JolAdminFactory.INSTANCE.getBaseCards()
                : JolAdminFactory.INSTANCE.getCardsForGame(game);
        CardEntry card = cards.getCardById(id);
        ret.put(callback, new CardBean(card));
        return ret;
    }

    public Map<String, Object> doToggle(String game, String id) {
        GameView view = getView(game);
        view.toggleCollapsed(id);
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> submitForm(String gamename, String phase, String command, String chat,
            String ping, String endTurn, String global, String text) {
        phase = ne(phase);
        command = ne(command);
        chat = ne(chat);
        ping = ne(ping);
        endTurn = ne(endTurn);
        HttpServletRequest request = provider.getHttpServletRequest();
        String player = Utils.getPlayer(request);
        GameModel game = getModel(gamename);
        String status = game.submit(player, phase, command, chat, ping, endTurn, global, text);
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        ret.put("showStatus", status);
        return ret;
    }

    public Map<String, Object> submitDeck(String name, String deck) {
        name = ne(name);
        name = Utils.sanitizeName(name);
        PlayerModel model = getPlayer();
        if (model != null && name != null && deck != null) {
            model.submitDeck(name, deck);
        }
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        ret.put("repldeckname", name);
        return ret;
    }

    public Map<String, Object> registerDeck(String game, String name) {
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        String player = getPlayer().getPlayer();
        admin.addPlayerToGame(game, player, name);
        return UpdateFactory.getUpdate(provider);
    }

    public boolean removeDeck(String name) {
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        String player = getPlayer().getPlayer();
        admin.removeDeck(player, name);
        return true;
    }

    public Map<String, Object> getDeck(String name) {
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        String player = getPlayer().getPlayer();
        DeckEditBean deck = new DeckEditBean(player, name);
        ret.put("showDeck", deck);
        return ret;
    }

    public Map<String, Object> refreshDeck(String name, String deck, String shuffle) {
        boolean doshuffle = "true".equals(shuffle);
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        getPlayer().setTmpDeck(name, deck);
        DeckEditBean bean = new DeckEditBean(deck, doshuffle);
        ret.put("showDeck", bean);
        return ret;
    }

    public Map<String, Object> cardSearch(String type, String string) {
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        CardSearch search = JolAdminFactory.INSTANCE.getBaseCards();
        CardSet set = search.getAllCards();
        type = ne(type);
        if (type != null && !type.equals("All")) {
            set = search.searchByType(set, type);
        }
        set = search.searchByText(set, string);
        CardEntry[] arr = set.getCardArray();
        CardBean[] beans = new CardBean[arr.length];
        for (int i = 0, j = arr.length - 1; i < arr.length; i++, j--) {
            beans[j] = new CardBean(arr[i]);
        }
        ret.put("showCards", beans);
        return ret;
    }

    public String doCommand(String cmd) {
        HttpServletRequest request = provider.getHttpServletRequest();
        String player = Utils.getPlayer(request);
        JolAdminFactory admin = JolAdminFactory.INSTANCE;
        if (admin.isSuperUser(player)) {
            cmd = cmd.trim();
            if (cmd.length() == 0) {
                return admin.dump(cmd);
            }
            if (cmd.startsWith("dump")) {
                return admin.dump(cmd.substring(5).trim());
            }
            return InteractiveAdmin.executeBlock(cmd);
        }
        return "Permission denied";
    }

    public BugDetailBean getBugDetail(String index) {
        return new BugDetailBean(JolAdminFactory.INSTANCE.getBug(index));
    }

    private void endGameImpl(String name) {
        System.err.println("Attempting to close " + name);
        PlayerModel player = getPlayer();
         System.err.println(player.isSuper());
        System.err.println(abean.getGameModel(name).getOwner());
        if (player != null && (player.isSuper() || player.getPlayer().equals(abean.getGameModel(name).getOwner()))) {
            System.err.println("Closing " + name);
            abean.endGame(name);
        }
    }

    private PlayerModel getPlayer() {
        HttpServletRequest request = provider.getHttpServletRequest();
        return Utils.getPlayerModel(request, abean);
    }

    private GameView getView(String name) {
        HttpServletRequest request = provider.getHttpServletRequest();
        String player = Utils.getPlayer(request);
        GameModel gmodel = getModel(name);
        return gmodel.getView(player);
    }

    private GameModel getModel(String name) {
        return abean.getGameModel(name);
    }

    private static final String ne(String arg) {
        if ("".equals(arg)) {
            return null;
        }
        return arg;
    }
}
