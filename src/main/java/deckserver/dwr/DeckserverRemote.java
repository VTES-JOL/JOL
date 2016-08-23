package deckserver.dwr;

import deckserver.client.JolAdmin;
import deckserver.dwr.bean.AdminBean;
import deckserver.dwr.bean.CardBean;
import deckserver.dwr.bean.DeckEditBean;
import deckserver.game.cards.CardEntry;
import deckserver.game.cards.CardSet;
import deckserver.game.cards.CardType;
import deckserver.game.cards.OldCardSearch;
import deckserver.game.turn.GameAction;
import deckserver.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DeckserverRemote implements DSRemote {
    private static Logger logger = LoggerFactory.getLogger(DeckserverRemote.class);

    private final AdminBean abean;
    private ContextProvider provider;

    public DeckserverRemote() {
        provider = new DWRContextProvider();
        abean = AdminBean.INSTANCE;
    }

    private static String ne(String arg) {
        if ("".equals(arg)) {
            return null;
        }
        return arg;
    }

    public String[] getTypes() {
        Set<CardType> cardTypes = EnumSet.allOf(CardType.class);
        List<String> labels = cardTypes.stream().map(CardType::getLabel).collect(Collectors.toList());
        return labels.toArray(new String[labels.size()]);
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

    public Map<String, Object> invitePlayer(String game, String name) {
        String player = getPlayer().getPlayer();
        JolAdmin admin = JolAdmin.INSTANCE;
        if (admin.isAdmin(player)) {
            admin.invitePlayer(game, name);
        }
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> startGame(String game) {
        String player = getPlayer().getPlayer();
        JolAdmin admin = JolAdmin.INSTANCE;
        if (admin.getOwner(game).equals(player) && admin.isOpen(game)) {
            admin.startGame(game);
            MailUtil.sendStartMsg(admin.getGame(game));
            getModel(game).firstPing();
        }
        abean.notifyAboutGame(game);
        return UpdateFactory.getUpdate(provider);
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
            GameAction[] actions = JolAdmin.INSTANCE.getGame(game).getActions(turn);
            ret = new String[actions.length];
            for (int i = 0; i < actions.length; i++) {
                ret[i] = actions[i].getText();
            }
        }
        return ret;
    }

    public Map<String, Object> getCardText(String callback, String id) {
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        OldCardSearch cards = JolAdmin.INSTANCE.getAllCards();
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
        if (model != null && deck != null) {
            model.submitDeck(name, deck);
        }
        Map<String, Object> ret = UpdateFactory.getUpdate(provider);
        ret.put("callbackUpdateDeck", name);
        return ret;
    }

    public Map<String, Object> registerDeck(String game, String name) {
        JolAdmin admin = JolAdmin.INSTANCE;
        String player = getPlayer().getPlayer();
        admin.addPlayerToGame(game, player, name);
        return UpdateFactory.getUpdate(provider);
    }

    public Map<String, Object> removeDeck(String name) {
        JolAdmin admin = JolAdmin.INSTANCE;
        String player = getPlayer().getPlayer();
        admin.removeDeck(player, name);
        PlayerModel model = getPlayer();
        if (model != null) {
            model.removeDeck();
        }
        return UpdateFactory.getUpdate(provider);
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
        OldCardSearch search = JolAdmin.INSTANCE.getAllCards();
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
        ret.put("callbackShowCards", beans);
        return ret;
    }

    private void endGameImpl(String name) {
        logger.error("Attempting to close " + name);
        PlayerModel player = getPlayer();
        logger.error(abean.getGameModel(name).getOwner());
        if (player.getPlayer().equals(abean.getGameModel(name).getOwner())) {
            logger.error("Closing " + name);
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
}
