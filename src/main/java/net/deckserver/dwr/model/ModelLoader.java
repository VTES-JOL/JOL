package net.deckserver.dwr.model;

import com.google.common.base.Strings;
import net.deckserver.game.interfaces.state.*;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.interfaces.turn.TurnRecorder;
import net.deckserver.game.jaxb.XmlFileUtils;
import net.deckserver.game.jaxb.actions.GameActions;
import net.deckserver.game.jaxb.state.GameState;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.storage.state.RegionType;
import net.deckserver.game.storage.state.StoreGame;
import net.deckserver.game.storage.turn.StoreTurnRecorder;
import net.deckserver.game.ui.state.DsGame;
import net.deckserver.game.ui.turn.DsTurnRecorder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class ModelLoader {

    private static final Path BASE_PATH = Paths.get(System.getenv("JOL_DATA"));

    public static JolGame loadGame(String gameId) {
        Path gameStatePath = BASE_PATH.resolve("games").resolve(gameId).resolve("game.xml");
        GameState gameState = XmlFileUtils.loadGameState(gameStatePath);
        Path gameActionsPath = BASE_PATH.resolve("games").resolve(gameId).resolve("actions.xml");
        GameActions gameActions = XmlFileUtils.loadGameActions(gameActionsPath);
        DsGame deckServerState = new DsGame();
        DsTurnRecorder deckServerActions = new DsTurnRecorder();
        ModelLoader.createModel(deckServerState, new StoreGame(gameState));
        ModelLoader.createRecorder(deckServerActions, new StoreTurnRecorder(gameActions));
        return new JolGame(gameId, deckServerState, deckServerActions);
    }

    public static void saveGame(JolGame game) {
        String gameId = game.getId();
        Path gameStatePath = BASE_PATH.resolve("games").resolve(gameId).resolve("game.xml");
        Path gameActionsPath = BASE_PATH.resolve("games").resolve(gameId).resolve("actions.xml");
        Game deckServerState = game.getState();
        TurnRecorder deckServerActions = game.getTurnRecorder();
        GameState gameState = new GameState();
        GameActions gameActions = new GameActions();
        gameActions.setCounter("1");
        gameActions.setGameCounter("1");
        ModelLoader.createModel(new StoreGame(gameState), deckServerState);
        ModelLoader.createRecorder(new StoreTurnRecorder(gameActions), deckServerActions);
        XmlFileUtils.saveGameState(gameState, gameStatePath);
        XmlFileUtils.saveGameActions(gameActions, gameActionsPath);
    }

    public static void writeState(String id, DsGame state, String turn) {
        GameState gstate = new GameState();
        StoreGame wgame = new StoreGame(gstate);
        ModelLoader.createModel(wgame, state);
        String fileName = Strings.isNullOrEmpty(turn) ? "game.xml" : "game-" + turn + ".xml";
        Path gamePath = BASE_PATH.resolve("games").resolve(id).resolve(fileName);
        XmlFileUtils.saveGameState(gstate, gamePath);
    }

    public static void writeActions(String id, DsTurnRecorder actions, String turn) {
        GameActions gactions = new GameActions();
        gactions.setCounter("1");
        gactions.setGameCounter("1");
        StoreTurnRecorder wrec = new StoreTurnRecorder(gactions);
        ModelLoader.createRecorder(wrec, actions);
        String fileName = Strings.isNullOrEmpty(turn) ? "actions.xml" : "actions-" + turn + ".xml";
        Path actionsPath = BASE_PATH.resolve("games").resolve(id).resolve(fileName);
        XmlFileUtils.saveGameActions(gactions, actionsPath);
    }

    private static void createModel(Game game, Game orig) {
        game.setName(orig.getName());
        moveNotes(orig, game);
        List<String> players = orig.getPlayers();
        for (String player : players) {
            game.addPlayer(player);
            game.addLocation(player, RegionType.READY);
            game.addLocation(player, RegionType.TORPOR);
            game.addLocation(player, RegionType.UNCONTROLLED);
            game.addLocation(player, RegionType.HAND);
            game.addLocation(player, RegionType.ASH_HEAP);
            game.addLocation(player, RegionType.LIBRARY);
            game.addLocation(player, RegionType.CRYPT);
            game.addLocation(player, RegionType.REMOVED_FROM_GAME);
            game.addLocation(player, RegionType.RESEARCH);
            Location[] locs = orig.getPlayerLocations(player);
            for (Location loc : locs) {
                moveLoc(game, orig, player, loc);
            }
        }
    }

    private static void moveNotes(NoteTaker from, NoteTaker to) {
        List<Notation> notes = from.getNotes();
        for (Notation note : notes) {
            Notation n = to.addNote(note.getName());
            n.setValue(note.getValue());
        }
    }

    private static void moveLoc(Game game, Game orig, String player, Location loc) {
        String name = orig.getPlayerRegionName(loc);
        Location to = game.getPlayerLocation(player, RegionType.of(name));
        moveNotes(loc, to);
        moveCards(loc, to);
        loc.setOwner(player);
    }

    private static void moveCards(CardContainer from, CardContainer to) {
        Card[] cards = from.getCards();
        to.setCards(cards);
        for (int i = 0; i < cards.length; i++) {
            Card toCard = to.getCards()[i];
            moveNotes(cards[i], toCard);
            Card[] inner = cards[i].getCards();
            if (inner.length > 0) {
                moveCards(cards[i], toCard);
            }
        }
    }

    private static void createRecorder(TurnRecorder to, TurnRecorder orig) {
        String[] turns = orig.getTurns();
        for (String turn : turns) {
            to.addTurn(orig.getMethTurn(turn), turn);
            GameAction[] actions = orig.getActions(turn);
            for (GameAction action : actions) {
                to.addCommand(turn, action.getText(), action.command());
            }
        }
    }
}
