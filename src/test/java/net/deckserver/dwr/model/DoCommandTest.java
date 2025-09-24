package net.deckserver.dwr.model;

import net.deckserver.storage.json.cards.Path;
import net.deckserver.storage.json.cards.RegionType;
import net.deckserver.storage.json.game.CardData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
public class DoCommandTest {

    private JolGame game;
    private DoCommand worker;

    @BeforeAll
    public static void init() {
        JolAdmin.INSTANCE.setup();
        JolAdmin.INSTANCE.upgrade();
    }

    private String getLastMessage() {
        return Arrays.asList(game.getActions()).getLast().getText();
    }

    @BeforeEach
    void setUp() {
        game = ModelLoader.loadGame("command-test", true);
        worker = new DoCommand(game, new GameModel("Command Test"));
    }

    @Test
    void burnTopLibrary() throws CommandException {
        List<? extends CardData> ashCards = game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards();
        assertEquals("143", game.getData().getPlayerRegion("Player2", RegionType.LIBRARY).getCard(0).getId());
        assertEquals(0, ashCards.size());
        worker.doCommand("Player2", "burn library 1");
        assertEquals("176", game.getData().getPlayerRegion("Player2", RegionType.LIBRARY).getCard(0).getId());
        ashCards = game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards();
        assertEquals(1, ashCards.size());
        assertThat(getLastMessage(), containsString("Player2 burns <a class='card-name' data-card-id='101801' data-secured='false'>Slaughtering the Herd</a> 1 from their library."));
    }

    @Test
    void burnReady() throws CommandException {
        assertEquals("111", game.getData().getPlayerRegion("Player2", RegionType.READY).getCard(0).getId());
        assertEquals(0, game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size());
        worker.doCommand("Player2", "burn ready 1");
        assertEquals(1, game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size());
        assertThat(getLastMessage(), containsString("Player2 burns <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> from their ready region."));
    }

    @Test
    void burnRandom() throws CommandException {
        assertEquals("111", game.getData().getPlayerRegion("Player2", RegionType.READY).getCard(0).getId());
        worker.doCommand("Player2", "burn ready random");
        assertEquals(1, game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size());
        assertThat(getLastMessage(), containsString("Player2 burns"));
        assertThat(getLastMessage(), containsString("(picked randomly)"));
        assertThat(getLastMessage(), containsString("from their ready region."));
    }

    @Test
    void burnAnotherPlayer() throws CommandException {
        assertEquals("111", game.getData().getPlayerRegion("Player2", RegionType.READY).getCard(0).getId());
        assertEquals(0, game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size());
        worker.doCommand("Player3", "burn Player2 ready 1");
        assertEquals(1, game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size());
        assertThat(getLastMessage(), containsString("Player3 burns <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> from Player2's ready region"));
    }

    @Test
    void timeout() throws CommandException {
        String requestor = game.getData().getTimeoutRequestor();
        assertNull(requestor);
        worker.doCommand("Player2", "timeout");
        requestor = game.getData().getTimeoutRequestor();
        assertThat(requestor, is("Player2"));
        assertThat(getLastMessage(), containsString("Player2 has requested that the game be timed out."));
    }

    @Test
    void timeoutComplete() throws CommandException {
        String requestor = game.getData().getTimeoutRequestor();
        assertNull(requestor);
        worker.doCommand("Player2", "timeout");
        requestor = game.getData().getTimeoutRequestor();
        assertThat(requestor, is("Player2"));
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
        String choice = game.getData().getPlayer("Player3").getChoice();
        assertNull(choice);
        worker.doCommand("Player3", "choose 5");
        choice = game.getData().getPlayer("Player3").getChoice();
        assertThat(choice, is("5"));
        assertThat(getLastMessage(), containsString("Player3 has made their choice."));
    }

    @Test
    void reveal() throws CommandException {
        String player3Choice = game.getData().getPlayer("Player3").getChoice();
        String player5Choice = game.getData().getPlayer("Player5").getChoice();
        assertNull(player3Choice);
        assertNull(player5Choice);
        worker.doCommand("Player3", "choose 5");
        worker.doCommand("Player5", "choose 1");
        player3Choice = game.getData().getPlayer("Player3").getChoice();
        player5Choice = game.getData().getPlayer("Player5").getChoice();
        assertThat(player3Choice, is("5"));
        assertThat(player5Choice, is("1"));
        worker.doCommand("Player4", "reveal");
        player3Choice = game.getData().getPlayer("Player3").getChoice();
        player5Choice = game.getData().getPlayer("Player5").getChoice();
        assertNull(player3Choice);
        assertNull(player5Choice);
    }

