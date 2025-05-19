package net.deckserver.dwr.model;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.jaxb.state.Notation;
import net.deckserver.game.storage.state.RegionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
public class DoCommandTest {

    private JolGame game;
    private DoCommand worker;

    private String getLastMessage() {
        return Arrays.asList(game.getActions()).getLast().getText();
    }

    @BeforeAll
    public static void init() {
        JolAdmin.INSTANCE.setup();
    }

    @BeforeEach
    void setUp() {
        game = ModelLoader.loadGame("command-test");
        worker = new DoCommand(game, new GameModel("Command Test"));
    }

    @Test
    void burnTopLibrary() throws CommandException {
        Card[] ashCards = game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards();
        assertEquals("143", game.getState().getPlayerLocation("Player2", RegionType.LIBRARY.xmlLabel()).getCard(0).getId());
        assertEquals(0, ashCards.length);
        worker.doCommand("Player2", "burn library 1");
        assertEquals("176", game.getState().getPlayerLocation("Player2", RegionType.LIBRARY.xmlLabel()).getCard(0).getId());
        ashCards = game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards();
        assertEquals(1, ashCards.length);
        assertThat(getLastMessage(), containsString("Player2 burns <a class='card-name' data-card-id='101801'>Slaughtering the Herd</a> 1 from their library."));
    }

    @Test
    void burnReady() throws CommandException {
        assertEquals("111", game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCard(0).getId());
        assertEquals(0, game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length);
        worker.doCommand("Player2", "burn ready 1");
        assertEquals(1, game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length);
        assertThat(getLastMessage(), containsString("Player2 burns <a class='card-name' data-card-id='201337'>Talley, The Hound</a> from their ready region."));
    }

    @Test
    void burnRandom() throws CommandException{
        assertEquals("111", game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCard(0).getId());
        worker.doCommand("Player2", "burn ready random");
        assertEquals(1, game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length);
        assertThat(getLastMessage(), containsString("Player2 burns"));
        assertThat(getLastMessage(), containsString("(picked randomly)"));
        assertThat(getLastMessage(), containsString("from their ready region."));
    }

