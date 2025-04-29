package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.turn.GameAction;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.storage.state.RegionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
public class DoCommandTest {

    private JolGame game;
    private DoCommand worker;

    @BeforeEach
    void setUp() {
        game = ModelLoader.loadGame("command-test");
        worker = new DoCommand(game);
    }

    @Test
    void burnTopLibrary() throws CommandException {
        Card[] ashCards = game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards();
        assertEquals("111", game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCard(0).getId());
        assertEquals(0, ashCards.length);
        worker.doCommand("Player2", "burn ready top");
        assertEquals("133", game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCard(0).getId());
        ashCards = game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards();
        assertEquals(2, ashCards.length);
        assertThat(getLastMessage(), containsString("Player2 burns <a class='card-name' data-card-id='201337'>Talley, The Hound</a> from top of their ready region"));
    }

    @Test
    void timeout() throws CommandException {
        List<Notation> notes = game.getState().getNotes();
        assertThat(notes, not(hasItems(new Notation("timeout", null))));
        worker.doCommand("Player2", "timeout");
        notes = game.getState().getNotes();
        assertThat(notes, hasItems(new Notation("timeout", "Player2")));
        assertThat(getLastMessage(), containsString("Player2 has requested that the game be timed out."));
    }

    @Test
    void timeoutComplete() throws CommandException {
        List<Notation> notes = game.getState().getNotes();
        assertThat(notes, not(hasItems(new Notation("timeout", null))));
        worker.doCommand("Player2", "timeout");
        assertThat(notes, hasItems(new Notation("timeout", "Player2")));
        worker.doCommand("Player3", "timeout");
        assertThat(game.getVictoryPoints("Player2"), is(0.5));
        assertThat(game.getPool("Player2"), is(0));
        assertThat(game.getVictoryPoints("Player3"), is(0.5));
        assertThat(game.getPool("Player3"), is(0));
        assertThat(getLastMessage(), containsString("Game has timed out.  Surviving players have been awarded ½ VP."));

    }

    @Test
    void vp() throws CommandException {
        assertThat(game.getVictoryPoints("Player2"), is(0.0));
        worker.doCommand("Player2", "vp +1");
        assertThat(game.getVictoryPoints("Player2"), is(1.0));
        assertThat(getLastMessage(), containsString("Player2 has gained 1 victory points."));
    }

    @Test
    void withdraw() throws CommandException {
        assertThat(game.getVictoryPoints("Player2"), is(0.0));
        worker.doCommand("Player2", "vp withdraw");
        assertThat(game.getVictoryPoints("Player2"), is(0.5));
        assertThat(getLastMessage(), containsString("Player2 withdraws."));
    }