    @Test
    void label() throws CommandException {
        CardData card = game.getCard("111");
        assertThat(game.getLabel(card), is(""));
        worker.doCommand("Player5", "label PLayer2 ready 1 test");
        assertThat(game.getLabel(card), is("test"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>: \"test\""));
        worker.doCommand("Player5", "label Player2 ready 1");
        assertThat(game.getLabel(card), is(""));
        assertThat(getLastMessage(), containsString("removes label from <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>"));
        worker.doCommand("Player5", "Label Player2 ready 1 again");
        assertThat(game.getLabel(card), is("again"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>: \"again\""));
    }

    @Test
    void votes() throws CommandException {
        CardData card = game.getCard("111");
        assertThat(game.getVotes(card), is(""));
        worker.doCommand("Player2", "votes ready 1 +1");
        assertThat(game.getVotes(card), is("1"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> now has 1 votes."));
        worker.doCommand("Player2", "votes ready 1 3");
        assertThat(game.getVotes(card), is("3"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> now has 3 votes."));
        card = game.getCard("318");
        assertThat(game.getVotes(card), is("P"));
        worker.doCommand("Player3", "votes Player4 ready 1 0");
        assertThat(game.getVotes(card), is("0"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201244' data-secured='false'>Sascha Vykos, The Angel of Caine</a> now has no votes."));
        worker.doCommand("Player3", "votes Player4 ready 1 P");
        assertThat(game.getVotes(card), is("P"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201244' data-secured='false'>Sascha Vykos, The Angel of Caine</a> is priscus."));
        worker.doCommand("Player3", "votes Player4 ready 1 priscus");
        assertThat(game.getVotes(card), is("P"));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201244' data-secured='false'>Sascha Vykos, The Angel of Caine</a> is priscus."));
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
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(0));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().getFirst(), hasProperty("id", is("141")));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(7));
        worker.doCommand("Player2", "discard 1");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(6));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().getFirst(), hasProperty("id", is("141")));
        assertThat(getLastMessage(), containsString("Player2 discards <a class='card-name' data-card-id='100852' data-secured='false'>Graverobbing</a>"));
    }

    @Test
    void discardDraw() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(0));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(7));
        worker.doCommand("Player2", "discard 1 draw");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().getFirst(), hasProperty("id", is("141")));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(7));
        assertThat(getLastMessage(), containsString("Player2 draws from their library."));
    }

    @Test
    void discardRandom() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(0));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(7));
        worker.doCommand("Player2", "discard random");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(6));
        assertThat(getLastMessage(), containsString("Player2 discards"));
        assertThat(getLastMessage(), containsString("(picked randomly)"));
    }

    @Test
    void draw() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(7));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.LIBRARY).getCards().getFirst(), hasProperty("id", is("143")));
        worker.doCommand("Player2", "draw");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(8));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().get(7), hasProperty("id", is("143")));
        assertThat(getLastMessage(), containsString("Player2 draws from their library."));
    }

    @Test
    void drawCrypt() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.UNCONTROLLED).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.CRYPT).getCards().getFirst(), hasProperty("id", is("105")));
        worker.doCommand("Player2", "draw crypt");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.UNCONTROLLED).getCards().size(), is(4));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.UNCONTROLLED).getCards().get(3), hasProperty("id", is("105")));
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
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(7));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().getFirst(), hasProperty("id", is("141")));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(0));
        worker.doCommand("Player2", "play 1");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(6));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().getFirst(), hasProperty("id", is("141")));
        assertThat(getLastMessage(), containsString("Player2 plays <a class='card-name' data-card-id='100852' data-secured='false'>Graverobbing</a>."));
    }

    @Test
    void playWithMode() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(7));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().get(2), hasProperty("id", is("173")));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(0));
        worker.doCommand("Player2", "play 3 @ obt");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.HAND).getCards().size(), is(6));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.ASH_HEAP).getCards().getFirst(), hasProperty("id", is("173")));
        assertThat(getLastMessage(), containsString("Player2 plays <a class='card-name' data-card-id='101735' data-secured='false'>Shadow Body</a> at <span class='icon obt'></span>."));
    }

    @Test
    void playToRegion() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().size(), is(7));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().get(3), hasProperty("id", is("454")));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().size(), is(3));
        worker.doCommand("Player5", "play 4 ready");
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().size(), is(6));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().size(), is(4));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().get(3), hasProperty("id", is("454")));
        assertThat(getLastMessage(), containsString("Player5 plays <a class='card-name' data-card-id='100633' data-secured='false'>Embrace, The</a> to their ready region."));
    }

    @Test
    void playAndDraw() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().size(), is(7));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().get(3), hasProperty("id", is("454")));
        worker.doCommand("Player5", "play 4 ready draw");
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().size(), is(7));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().get(3), hasProperty("id", is("454")));
        assertThat(getLastMessage(), containsString("Player5 draws from their library."));
    }

    @Test
    void playFromOutsideHand() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.LIBRARY).getCards().size(), is(76));
        assertThat(game.getData().getCard("6").getCards().size(), is(0));
        worker.doCommand("Player1", "play library 11 ready 1");
        assertThat(game.getData().getCard("6").getCards().size(), is(1));
        assertThat(game.getData().getCard("6").getCards().getFirst(), hasProperty("id", is("18")));
        assertThat(getLastMessage(), containsString("Player1 plays <a class='card-name' data-card-id='100070' data-secured='false'>Animalism</a> from their library on <a class='card-name' data-card-id='200519' data-secured='false'>Gillian Krader</a> in their ready region."));
    }

    @Test
    void playToAnotherPlayersCard() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.LIBRARY).getCards().size(), is(76));
        assertThat(game.getData().getCard("6").getCards().size(), is(0));
        worker.doCommand("Player1", "play library 11 Player2 ready 1");
        assertThat(game.getCard("111").getCards().size(), is(1));
        assertThat(game.getCard("111").getCards().getFirst(), hasProperty("id", is("18")));
        assertThat(getLastMessage(), containsString("Player1 plays <a class='card-name' data-card-id='100070' data-secured='false'>Animalism</a> from their library on <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> in Player2's ready region."));
    }

    @Test
    void playToAnotherPlayersRegion() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.LIBRARY).getCards().size(), is(76));
        assertThat(game.getData().getCard("6").getCards().size(), is(0));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.READY).getCards().size(), is(2));
        worker.doCommand("Player1", "play library 11 Player2 ready");
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.READY).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player2", RegionType.READY).getCards().get(2), hasProperty("id", is("18")));
        assertThat(getLastMessage(), containsString("Player1 plays <a class='card-name' data-card-id='100070' data-secured='false'>Animalism</a> from their library to Player2's ready region."));
    }

    @Test
    void playCrypt() {
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "play vamp"));
    }

    @Test
    void influence() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().size(), is(2));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.READY).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().getFirst(), hasProperty("id", is("4")));
        worker.doCommand("Player1", "influence 1");
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.READY).getCards().size(), is(4));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.READY).getCards().getFirst(), hasProperty("id", is("4")));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().size(), is(1));
        assertThat(getLastMessage(), containsString("Player1 influences out <a class='card-name' data-card-id='201025' data-secured='false'>Muse</a>."));
    }

    @Test
    void influenceAgain() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().size(), is(2));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.READY).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().getFirst(), hasProperty("id", is("4")));
        worker.doCommand("Player1", "influence 1");
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().size(), is(1));
        worker.doCommand("Player1", "move ready 1 uncontrolled");
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().size(), is(2));
        worker.doCommand("Player1", "influence 2");
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.UNCONTROLLED).getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.READY).getCards().getFirst(), hasProperty("id", is("4")));
        assertThat(getLastMessage(), containsString("Player1 influences out <a class='card-name' data-card-id='201025' data-secured='false'>Muse</a>."));
    }

    @Test
    void influenceNoCapacity() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.UNCONTROLLED).getCards().size(), is(4));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.UNCONTROLLED).getCards().get(3), hasProperty("id", is("442")));
        worker.doCommand("Player5", "influence 4");
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().getFirst(), hasProperty("id", is("442")));
        assertThat(getLastMessage(), containsString("Player5 influences out <a class='card-name' data-card-id='102165' data-secured='false'>Web of Knives Recruit</a>."));
    }

    @Test
    void influenceVotes() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player4", RegionType.UNCONTROLLED).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player4", RegionType.READY).getCards().size(), is(2));
        assertThat(game.getData().getPlayerRegion("Player4", RegionType.UNCONTROLLED).getCards().get(2), hasProperty("id", is("317")));
        worker.doCommand("Player4", "influence 3");
        assertThat(game.getData().getPlayerRegion("Player4", RegionType.READY).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player4", RegionType.READY).getCards().getFirst(), hasProperty("id", is("317")));
        assertThat(game.getData().getPlayerRegion("Player4", RegionType.UNCONTROLLED).getCards().size(), is(2));
        assertThat(getLastMessage(), containsString("Player4 influences out <a class='card-name' data-card-id='200810' data-secured='false'>Lambach</a>, votes: 3."));
    }

    @Test
    void move() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().size(), is(1));
        worker.doCommand("Player5", "move Player3 ready 1 ready");
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.READY).getCards().size(), is(4));
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().size(), is(0));
        assertThat(getLastMessage(), containsString("Player5 moves <a class='card-name' data-card-id='200788' data-secured='false'>Klaus van der Veken</a> to Player5's ready region."));
    }

    @Test
    void moveTopSort() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().size(), is(1));
        worker.doCommand("Player3", "move ready 1 top");
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().size(), is(1));
        assertThat(getLastMessage(), containsString("Player3 moves <a class='card-name' data-card-id='200788' data-secured='false'>Klaus van der Veken</a> to the top of their ready region."));

    }

    @Test
    void moveTop() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.LIBRARY).getCards().size(), is(64));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().size(), is(7));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().getFirst(), hasProperty("id", is("489")));
        worker.doCommand("Player5", "move hand 1 library top");
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.LIBRARY).getCards().size(), is(65));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.HAND).getCards().size(), is(6));
        assertThat(game.getData().getPlayerRegion("Player5", RegionType.LIBRARY).getCards().getFirst(), hasProperty("id", is("489")));
        assertThat(getLastMessage(), containsString("Player5 moves card #1 in their hand to the top of their library."));
    }

    @Test
    void moveToCard() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().getFirst().getCards().size(), is(2));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.READY).getCards().get(2).getCards().size(), is(0));
        worker.doCommand("Player1", "move Player3 ready 1.1 ready 3");
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().getFirst().getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.READY).getCards().get(2).getCards().size(), is(1));
        assertThat(getLastMessage(), containsString("Player1 puts <a class='card-name' data-card-id='101014' data-secured='false'>Ivory Bow</a> on <a class='card-name' data-card-id='100298' data-secured='false'>Carlton Van Wyk</a> in their ready region."));
    }

    @Test
    void moveCardLoop() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().getFirst().getCards().size(), is(2));
        assertThrows(CommandException.class, () -> worker.doCommand("Player3", "move ready 1 ready 1.1"));
    }

    @Test
    void moveComplicated() throws CommandException {
        assertThat(game.getData().getCard("208").getCards().size(), is(2));
        assertThat(game.getData().getCard("246").getCards().size(), is(0));
        worker.doCommand("Player3", "move ready 1.2 ready 1.1");
        assertThat(getLastMessage(), containsString("Player3 puts <a class='card-name' data-card-id='100199' data-secured='false'>Blood Doll</a> 1.2 on <a class='card-name' data-card-id='101014' data-secured='false'>Ivory Bow</a> in their ready region."));
        assertThat(game.getData().getCard("208").getCards().size(), is(1));
        assertThat(game.getData().getCard("246").getCards().size(), is(1));
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
        CardData card = game.getCard("111");
        assertThat(game.getCounters(card), is(6));
        worker.doCommand("Player2", "blood ready 1 +1");
        assertThat(game.getCounters(card), is(7));
        assertThat(getLastMessage(), containsString("Player2 adds 1 blood to <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>, now 7."));
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "blood ready 2"));
    }

    @Test
    void contest() throws CommandException {
        CardData card = game.getCard("111");
        assertThat(game.getContested(card), is(false));
        worker.doCommand("Player2", "contest ready 1");
        assertThat(game.getContested(card), is(true));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> is now contested."));
        worker.doCommand("Player2", "contest ready 1 clear");
        assertThat(game.getContested(card), is(false));
        assertThat(getLastMessage(), containsString("<a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> is no longer contested."));
    }

    @Test
    void disciplines() throws CommandException {
        CardData card = game.getCard("111");
        assertThat(game.getDisciplines(card), contains("aus", "dom", "OBT", "POT"));
        worker.doCommand("Player2", "disc ready 1 +ani");
        assertThat(game.getDisciplines(card), contains("ani", "aus", "dom", "OBT", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon ani'></span> to <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 -obt");
        assertThat(game.getDisciplines(card), contains("ani", "aus", "dom", "obt", "POT"));
        assertThat(getLastMessage(), containsString("Player2 removed <span class='icon obt'></span> to <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 +dom");
        assertThat(game.getDisciplines(card), contains("ani", "aus", "obt", "DOM", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon DOM'></span> to <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 +ani");
        assertThat(game.getDisciplines(card), contains("aus", "obt", "ANI", "DOM", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon ANI'></span> to <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 1 reset");
        assertThat(game.getDisciplines(card), contains("aus", "dom", "OBT", "POT"));
        assertThat(getLastMessage(), containsString("Player2 reset <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> back to <span class='icon aus'></span> <span class='icon dom'></span> <span class='icon OBT'></span> <span class='icon POT'></span>"));
        worker.doCommand("Player2", "disc ready 1 +ani +dom");
        assertThat(game.getDisciplines(card), contains("ani", "aus", "DOM", "OBT", "POT"));
        assertThat(getLastMessage(), containsString("Player2 added <span class='icon ani'></span> <span class='icon DOM'></span> to <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>."));
        worker.doCommand("Player2", "disc ready 2 reset");
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "disc ready 1 +blah"));
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "disc ready 1 aani"));
    }

    @Test
    void capacity() throws CommandException {
        CardData card = game.getCard("111");
        assertThat(card.getCapacity(), is(6));
        worker.doCommand("Player2", "capacity ready 1 +1");
        assertThat(card.getCapacity(), is(7));
        assertThat(getLastMessage(), containsString("Capacity of <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> now 7"));
        worker.doCommand("Player2", "capacity ready 1 -1");
        assertThat(getLastMessage(), containsString("Capacity of <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> now 6"));
        assertThrows(CommandException.class, () -> worker.doCommand("Player2", "capacity ready 2"));
        worker.doCommand("Player2", "capacity ready 1 -7");
        assertThat(getLastMessage(), containsString("Capacity of <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a> now 0"));
    }

    @Test
    void unlockAll() throws CommandException {
        CardData card1 = game.getCard("6");
        CardData card2 = game.getCard("11");
        CardData card3 = game.getCard("37");
        assertThat(card1.isLocked(), is(false));
        assertThat(card2.isLocked(), is(true));
        assertThat(card3.isLocked(), is(true));
        worker.doCommand("Player1", "unlock");
        assertThat(card1.isLocked(), is(false));
        assertThat(card2.isLocked(), is(false));
        assertThat(card3.isLocked(), is(false));
        assertThat(getLastMessage(), containsString("Player1 unlocks."));
    }

    @Test
    void unlock() throws CommandException {
        CardData card1 = game.getCard("6");
        CardData card2 = game.getCard("11");
        CardData card3 = game.getCard("37");
        assertThat(card1.isLocked(), is(false));
        assertThat(card2.isLocked(), is(true));
        assertThat(card3.isLocked(), is(true));
        worker.doCommand("Player1", "unlock ready 2");
        assertThat(card1.isLocked(), is(false));
        assertThat(card2.isLocked(), is(false));
        assertThat(card3.isLocked(), is(true));
        assertThat(getLastMessage(), containsString("Player1 unlocks <a class='card-name' data-card-id='201039' data-secured='false'>Navar McClaren</a>."));
    }

    @Test
    void lock() throws CommandException {
        CardData card1 = game.getCard("6");
        CardData card2 = game.getCard("11");
        CardData card3 = game.getCard("37");
        assertThat(card1.isLocked(), is(false));
        assertThat(card2.isLocked(), is(true));
        assertThat(card3.isLocked(), is(true));
        worker.doCommand("Player1", "lock ready 1");
        assertThat(card1.isLocked(), is(true));
        assertThat(card2.isLocked(), is(true));
        assertThat(card3.isLocked(), is(true));
        assertThat(getLastMessage(), containsString("Player1 locks <a class='card-name' data-card-id='200519' data-secured='false'>Gillian Krader</a>."));
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
    @Disabled
    void showAll() throws CommandException {
        assertThat(game.getPrivateNotes("Player2"), is(""));
        worker.doCommand("Player3", "show library all");
        assertThat(game.getPrivateNotes("Player1"), containsString("81 cards of Player3's LIBRARY"));
        assertThat(game.getPrivateNotes("Player2"), containsString("81 cards of Player3's LIBRARY"));
        assertThat(game.getPrivateNotes("Player3"), containsString("81 cards of Player3's LIBRARY"));
        assertThat(game.getPrivateNotes("Player4"), containsString("81 cards of Player3's LIBRARY"));
        assertThat(game.getPrivateNotes("Player5"), containsString("81 cards of Player3's LIBRARY"));
        assertThat(getLastMessage(), containsString("Player3 shows everyone 81 cards of their Library."));
        worker.doCommand("Player3", "show hand all");
        assertThat(game.getPrivateNotes("Player2"), not(is("")));
        assertThat(getLastMessage(), containsString("Player3 shows everyone 7 cards of their Hand."));
    }

    @Test
    @Disabled
    void showPlayer() throws CommandException {
        assertThat(game.getPrivateNotes("Player4"), is(""));
        worker.doCommand("Player3", "show hand Player4 all");
        assertThat(game.getPrivateNotes("Player4"), not(is("")));
        assertThat(getLastMessage(), containsString("Player3 shows Player4 7 cards of their Hand."));
    }

    @Test
    @Disabled
    void showSelf() throws CommandException {
        assertThat(game.getPrivateNotes("Player3"), is(""));
        worker.doCommand("Player3", "show hand");
        assertThat(game.getPrivateNotes("Player3"), not(is("")));
        assertThat(getLastMessage(), containsString("Player3 looks at 7 cards of their Hand."));
    }

    @Test
    void shuffle() throws CommandException {
        List<String> cards = game.getData().getPlayerRegion("Player3", RegionType.HAND).getCards().stream().map(CardData::getId).toList();
        assertThat(cards, contains("283", "299", "235", "257", "252", "242", "264"));
        worker.doCommand("Player3", "shuffle hand");
        cards = game.getData().getPlayerRegion("Player3", RegionType.HAND).getCards().stream().map(CardData::getId).toList();
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
        CardData card = game.getCard("111");
        assertThat(game.getCounters(card), is(6));
        assertThat(game.getPool("Player2"), is(30));
        worker.doCommand("Player2", "transfer ready 1 +1");
        assertThat(game.getCounters(card), is(7));
        assertThat(game.getPool("Player2"), is(29));
        assertThat(getLastMessage(), containsString("Player2 transferred 1 blood onto <a class='card-name' data-card-id='201337' data-secured='false'>Talley, The Hound</a>. Currently: 7, Pool: 29"));
    }

    @Test
    void rfg() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().size(), is(1));
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.REMOVED_FROM_GAME).getCards().size(), is(0));
        worker.doCommand("Player5", "rfg Player3 ready 1");
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.READY).getCards().size(), is(0));
        assertThat(game.getData().getPlayerRegion("Player3", RegionType.REMOVED_FROM_GAME).getCards().size(), is(1));
        assertThat(getLastMessage(), containsString("Player5 removes <a class='card-name' data-card-id='200788' data-secured='false'>Klaus van der Veken</a> in Player3's ready region from the game."));
    }

    @Test
    void rfgRandom() throws CommandException {
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.ASH_HEAP).getCards().size(), is(4));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.REMOVED_FROM_GAME).getCards().size(), is(0));
        worker.doCommand("Player5", "rfg Player1 ash random");
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.ASH_HEAP).getCards().size(), is(3));
        assertThat(game.getData().getPlayerRegion("Player1", RegionType.REMOVED_FROM_GAME).getCards().size(), is(1));
        assertThat(getLastMessage(), containsString("Player5 removes"));
        assertThat(getLastMessage(), containsString("(picked randomly)"));
        assertThat(getLastMessage(), containsString("from the game."));
    }

    @Test
    void pathTest() throws CommandException {
        CardData card = game.getData().getCard("111");
        assertThat(card.getPath(), is(Path.NONE));
        worker.doCommand("Player2", "path 1 caine");
        assertThat(card.getPath(), is(Path.CAINE));
    }
}