    @Test
    void burnAnotherPlayer() throws CommandException {
        assertEquals("111", game.getState().getPlayerLocation("Player2", RegionType.READY.xmlLabel()).getCard(0).getId());
        assertEquals(0, game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length);
        worker.doCommand("Player3", "burn Player2 ready 1");
        assertEquals(1, game.getState().getPlayerLocation("Player2", RegionType.ASH_HEAP.xmlLabel()).getCards().length);
        assertThat(getLastMessage(), containsString("Player3 burns <a class='card-name' data-card-id='201337'>Talley, The Hound</a> from Player2's ready region"));
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
        worker.doCommand("Player5", "Label Player2 ready 1 again");
        assertThat(game.getLabel("111"), is("again"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337'>Talley, The Hound</a> now \"again\""));
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
        assertThat(game.getState().getCard("111").getCards().length, is(1));
        assertThat(game.getState().getCard("111").getCards()[0], hasProperty("id", is("18")));
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

    @Test
    void influence() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(2));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.READY.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards()[0], hasProperty("id", is("4")));
        worker.doCommand("Player1", "influence 1");
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.READY.xmlLabel()).getCards().length, is(4));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.READY.xmlLabel()).getCards()[0], hasProperty("id", is("4")));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(1));
        assertThat(getLastMessage(), containsString("Player1 influences out <a class='card-name' data-card-id='201025'>Muse</a>, capacity: 3"));
    }

    @Test
    void influenceAgain() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(2));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.READY.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards()[0], hasProperty("id", is("4")));
        worker.doCommand("Player1", "influence 1");
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(1));
        worker.doCommand("Player1", "move ready 1 uncontrolled");
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(2));
        worker.doCommand("Player1", "influence 2");
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.READY.xmlLabel()).getCards()[0], hasProperty("id", is("4")));
        assertThat(getLastMessage(), containsString("Player1 influences out <a class='card-name' data-card-id='201025'>Muse</a>."));
    }

    @Test
    void influenceNoCapacity() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(4));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.UNCONTROLLED.xmlLabel()).getCards()[3], hasProperty("id", is("442")));
        worker.doCommand("Player5", "influence 4");
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards()[0], hasProperty("id", is("442")));
        assertThat(getLastMessage(), containsString("Player5 influences out <a class='card-name' data-card-id='102165'>Web of Knives Recruit</a>."));
    }

    @Test
    void influenceVotes() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player4", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player4", RegionType.READY.xmlLabel()).getCards().length, is(2));
        assertThat(game.getState().getPlayerLocation("Player4", RegionType.UNCONTROLLED.xmlLabel()).getCards()[2], hasProperty("id", is("317")));
        worker.doCommand("Player4", "influence 3");
        assertThat(game.getState().getPlayerLocation("Player4", RegionType.READY.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player4", RegionType.READY.xmlLabel()).getCards()[0], hasProperty("id", is("317")));
        assertThat(game.getState().getPlayerLocation("Player4", RegionType.UNCONTROLLED.xmlLabel()).getCards().length, is(2));
        assertThat(getLastMessage(), containsString("Player4 influences out <a class='card-name' data-card-id='200810'>Lambach</a>, capacity: 10, votes: 3"));
    }

    @Test
    void move() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards().length, is(1));
        worker.doCommand("Player5", "move Player3 ready 1 ready");
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.READY.xmlLabel()).getCards().length, is(4));
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards().length, is(0));
        assertThat(getLastMessage(), containsString("Player5 moves <a class='card-name' data-card-id='200788'>Klaus van der Veken</a> to Player5's ready region."));
    }

    @Test
    void moveTopSort() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards().length, is(1));
        worker.doCommand("Player3", "move ready 1 top");
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards().length, is(1));
        assertThat(getLastMessage(), containsString("Player3 moves <a class='card-name' data-card-id='200788'>Klaus van der Veken</a> to the top of their ready region."));

    }

    @Test
    void moveTop() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.LIBRARY.xmlLabel()).getCards().length, is(64));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards().length, is(7));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards()[0], hasProperty("id", is("489")));
        worker.doCommand("Player5", "move hand 1 library top");
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.LIBRARY.xmlLabel()).getCards().length, is(65));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.HAND.xmlLabel()).getCards().length, is(6));
        assertThat(game.getState().getPlayerLocation("Player5", RegionType.LIBRARY.xmlLabel()).getCards()[0], hasProperty("id", is("489")));
        assertThat(getLastMessage(), containsString("Player5 moves card #1 in their hand to the top of their library."));
    }

    @Test
    void moveToCard() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards()[0].getCards().length, is(2));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.READY.xmlLabel()).getCards()[2].getCards().length, is(0));
        worker.doCommand("Player1", "move Player3 ready 1.1 ready 3");
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards()[0].getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.READY.xmlLabel()).getCards()[2].getCards().length, is(1));
        assertThat(getLastMessage(), containsString("Player1 puts <a class='card-name' data-card-id='101014'>Ivory Bow</a> on <a class='card-name' data-card-id='100298'>Carlton Van Wyk</a> in their ready region."));
    }

    @Test
    void moveCardLoop() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards()[0].getCards().length, is(2));
        assertThrows(CommandException.class, () -> worker.doCommand("Player3", "move ready 1 ready 1.1"));
    }

    @Test
    void moveComplicated() throws CommandException {
        assertThat(game.getState().getCard("208").getCards().length, is(2));
        assertThat(game.getState().getCard("246").getCards().length, is(0));
        worker.doCommand("Player3", "move ready 1.2 ready 1.1");
        assertThat(getLastMessage(), containsString("Player3 puts <a class='card-name' data-card-id='100199'>Blood Doll</a> 1.2 on <a class='card-name' data-card-id='101014'>Ivory Bow</a> in their ready region."));
        assertThat(game.getState().getCard("208").getCards().length, is(1));
        assertThat(game.getState().getCard("246").getCards().length, is(1));
        assertThrows(CommandException.class, () -> worker.doCommand("Player3", "move ready 1 ready 1.1.1"));
    }

    @Test
    void moveSelf() {
        assertThrows(CommandException.class, () -> worker.doCommand("Player1", "move Player1 ready 1 ready 1"));
    }

    @Test
    void pool() throws CommandException {
        assertThat(game.getPool("Player4"), is(22));
        worker.doCommand("Player4", "pool +3");
        assertThat(game.getPool("Player4"), is(25));
        assertThat(getLastMessage(), containsString("Player4's pool was 22, now is 25."));
        assertThat(game.getPool("Player2"), is(30));
        worker.doCommand("Player4", "pool Player2 -3");
        assertThat(game.getPool("Player2"), is(27));
        assertThat(getLastMessage(), containsString("Player2's pool was 30, now is 27."));
        assertThrows(CommandException.class, () -> worker.doCommand("Player4", "pool"));
    }

    @Test
    void poolNeedsSign() {
        assertThrows(CommandException.class, () -> worker.doCommand("Player4", "pool 3"));
    }

    @Test
    void poolOtherPlayer() throws CommandException {
        assertThat(game.getPool("Player4"), is(22));
        worker.doCommand("Player2", "pool Player4 +3");
        assertThat(game.getPool("Player4"), is(25));
        assertThat(getLastMessage(), containsString("Player4's pool was 22, now is 25."));
    }

    @Test
    void blood() throws CommandException {
        assertThat(game.getCounters("111"), is(6));
        worker.doCommand("Player2", "blood ready 1 +1");
        assertThat(game.getCounters("111"), is(7));
        assertThat(getLastMessage(), containsString("Player2 adds 1 blood to <a class='card-name' data-card-id='201337'>Talley, The Hound</a>, now 7."));
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "blood ready 2"));
    }

    @Test
    void contest() throws CommandException {
        assertThat(game.getContested("111"), is(false));
        worker.doCommand("Player2", "contest ready 1");
        assertThat(game.getContested("111"), is(true));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337'>Talley, The Hound</a> is now contested."));
        worker.doCommand("Player2", "contest ready 1 clear");
        assertThat(game.getContested("111"), is(false));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337'>Talley, The Hound</a> is no longer contested."));
    }

    @Test
    void disciplines() throws CommandException {
        assertThat(game.getDisciplines("111"), contains("aus", "dom", "OBT", "POT"));
        worker.doCommand("Player2", "disc ready 1 +ani");
        assertThat(game.getDisciplines("111"), contains("ani", "aus", "dom", "OBT", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon ani'></span> to <a class='card-name' data-card-id='201337'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 -obt");
        assertThat(game.getDisciplines("111"), contains("ani", "aus", "dom", "obt", "POT"));
        assertThat(getLastMessage(), containsString("Player2 removed <span class='icon obt'></span> to <a class='card-name' data-card-id='201337'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 +dom");
        assertThat(game.getDisciplines("111"), contains("ani", "aus", "obt", "DOM", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon DOM'></span> to <a class='card-name' data-card-id='201337'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 +ani");
        assertThat(game.getDisciplines("111"), contains("aus", "obt", "ANI", "DOM", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon ANI'></span> to <a class='card-name' data-card-id='201337'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 reset");
        assertThat(game.getDisciplines("111"), contains("aus", "dom", "OBT", "POT"));
        assertThat(getLastMessage(), containsString("Player2 reset <a class='card-name' data-card-id='201337'>Talley, The Hound</a> back to <span class='icon aus'></span> <span class='icon dom'></span> <span class='icon OBT'></span> <span class='icon POT'></span>"));
        worker.doCommand("Player2", "disc ready 1 +ani +dom");
        assertThat(game.getDisciplines("111"), contains("ani", "aus", "DOM", "OBT", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon ani'></span> <span class='icon DOM'></span> to <a class='card-name' data-card-id='201337'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 2 reset");
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "disc ready 1 +blah"));
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "disc ready 1 aani"));
    }

    @Test
    void capacity() throws CommandException{
        assertThat(game.getCapacity("111"), is(6));
        worker.doCommand("Player2", "capacity ready 1 +1");
        assertThat(game.getCapacity("111"), is(7));
        assertThat(getLastMessage(), containsString("Capacity of <a class='card-name' data-card-id='201337'>Talley, The Hound</a> now 7"));
        worker.doCommand("Player2", "capacity ready 1 -1");
        assertThat(getLastMessage(), containsString("Capacity of <a class='card-name' data-card-id='201337'>Talley, The Hound</a> now 6"));
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "capacity ready 2"));
        worker.doCommand("Player2", "capacity ready 1 -7");
        assertThat(getLastMessage(), containsString("Capacity of <a class='card-name' data-card-id='201337'>Talley, The Hound</a> now 0"));
    }

    @Test
    void unlockAll() throws CommandException {
        assertThat(game.isTapped("6"), is(false));
        assertThat(game.isTapped("11"), is(true));
        assertThat(game.isTapped("37"), is(true));
        worker.doCommand("Player1", "unlock");
        assertThat(game.isTapped("6"), is(false));
        assertThat(game.isTapped("11"), is(false));
        assertThat(game.isTapped("37"), is(false));
        assertThat(getLastMessage(), containsString("Player1 unlocks."));
    }

    @Test
    void unlock() throws CommandException {
        assertThat(game.isTapped("6"), is(false));
        assertThat(game.isTapped("11"), is(true));
        assertThat(game.isTapped("37"), is(true));
        worker.doCommand("Player1", "unlock ready 2");
        assertThat(game.isTapped("6"), is(false));
        assertThat(game.isTapped("11"), is(false));
        assertThat(game.isTapped("37"), is(true));
        assertThat(getLastMessage(), containsString("Player1 unlocks <a class='card-name' data-card-id='201039'>Navar McClaren</a>."));
    }

    @Test
    void lock() throws CommandException {
        assertThat(game.isTapped("6"), is(false));
        assertThat(game.isTapped("11"), is(true));
        assertThat(game.isTapped("37"), is(true));
        worker.doCommand("Player1", "lock ready 1");
        assertThat(game.isTapped("6"), is(true));
        assertThat(game.isTapped("11"), is(true));
        assertThat(game.isTapped("37"), is(true));
        assertThat(getLastMessage(), containsString("Player1 locks <a class='card-name' data-card-id='200519'>Gillian Krader</a>."));
        assertThrows(CommandException.class, () -> worker.doCommand("Player1", "lock ready 1"));
    }

    @Test
    void order() throws CommandException {
        assertThat(game.getPlayers(), contains("Player2", "Player4", "Player5", "Player3", "Player1"));
        worker.doCommand("Player1", "order 5 1 4 2 3");
        assertThat(game.getPlayers(), contains("Player1", "Player2", "Player3", "Player4", "Player5"));
        assertThat(getLastMessage(), containsString("Player order Player1 Player2 Player3 Player4 Player5"));
        assertThrows(CommandException.class, () -> worker.doCommand("Player1", "order Player1 Player2 Player3 Player4 Player5"));
        assertThrows(CommandException.class, () -> worker.doCommand("Player1", "order 6 5 4 3 2 1"));
        assertThrows(CommandException.class, () -> worker.doCommand("Player1", "order -3 1 2 3 4 5"));
    }

    @Test
    void showAll() throws CommandException {
        assertThat(game.getPrivateNotes("Player2"), is(""));
        worker.doCommand("Player3", "show library all");
        assertThat(game.getPrivateNotes("Player2"), not(is("")));
        assertThat(getLastMessage(), containsString("Player3 shows everyone 81 cards of their library."));
        worker.doCommand("Player3", "show hand all");
        assertThat(game.getPrivateNotes("Player2"), not(is("")));
        assertThat(getLastMessage(), containsString("Player3 shows everyone 7 cards of their hand."));
    }

    @Test
    void showPlayer() throws CommandException {
        assertThat(game.getPrivateNotes("Player4"), is(""));
        worker.doCommand("Player3", "show hand Player4 all");
        assertThat(game.getPrivateNotes("Player4"), not(is("")));
        assertThat(getLastMessage(), containsString("Player3 shows Player4 7 cards of their hand."));
    }

    @Test
    void showSelf() throws CommandException {
        assertThat(game.getPrivateNotes("Player3"), is(""));
        worker.doCommand("Player3", "show hand");
        assertThat(game.getPrivateNotes("Player3"), not(is("")));
        assertThat(getLastMessage(), containsString("Player3 looks at 7 cards of their hand."));
    }

    @Test
    void shuffle() throws CommandException {
        List<String> cards = Stream.of(game.getState().getPlayerLocation("Player3", RegionType.HAND.xmlLabel()).getCards()).map(Card::getId).toList();
        assertThat(cards, contains("283", "299", "235", "257", "252", "242", "264"));
        worker.doCommand("Player3", "shuffle hand");
        cards = Stream.of(game.getState().getPlayerLocation("Player3", RegionType.HAND.xmlLabel()).getCards()).map(Card::getId).toList();
        assertThat(cards, not(contains("283", "299", "235", "257", "252", "242", "264")));
        assertThat(cards, containsInAnyOrder("283", "299", "235", "257", "252", "242", "264"));
        assertThat(getLastMessage(), containsString("Player3 shuffles their hand."));
    }

    @Test
    void shuffleLibrary() throws CommandException {
        worker.doCommand("Player3", "shuffle library 200");
        assertThat(getLastMessage(), containsString("Player3 shuffles their library."));
    }

    @Test
    void shufflePartial() throws CommandException {
        worker.doCommand("Player3", "shuffle library 7");
        assertThat(getLastMessage(), containsString("Player3 shuffles the first 7 cards of their library."));
    }

    @Test
    void transferOn() throws CommandException {
        assertThat(game.getCounters("111"), is(6));
        assertThat(game.getPool("Player2"), is(30));
        worker.doCommand("Player2", "transfer ready 1 +1");
        assertThat(game.getCounters("111"), is(7));
        assertThat(game.getPool("Player2"), is(29));
        assertThat(getLastMessage(), containsString("Player2 transferred 1 blood onto <a class='card-name' data-card-id='201337'>Talley, The Hound</a>. Currently: 7, Pool: 29"));
    }

    @Test
    void rfg() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards().length, is(1));
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.REMOVED_FROM_GAME.xmlLabel()).getCards().length, is(0));
        worker.doCommand("Player5", "rfg Player3 ready 1");
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.READY.xmlLabel()).getCards().length, is(0));
        assertThat(game.getState().getPlayerLocation("Player3", RegionType.REMOVED_FROM_GAME.xmlLabel()).getCards().length, is(1));
        assertThat(getLastMessage(), containsString("Player5 removes <a class='card-name' data-card-id='200788'>Klaus van der Veken</a> in Player3's ready region from the game."));
    }

    @Test
    void rfgRandom() throws CommandException {
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(4));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.REMOVED_FROM_GAME.xmlLabel()).getCards().length, is(0));
        worker.doCommand("Player5", "rfg Player1 ash random");
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.ASH_HEAP.xmlLabel()).getCards().length, is(3));
        assertThat(game.getState().getPlayerLocation("Player1", RegionType.REMOVED_FROM_GAME.xmlLabel()).getCards().length, is(1));
        assertThat(getLastMessage(), containsString("Player5 removes"));
        assertThat(getLastMessage(), containsString("(picked randomly)"));
        assertThat(getLastMessage(), containsString("from the game."));

    }
}
