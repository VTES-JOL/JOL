package net.deckserver.dwr;

import net.deckserver.Utils;
import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.bean.CardBean;
import net.deckserver.dwr.bean.DeckEditBean;
import net.deckserver.dwr.creators.UpdateFactory;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.game.storage.cards.CardType;
import org.directwebremoting.WebContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeckserverRemote {
    private static Logger logger = LoggerFactory.getLogger(DeckserverRemote.class);

    private final AdminBean abean;
    private HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();

    public DeckserverRemote() {
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
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> createGame(String name) {
        PlayerModel player = getPlayer();
        if (player != null && player.isAdmin()) {
            abean.createGame(name, player);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> setMessage(String message) {
        abean.setMessage(message);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> endGame(String name) {
        PlayerModel player = getPlayer();
        if (player.isSuperUser() || player.getPlayer().equals(abean.getGameModel(name).getOwner())) {
            abean.endGame(name);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> invitePlayer(String game, String name) {
        String player = getPlayer().getPlayer();
        JolAdmin admin = JolAdmin.getInstance();
        if (admin.isAdmin(player) || admin.isSuperUser(player)) {
            admin.invitePlayer(game, name);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> startGame(String game) {
        String player = getPlayer().getPlayer();
        JolAdmin admin = JolAdmin.getInstance();
        if ((admin.getOwner(game).equals(player) || admin.isSuperUser(player)) && admin.isOpen(game)) {
            admin.startGame(game);
            getModel(game).firstPing();
        }
        abean.notifyAboutGame(game);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> chat(String text) {
        String player = Utils.getPlayer(request);
        abean.chat(player, text);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> init() {
        String player = Utils.getPlayer(request);
        abean.remove(player);
        return UpdateFactory.getUpdate();
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
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> getState(String game, boolean forceLoad) {
        if (forceLoad) {
            getView(game).reset();
        }
        return UpdateFactory.getUpdate();
    }

    public String[] getHistory(String game, String turn) {
        String[] ret = new String[0];
        if (game != null && turn != null) {
            GameAction[] actions = JolAdmin.getInstance().getGame(game).getActions(turn);
            ret = new String[actions.length];
            for (int i = 0; i < actions.length; i++) {
                ret[i] = actions[i].getText();
            }
        }
        return ret;
    }

    public String getCardText(String callback, String id) {
        CardSearch cards = JolAdmin.getInstance().getAllCards();
        CardEntry card = cards.getCardById(id);
        return Stream.of(card.getFullText()).collect(Collectors.joining("<br/>"));
    }

    public Map<String, Object> doToggle(String game, String id) {
        GameView view = getView(game);
        view.toggleCollapsed(id);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> gameChat(String gamename, String chat) {
        String player = Utils.getPlayer(request);
        GameModel game = getModel(gamename);
        String status = game.chat(player, chat);
        Map<String, Object> ret = UpdateFactory.getUpdate();
        ret.put("showStatus", status);
        return ret;
    }

    public Map<String, Object> submitForm(String gamename, String phase, String command, String chat,
                                          String ping, String endTurn, String global, String text) {
        phase = ne(phase);
        command = ne(command);
        chat = ne(chat);
        ping = ne(ping);
        endTurn = ne(endTurn);
        String player = Utils.getPlayer(request);
        GameModel game = getModel(gamename);
        String status = game.submit(player, phase, command, chat, ping, endTurn, global, text);
        Map<String, Object> ret = UpdateFactory.getUpdate();
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
        Map<String, Object> ret = UpdateFactory.getUpdate();
        ret.put("callbackUpdateDeck", name);
        return ret;
    }

    public String gameDeck(String game) {
        String player = Utils.getPlayer(request);
        HttpServletResponse response = WebContextFactory.get().getHttpServletResponse();
        response.setContentType("text/javascript;charset=UTF-8");
        return JolAdmin.getInstance().getGameDeck(game, player);
    }

    public Map<String, Object> registerDeck(String game, String name) {
        JolAdmin admin = JolAdmin.getInstance();
        String player = getPlayer().getPlayer();
        admin.addPlayerToGame(game, player, name);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> removeDeck(String name) {
        JolAdmin admin = JolAdmin.getInstance();
        String player = getPlayer().getPlayer();
        admin.removeDeck(player, name);
        PlayerModel model = getPlayer();
        if (model != null) {
            model.removeDeck();
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> getDeck(String name) {
        Map<String, Object> ret = UpdateFactory.getUpdate();
        String player = getPlayer().getPlayer();
        DeckEditBean deck = new DeckEditBean(player, name);
        ret.put("showDeck", deck);
        return ret;
    }

    public Map<String, Object> refreshDeck(String name, String deck, String shuffle) {
        boolean doshuffle = "true".equals(shuffle);
        Map<String, Object> ret = UpdateFactory.getUpdate();
        getPlayer().setTmpDeck(name, deck);
        DeckEditBean bean = new DeckEditBean(deck, doshuffle);
        ret.put("showDeck", bean);
        return ret;
    }

    public Map<String, Object> cardSearch(String type, String string) {
        Map<String, Object> ret = UpdateFactory.getUpdate();
        CardSearch search = JolAdmin.getInstance().getAllCards();
        CardEntry[] set = search.getAllCards();
        type = ne(type);
        if (type != null && !type.equals("All")) {
            set = search.searchByType(set, type);
        }
        set = search.searchByText(set, string);
        CardBean[] beans = new CardBean[set.length];
        for (int i = 0, j = set.length - 1; i < set.length; i++, j--) {
            beans[j] = new CardBean(set[i]);
        }
        ret.put("callbackShowCards", beans);
        return ret;
    }

    public Map<String, Object> updateProfile(String email, boolean receivePing, boolean receiveSummary) {
        String player = Utils.getPlayer(request);
        JolAdmin.getInstance().updateProfile(player, email, receivePing, receiveSummary);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> changePassword(String newPassword) {
        String player = Utils.getPlayer(request);
        JolAdmin.getInstance().changePassword(player, newPassword);
        return UpdateFactory.getUpdate();
    }


    private PlayerModel getPlayer() {
        return Utils.getPlayerModel(request, abean);
    }

    private GameView getView(String name) {
        String player = Utils.getPlayer(request);
        GameModel gmodel = getModel(name);
        return gmodel.getView(player);
    }

    private GameModel getModel(String name) {
        return abean.getGameModel(name);
    }
}