    @Test
    void vpInvalid() {
        assertThat(game.getVictoryPoints("Player2"), is(0.0));
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "vp"));
    }

    @Test
    void choose() throws CommandException {
        List<Notation> notes = game.getState().getNotes();
        assertThat(notes, not(hasItems(new Notation("Player3-choice", null))));
        worker.doCommand("Player3", "choose 5");
        notes = game.getState().getNotes();
        assertThat(notes, hasItems(new Notation("Player3-choice", "5")));
        assertThat(getLastMessage(), containsString("Player3 has made their choice."));
    }

    @Test
    void reveal() throws CommandException {
        List<Notation> notes = game.getState().getNotes();
        assertThat(notes, not(hasItems(new Notation("Player3-choice", null), new Notation("Player5-choice", null))));
        worker.doCommand("Player3", "choose 5");
        worker.doCommand("Player5", "choose 1");
        notes = game.getState().getNotes();
        assertThat(notes, hasItems(new Notation("Player3-choice", "5"), new Notation("Player5-choice", "1")));
        worker.doCommand("Player4", "reveal");
        notes = game.getState().getNotes();
        assertThat(notes, hasItems(new Notation("Player3-choice", ""), new Notation("Player5-choice", "")));
    }

    @Test
    void label() throws CommandException {
        assertThat(game.getLabel("111"), is(""));
        worker.doCommand("Player5", "label PLayer2 ready 1 test");
        assertThat(game.getLabel("111"), is("test"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337'>Talley, The Hound</a> now \"test\""));
        worker.doCommand("Player5", "label Player2 ready 1");
        assertThat(game.getLabel("111"), is(""));
        assertThat(getLastMessage(), containsString("Removed label from <a class='card-name' data-card-id='201337'>Talley, The Hound</a>"));
    }

    @Test
    void votes() throws CommandException {
        assertThat(game.getVotes("111"), is(""));
        worker.doCommand("Player2", "votes ready 1 +1");
        assertThat(game.getVotes("111"), is("1"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337'>Talley, The Hound</a> now has 1 votes."));
        worker.doCommand("Player2", "votes ready 1 3");
        assertThat(game.getVotes("111"), is("3"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337'>Talley, The Hound</a> now has 3 votes."));
        assertThat(game.getVotes("318"), is("P"));
        worker.doCommand("Player3", "votes Player4 ready 1 0");
        assertThat(game.getVotes("318"), is("0"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201244'>Sascha Vykos, The Angel of Caine</a> now has no votes."));
        worker.doCommand("Player3", "votes Player4 ready 1 P");
        assertThat(game.getVotes("318"), is("P"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201244'>Sascha Vykos, The Angel of Caine</a> is priscus."));
        worker.doCommand("Player3", "votes Player4 ready 1 priscus");
        assertThat(game.getVotes("318"), is("P"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201244'>Sascha Vykos, The Angel of Caine</a> is priscus."));
    }

    @Test
    void random() throws CommandException {
        worker.doCommand("Player1", "random 6");
        assertThat(getLastMessage(), containsString("Player1 rolls from 1-6"));
        worker.doCommand("Player1", "random 0");
        assertThat(getLastMessage(), containsString("Player1 rolls from 1-2"));
    }

    @Test
    void flip() throws CommandException {
        worker.doCommand("Player5", "flip");
        worker.doCommand("Player5", "flip");
        worker.doCommand("Player5", "flip");
        worker.doCommand("Player5", "flip");
        assertThat(getLastMessage(), containsString("Player5 flips a coin : "));
    }

    @Test
    void discard() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(0));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards()[0], hasProperty("id", is("141")));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        worker.doCommand("Player2", "discard 1");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(6));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards()[0], hasProperty("id", is("141")));
        assertThat(getLastMessage(), containsString("Player2 discards <a class='card-name' data-card-id='100852'>Graverobbing</a>"));
    }

    @Test
    void discardDraw() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(0));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        worker.doCommand("Player2", "discard 1 draw");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards()[0], hasProperty("id", is("141")));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(getLastMessage(), containsString("Player2 draws from their library."));
    }

    @Test
    void discardRandom() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(0));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        worker.doCommand("Player2", "discard random");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(6));
        assertThat(getLastMessage(), containsString("Player2 discards"));
        assertThat(getLastMessage(), containsString("(picked randomly)"));
    }

    @Test
    void draw() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.LIBRARY.xmlLabel()).getCards()[0], hasProperty("id", is("143")));
        worker.doCommand("Player2", "draw");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(8));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards()[7], hasProperty("id", is("143")));
        assertThat(getLastMessage(), containsString("Player2 draws from their library."));
    }

    @Test
    void drawCrypt() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.CRYPT.xmlLabel()).getCards()[0], hasProperty("id", is("105")));
        worker.doCommand("Player2", "draw crypt");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(4));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.UNCONTROLLED.xmlLabel()).getCards()[3], hasProperty("id", is("105")));
        assertThat(getLastMessage(), containsString("Player2 draws from their crypt."));
    }

    @Test
    void edge() throws CommandException {
        assertThat(game.getEdge(), is("no one"));
        worker.doCommand("Player1", "edge");
        assertThat(game.getEdge(), is("Player1"));
        assertThat(getLastMessage(), containsString("Player1 gains the edge from no one."));
    }

    @Test
    void edgeMove() throws CommandException {
        assertThat(game.getEdge(), is("no one"));
        worker.doCommand("Player1", "edge");
        assertThat(game.getEdge(), is("Player1"));
        worker.doCommand("Player2", "edge");
        assertThat(game.getEdge(), is("Player2"));
        assertThat(getLastMessage(), containsString("Player2 gains the edge from Player1."));
    }

    @Test
    void edgeBurn() throws CommandException {
        assertThat(game.getEdge(), is("no one"));
        worker.doCommand("Player1", "edge");
        assertThat(game.getEdge(), is("Player1"));
        worker.doCommand("Player1", "edge burn");
        assertThat(getLastMessage(), containsString("Player1 burns the edge."));
        worker.doCommand("Player5", "edge");
        assertThat(getLastMessage(), containsString("Player5 gains the edge from no one."));
    }

    @Test
    void play() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards()[0], hasProperty("id", is("141")));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(0));
        worker.doCommand("Player2", "play 1");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(6));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards()[0], hasProperty("id", is("141")));
        assertThat(getLastMessage(), containsString("Player2 plays <a class='card-name' data-card-id='100852'>Graverobbing</a>."));
    }

    @Test
    void playWithMode() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards()[2], hasProperty("id", is("173")));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(0));
        worker.doCommand("Player2", "play 3 @ obt");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.HAND.xmlLabel()).getCards().length, is(6));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards()[0], hasProperty("id", is("173")));
        assertThat(getLastMessage(), containsString("Player2 plays <a class='card-name' data-card-id='101735'>Shadow Body</a> at <span class='icon obt'></span>."));
    }

    @Test
    void playToRegion() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards()[3], hasProperty("id", is("454")));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards().length, is(3));
        worker.doCommand("Player5", "play 4 ready");
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards().length, is(6));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards().length, is(4));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards()[3], hasProperty("id", is("454")));
        assertThat(getLastMessage(), containsString("Player5 plays <a class='card-name' data-card-id='100633'>Embrace, The</a> to their ready region."));
    }

    @Test
    void playAndDraw() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards()[3], hasProperty("id", is("454")));
        worker.doCommand("Player5", "play 4 ready draw");
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards()[3], hasProperty("id", is("454")));
        assertThat(getLastMessage(), containsString("Player5 draws from their library."));
    }

    @Test
    void playFromOutsideHand() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.LIBRARY.xmlLabel()).getCards().length, is(76));
        assertThat(game.getState().getCard("6").getCards().length, is(0));
        worker.doCommand("Player1", "play library 11 ready 1");
        assertThat(game.getState().getCard("6").getCards().length, is(1));
        assertThat(game.getState().getCard("6").getCards()[0], hasProperty("id", is("18")));
        assertThat(getLastMessage(), containsString("Player1 plays <a class='card-name' data-card-id='100070'>Animalism</a> from their library on <a class='card-name' data-card-id='200519'>Gillian Krader</a> in their ready region."));
    }

    @Test
    void playToAnotherPlayersCard() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.LIBRARY.xmlLabel()).getCards().length, is(76));
        assertThat(game.getState().getCard("6").getCards().length, is(0));
        worker.doCommand("Player1", "play library 11 Player2 ready 1");
        assertThat(game.getState().getCard("111").getCards().length, is(2));
        assertThat(game.getState().getCard("111").getCards()[1], hasProperty("id", is("18")));
        assertThat(getLastMessage(), containsString("Player1 plays <a class='card-name' data-card-id='100070'>Animalism</a> from their library on <a class='card-name' data-card-id='201337'>Talley, The Hound</a> in Player2's ready region."));
    }

    @Test
    void playToAnotherPlayersRegion() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.LIBRARY.xmlLabel()).getCards().length, is(76));
        assertThat(game.getState().getCard("6").getCards().length, is(0));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCards().length, is(2));
        worker.doCommand("Player1", "play library 11 Player2 ready");
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCards()[2], hasProperty("id", is("18")));
        assertThat(getLastMessage(), containsString("Player1 plays <a class='card-name' data-card-id='100070'>Animalism</a> from their library to Player2's ready region."));
    }

    @Test
    void playCrypt() {
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "play vamp"));
    }

    private String getLastMessage() {
        return Arrays.asList(game.getActions()).getLast().getText();
    }
}
