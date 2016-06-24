package dsclient.modelimpl;

import nbclient.model.*;
import nbclient.vtesmodel.JolGame;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Writer;

import static org.slf4j.LoggerFactory.getLogger;

public final class ModelLoader {

    private static final Logger logger = getLogger(ModelLoader.class);

    public static void createModel(Game game, Game orig) {
        game.setName(orig.getName());
        moveNotes(orig, game);
        String[] players = orig.getPlayers();
        for (int i = 0; i < players.length; i++) {
            game.addPlayer(players[i]);
            game.addLocation(players[i], JolGame.READY_REGION);
            game.addLocation(players[i], JolGame.TORPOR);
            game.addLocation(players[i], JolGame.INACTIVE_REGION);
            game.addLocation(players[i], JolGame.HAND);
            game.addLocation(players[i], JolGame.ASHHEAP);
            game.addLocation(players[i], JolGame.LIBRARY);
            game.addLocation(players[i], JolGame.CRYPT);
            Location[] locs = (Location[]) orig.getPlayerLocations(players[i]);
            for (int j = 0; j < locs.length; j++) {
                moveLoc(game, orig, players[i], locs[j]);
            }
        }
    }

    private static void moveNotes(NoteTaker from, NoteTaker to) {
        Note[] notes = from.getNotes();
        for (int i = 0; i < notes.length; i++) {
            Note n = to.addNote(notes[i].getName());
            n.setValue(notes[i].getValue());
        }
    }

    private static void moveLoc(Game game, Game orig, String player, Location loc) {
        String name = orig.getPlayerRegionName(loc);
        Location to = (Location) game.getPlayerLocation(player, name);
        moveNotes(loc, to);
        moveCards(loc, to);
    }

    private static void moveCards(CardContainer from, CardContainer to) {
        Card[] cards = (Card[]) from.getCards();
        to.setCards(cards);
        for (int i = 0; i < cards.length; i++) {
            Card toCard = (Card) to.getCards()[i];
            moveNotes(cards[i], toCard);
            Card[] inner = (Card[]) cards[i].getCards();
            if (inner.length > 0) {
                moveCards(cards[i], toCard);
            }
        }
    }

    public static void createRecorder(TurnRecorder to, TurnRecorder orig) {
        String[] turns = orig.getTurns();
        for (int i = 0; i < turns.length; i++) {
            to.addTurn(orig.getMethTurn(turns[i]), turns[i]);
            GameAction[] actions = orig.getActions(turns[i]);
            for (int j = 0; j < actions.length; j++) {
                to.addCommand(turns[i], actions[j].getText(), actions[j].command());
            }
        }
    }

    public static void dumpState(Game game, Writer out) {
        try {
            out.write("Dumping " + game.getName() + "\n");
            dumpNotes(game, "    ", out);
            String[] players = game.getPlayers();
            for (int i = 0; i < players.length; i++) {
                out.write("  Player #" + i + " " + players[i] + "\n");
                Location[] locs = (Location[]) game.getPlayerLocations(players[i]);
                for (int j = 0; j < locs.length; j++) {
                    out.write("    Region" + game.getPlayerRegionName(locs[j]) + "\n");
                    dumpCards(locs[j], "      ", out);
                }
            }
        } catch (IOException ie) {
            logger.error("Error dumping state");
            ie.printStackTrace();
        }
    }

    private static void dumpCards(CardContainer box, String pre, Writer out) throws IOException {
        Card[] cards = (Card[]) box.getCards();
        for (int i = 0; i < cards.length; i++) {
            out.write(pre + "Card " + cards[i].getId() + "/" + cards[i].getCardId() + "\n");
            dumpNotes(cards[i], pre + "    ", out);
            dumpCards(cards[i], pre + "  ", out);
        }
    }

    private static void dumpNotes(NoteTaker notes, String pre, Writer out) throws IOException {
        Note[] note = notes.getNotes();
        for (int i = 0; i < note.length; i++) {
            out.write(pre + note[i].getName() + "/" + note[i].getValue() + "\n");
        }
    }
}
