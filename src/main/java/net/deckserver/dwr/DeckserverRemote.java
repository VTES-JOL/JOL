package net.deckserver.dwr;

import com.google.common.base.Strings;
import net.deckserver.dwr.creators.UpdateFactory;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.storage.json.deck.Deck;
import org.directwebremoting.WebContextFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeckserverRemote {

    private final JolAdmin admin;
    private final HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();

    public DeckserverRemote() {
        admin = JolAdmin.getInstance();
    }

    private static String ne(String arg) {
        if ("".equals(arg)) {
            return null;
        }
        return arg;
    }

    public static String getPlayer(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("meth");
    }

    public Map<String, Object> doPoll() {
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> createGame(String gameName, Boolean isPublic) {
        String playerName = getPlayer(request);
        if (!Strings.isNullOrEmpty(playerName)) {
            admin.createGame(gameName, isPublic, playerName);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> endGame(String name) {
        String playerName = getPlayer(request);
        if (playerName.equals(admin.getGameModel(name).getOwner())) {
            admin.endGame(name);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> invitePlayer(String game, String name) {
        String playerName = getPlayer(request);
        if (playerName != null) {
            admin.invitePlayer(game, name);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> unInvitePlayer(String game, String name) {
        String playerName = getPlayer(request);
        if (playerName != null) {
            admin.unInvitePlayer(game, name);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> registerDeck(String gameName, String deckName) {
        String playerName = getPlayer(request);
        if (!Strings.isNullOrEmpty(playerName)) {
            admin.registerDeck(gameName, playerName, deckName);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> startGame(String game) {
        String playerName = getPlayer(request);
        if ((admin.getOwner(game).equals(playerName) || admin.isSuperUser(playerName)) && admin.isStarting(game)) {
            admin.startGame(game);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> chat(String text) {
        String player = getPlayer(request);
        admin.chat(player, text);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> init() {
        String playerName = getPlayer(request);
        PlayerModel player = admin.getPlayerModel(playerName);
        player.resetChats();
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> navigate(String target) {
        String playerName = getPlayer(request);
        PlayerModel player = admin.getPlayerModel(playerName);
        if (target != null) {
            if (target.startsWith("g")) {
                player.enterGame(target.substring(1));
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

    public List<String> getHistory(String game, String turn) {
        List<String> ret = new ArrayList<>();
        if (game != null && turn != null) {
            GameAction[] actions = admin.getGame(game).getActions(turn);
            for (GameAction action : actions) {
                ret.add(action.getText());
            }
        }
        return ret;
    }

    public Deck getGameDeck(String gameName) {
        String playerName = getPlayer(request);
        if (gameName != null && !Strings.isNullOrEmpty(playerName)) {
            return admin.getGameDeck(gameName, playerName);
        }
        return null;
    }

    public Map<String, Object> doToggle(String game, String id) {
        GameView view = getView(game);
        view.toggleCollapsed(id);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> gameChat(String gameName, String chat) {
        String player = getPlayer(request);
        GameModel game = getModel(gameName);
        Map<String, Object> ret = UpdateFactory.getUpdate();
        // only process a command if the player is in the game
        if (game.getPlayers().contains(player)) {
            String status = game.chat(player, chat);
            ret = UpdateFactory.getUpdate();
            ret.put("showStatus", status);
        }
        return ret;
    }

    public Map<String, Object> submitForm(String gameName, String phase, String command, String chat,
                                          String ping, String endTurn, String global, String text) {
        String player = getPlayer(request);
        GameModel game = getModel(gameName);
        // only process a command if the player is in the game
        boolean isPlaying = game.getPlayers().contains(player);
        String status = null;
        if (isPlaying) {
            phase = ne(phase);
            command = ne(command);
            chat = ne(chat);
            ping = ne(ping);
            endTurn = ne(endTurn);
            status = game.submit(player, phase, command, chat, ping, endTurn, global, text);
        }
        Map<String, Object> ret = UpdateFactory.getUpdate();
        if (isPlaying)
            ret.put("showStatus", status);
        return ret;
    }

    public Map<String, Object> updateProfile(String email, String discordID, String veknID) {
        String player = getPlayer(request);
        admin.updateProfile(player, email, discordID, veknID);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> changePassword(String newPassword) {
        String player = getPlayer(request);
        admin.changePassword(player, newPassword);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> loadDeck(String deckName) {
        String playerName = getPlayer(request);
        admin.selectDeck(playerName, deckName);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> parseDeck(String deckName, String contents) {
        String playerName = getPlayer(request);
        admin.parseDeck(playerName, deckName, contents);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> newDeck() {
        String playerName = getPlayer(request);
        admin.newDeck(playerName);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> saveDeck(String deckName, String contents) {
        String playerName = getPlayer(request);
        admin.saveDeck(playerName, deckName, contents);
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> deleteDeck(String deckName) {
        String playerName = getPlayer(request);
        admin.deleteDeck(playerName, deckName);
        return UpdateFactory.getUpdate();
    }


    public Map<String, Object> setJudge(String name, boolean value) {
        String playerName = getPlayer(request);
        if (admin.isAdmin(playerName)) {
            admin.setJudge(name, value);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> setAdmin(String name, boolean value) {
        String playerName = getPlayer(request);
        if (admin.isAdmin(playerName)) {
            admin.setAdmin(name, value);
        }
        return UpdateFactory.getUpdate();
    }

    public Map<String, Object> setSuperUser(String name, boolean value) {
        String playerName = getPlayer(request);
        if (admin.isAdmin(playerName)) {
            admin.setSuperUser(name, value);
        }
        return UpdateFactory.getUpdate();
    }

    private GameView getView(String name) {
        String player = getPlayer(request);
        return getModel(name).getView(player);
    }

    private GameModel getModel(String name) {
        return admin.getGameModel(name);
    }

}
