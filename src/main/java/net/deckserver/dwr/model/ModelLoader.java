package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.*;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.interfaces.turn.TurnRecorder;
import net.deckserver.game.jaxb.state.Notation;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public final class ModelLoader {

    private static final Logger logger = getLogger(ModelLoader.class);

    public static void createModel(Game game, Game orig) {
        game.setName(orig.getName());
        moveNotes(orig, game);
        List<String> players = orig.getPlayers();
        for (String player : players) {
            game.addPlayer(player);
            game.addLocation(player, JolGame.READY_REGION);
            game.addLocation(player, JolGame.TORPOR);
            game.addLocation(player, JolGame.INACTIVE_REGION);
            game.addLocation(player, JolGame.HAND);
            game.addLocation(player, JolGame.ASHHEAP);
            game.addLocation(player, JolGame.LIBRARY);
            game.addLocation(player, JolGame.CRYPT);
            game.addLocation(player, JolGame.RFG);
            game.addLocation(player, JolGame.RESEARCH);
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
        Location to = game.getPlayerLocation(player, name);
        moveNotes(loc, to);
        moveCards(loc, to);
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

    public static void createRecorder(TurnRecorder to, TurnRecorder orig) {
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
