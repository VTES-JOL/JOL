package net.deckserver.dwr.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.enums.Clan;
import net.deckserver.game.enums.Phase;
import net.deckserver.game.enums.RegionType;
import net.deckserver.game.enums.Sect;
import net.deckserver.services.ChatService;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.PlayerData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class JolGameTest {

    private static final ObjectMapper objectmapper = new ObjectMapper();
    private static ExtendedDeck deck1;
    private static ExtendedDeck deck2;
    private static ExtendedDeck deck3;
    private static ExtendedDeck deck4;
    private JolGame existingGame;

    @BeforeAll
    static void init() {
        try {
            deck1 = objectmapper.readValue(Paths.get("src/test/resources/data/decks/deck1.json").toFile(), ExtendedDeck.class);
            deck2 = objectmapper.readValue(Paths.get("src/test/resources/data/decks/deck2.json").toFile(), ExtendedDeck.class);
            deck3 = objectmapper.readValue(Paths.get("src/test/resources/data/decks/deck3.json").toFile(), ExtendedDeck.class);
            deck4 = objectmapper.readValue(Paths.get("src/test/resources/data/decks/deck4.json").toFile(), ExtendedDeck.class);
        } catch (IOException e) {
            System.out.println("Error reading test decks");
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUpGame() {
        existingGame = ModelLoader.loadGame("command-test");
    }

    @Test
    void addPlayer() {
        // start new game
        JolGame newGame = new JolGame("new-game", new GameData());
        assertThat(newGame.getPlayers(), is(empty()));
        // add a new player
        newGame.addPlayer("Player1", deck1.getDeck());
        assertThat(newGame.getPlayers().size(), is(1));
        assertThat(newGame.data().getPlayer("Player1").getRegion(RegionType.CRYPT).getCards().size(), is(12));
        assertThat(newGame.data().getPlayer("Player1").getRegion(RegionType.LIBRARY).getCards().size(), is(75));
        // add another player
        newGame.addPlayer("Player2", deck2.getDeck());
        assertThat(newGame.getPlayers().size(), is(2));
        assertThat(newGame.data().getPlayer("Player2").getRegion(RegionType.CRYPT).getCards().size(), is(12));
        assertThat(newGame.data().getPlayer("Player2").getRegion(RegionType.LIBRARY).getCards().size(), is(90));
    }

    @Test
    void withdraw() {
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.0));
        assertThat(existingGame.getPool("Player1"), is(29));
        // withdraw
        existingGame.withdraw("Player1");
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.5));
        assertThat(existingGame.getPool("Player1"), is(0));
    }

    @Test
    void updateVP() {
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.0));
        existingGame.updateVP("Player1", 2.0f);
        assertThat(existingGame.getVictoryPoints("Player1"), is(2.0));
        existingGame.updateVP("Player1", -1.0f);
        assertThat(existingGame.getVictoryPoints("Player1"), is(1.0));
    }

    @Test
    void getVictoryPoints() {
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.0));
    }

    @Test
    void timeout() {
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.0));
        assertThat(existingGame.getPool("Player1"), is(29));
        assertThat(existingGame.getVictoryPoints("Player2"), is(0.0));
        assertThat(existingGame.getPool("Player2"), is(30));
        assertThat(existingGame.getVictoryPoints("Player3"), is(0.0));
        assertThat(existingGame.getPool("Player3"), is(30));
        assertThat(existingGame.getVictoryPoints("Player4"), is(0.0));
        assertThat(existingGame.getPool("Player4"), is(22));
        assertThat(existingGame.getVictoryPoints("Player5"), is(0.0));
        assertThat(existingGame.getPool("Player5"), is(23));
        existingGame.timeout();
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.5));
        assertThat(existingGame.getPool("Player1"), is(0));
        assertThat(existingGame.getVictoryPoints("Player2"), is(0.5));
        assertThat(existingGame.getPool("Player2"), is(0));
        assertThat(existingGame.getVictoryPoints("Player3"), is(0.5));
        assertThat(existingGame.getPool("Player3"), is(0));
        assertThat(existingGame.getVictoryPoints("Player4"), is(0.5));
        assertThat(existingGame.getPool("Player4"), is(0));
        assertThat(existingGame.getVictoryPoints("Player5"), is(0.5));
        assertThat(existingGame.getPool("Player5"), is(0));
    }

    @Test
    void requestTimeout() {
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.0));
        assertThat(existingGame.getPool("Player1"), is(29));
        assertNull(existingGame.data().getTimeoutRequestor());
        existingGame.requestTimeout("Player1");
        assertThat(existingGame.data().getTimeoutRequestor(), is("Player1"));
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.0));
        assertThat(existingGame.getPool("Player1"), is(29));
        existingGame.requestTimeout("Player1");
        assertThat(existingGame.data().getTimeoutRequestor(), is("Player1"));
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.0));
        assertThat(existingGame.getPool("Player1"), is(29));

        existingGame.requestTimeout("Player2");
        assertThat(existingGame.getVictoryPoints("Player1"), is(0.5));
        assertThat(existingGame.getPool("Player1"), is(0));
    }

    @Test
    void getName() {
        assertThat(existingGame.getName(), is("Test Game"));
    }

    @Test
    void getPlayers() {
        assertThat(existingGame.getPlayers(), contains("Player2", "Player4", "Player5", "Player3", "Player1"));
    }

    @Test
    void discard() {
        List<CardData> hand = getCards("Player2", RegionType.HAND);
        List<CardData> ashHeap = getCards("Player2", RegionType.ASH_HEAP);
        assertThat(toCardIds(hand), contains("141", "183", "173", "150", "199", "147", "162"));
        assertThat(toCardIds(ashHeap), not(hasItem("173")));
        assertThat(ashHeap.size(), is(0));
        assertThat(hand.size(), is(7));

        existingGame.discard("Player2", "173", false);
        hand = getCards("Player2", RegionType.HAND);
        ashHeap = getCards("Player2", RegionType.ASH_HEAP);
        assertThat(toCardIds(hand), contains("141", "183", "150", "199", "147", "162"));
        assertThat(toCardIds(ashHeap), hasItem("173"));
        assertThat(ashHeap.size(), is(1));
        assertThat(hand.size(), is(6));

        existingGame.discard("Player2", "199", true);
        hand = getCards("Player2", RegionType.HAND);
        ashHeap = getCards("Player2", RegionType.ASH_HEAP);
        assertThat(toCardIds(hand), contains("141", "183", "150", "147", "162"));
        assertThat(toCardIds(ashHeap), hasItem("199"));
        assertThat(ashHeap.size(), is(2));
        assertThat(hand.size(), is(5));
    }

    @Test
    void playCard() {
        List<CardData> hand = getCards("Player2", RegionType.HAND);
        List<CardData> ready = getCards("Player2", RegionType.READY);
        assertThat(toCardIds(hand), contains("141", "183", "173", "150", "199", "147", "162"));
        assertThat(toCardIds(ready), not(hasItem("173")));
        assertThat(ready.size(), is(2));
        assertThat(hand.size(), is(7));
        existingGame.playCard("Player2", "173", "Player2", RegionType.READY, null, new String[]{"DOM"});
        hand = getCards("Player2", RegionType.HAND);
        ready = getCards("Player2", RegionType.READY);
        assertThat(toCardIds(hand), containsInAnyOrder("141", "183", "147", "150", "199", "162"));
        assertThat(toCardIds(ready), hasItem("173"));
        assertThat(ready.size(), is(3));
        assertThat(hand.size(), is(6));
        assertFalse(existingGame.getCard("422").isContested());
        existingGame.playCard("Player5", "422", "Player5", RegionType.READY, null, null);
        assertTrue(existingGame.getCard("422").isContested());
        assertTrue(existingGame.getCard("424").isContested());
        existingGame.playCard("Player2", "150", "Player2", RegionType.RESEARCH, null, null);
        existingGame.playCard("Player2", "147", "Player2", RegionType.RESEARCH, "150", null);
        assertThat(existingGame.data().getPlayerRegion("Player2", RegionType.RESEARCH).size(), is(2));
    }

    @Test
    void moveToCard() throws CommandException {
        List<CardData> hand = getCards("Player2", RegionType.HAND);
        List<CardData> ready = getCards("Player2", RegionType.READY);
        assertThat(toCardIds(hand), contains("141", "183", "173", "150", "199", "147", "162"));
        existingGame.playCard("Player2", "147", "Player2", RegionType.RESEARCH, null, null);
        existingGame.moveToCard("Player2", "150", "147");
        assertThat(toCardIds(hand), contains("141", "183", "173", "199", "162"));
        existingGame.moveToRegion("Player2", "147", "Player3", RegionType.RESEARCH, false);
    }

    @Test
    void influenceCard() {
        List<CardData> uncontrolled = getCards("Player5", RegionType.UNCONTROLLED);
        assertThat(uncontrolled.size(), is(4));
        existingGame.drawCard("Player5", RegionType.CRYPT, RegionType.UNCONTROLLED);
        assertThat(uncontrolled.size(), is(5));
        assertThat(toCardIds(uncontrolled), hasItems("413", "412"));
        List<CardData> ready = getCards("Player5", RegionType.READY);
        assertThat(ready.size(), is(3));
        existingGame.influenceCard("Player5", "413");
        assertFalse(existingGame.getCard("413").isContested());
        assertThat(ready.size(), is(4));
        existingGame.influenceCard("Player5", "412");
        assertTrue(existingGame.getCard("413").isContested());
        assertTrue(existingGame.getCard("412").isContested());
        assertThat(ready.size(), is(5));
    }

    @Test
    void setSect() {
        assertThat(existingGame.getCard("6").getSect(), is(Sect.SABBAT));
        existingGame.setSect("6", Sect.of("independent"));
        assertThat(existingGame.getCard("6").getSect(), is(Sect.INDEPENDENT));
    }

    @Test
    void setPath() {
    }

    @Test
    void setClan() {
        assertThat(existingGame.getCard("6").getClan(), is(Clan.PANDER));
        existingGame.setClan("6", Clan.of("Brujah"));
        assertThat(existingGame.getCard("6").getClan(), is(Clan.BRUJAH));
    }

    @Test
    void shuffle() {
    }

    @Test
    void startGame() {
        // start new game
        JolGame newGame = new JolGame("new-game", new GameData());
        assertThat(newGame.getPlayers(), is(empty()));
        // add players
        newGame.addPlayer("Player1", deck1.getDeck());
        newGame.addPlayer("Player2", deck2.getDeck());
        newGame.addPlayer("Player3", deck3.getDeck());
        newGame.addPlayer("Player4", deck4.getDeck());
        assertThrows(IllegalArgumentException.class, () -> newGame.startGame(List.of("Player3")));
        assertThrows(IllegalArgumentException.class, () -> newGame.startGame(List.of("Player3", "Player1", "Player2", "New Player")));
        newGame.startGame(List.of("Player3", "Player4", "Player2", "Player1"));
        assertThat(newGame.getPlayers(), contains("Player3", "Player4", "Player2", "Player1"));
    }

    @Test
    void initGame() {
    }

    @Test
    void sendMsg() {
        existingGame.sendMsg("Player1", "Test message", false);
        existingGame.sendMsg("Judge", "Test message", true);
    }

    @Test
    void getCounters() {
    }

    @Test
    void getDisciplines() {
    }

    @Test
    void transfer() {
        assertThat(existingGame.getCard("6").getCounters(), is(2));
        assertThat(existingGame.getPool("Player5"), is(23));
        existingGame.transfer("Player5", "6", 2);
        assertThat(existingGame.getCard("6").getCounters(), is(4));
        assertThat(existingGame.getPool("Player5"), is(21));
        existingGame.transfer("Player5", "6", -2);
        assertThat(existingGame.getCard("6").getCounters(), is(2));
        assertThat(existingGame.getPool("Player5"), is(23));
    }

    @Test
    void changeCounters() {
        assertThat(existingGame.getCard("6").getCounters(), is(2));
        existingGame.changeCounters("Player5", "6", 1, false);
        assertThat(existingGame.getCard("6").getCounters(), is(3));
        existingGame.changeCounters("Player5", "6", -2, false);
        assertThat(existingGame.getCard("6").getCounters(), is(1));
        existingGame.changeCounters("Player5", "6", 0, false);
        assertThat(existingGame.getCard("6").getCounters(), is(1));
    }

    @Test
    void isVisible() {
    }

    @Test
    void getActivePlayer() {
    }

    @Test
    void getTurnLabel() {
        assertThat(existingGame.getTurnLabel(), is("Player2 1.1"));
        existingGame.newTurn();
        assertThat(existingGame.getTurnLabel(), is("Player4 1.2"));
    }

    @Test
    void getCard() {
        CardData card = existingGame.getCard("425");
        card.setNotes("Test notes");
        existingGame.burn("Player5", "425", "Player5", RegionType.READY, false);
    }

    @Test
    void hydrateCard() {
    }

    @Test
    void getPredatorOf() {
        assertThat(existingGame.getPredatorOf("Player2"), is("Player1"));
        JolGame newGame = new JolGame("test", new GameData("test", "Test Game"));
        newGame.addPlayer("Player1", deck1.getDeck());
        assertNull(newGame.getPredatorOf("Player1"));
    }

    @Test
    void getPreyOf() {
        assertThat(existingGame.getPreyOf("Player2"), is("Player4"));
        JolGame newGame = new JolGame("test", new GameData("test", "Test Game"));
        newGame.addPlayer("Player1", deck1.getDeck());
        assertNull(newGame.getPreyOf("Player1"));
    }

    @Test
    void getSize() {
        assertThat(existingGame.getSize("Player3", RegionType.LIBRARY), is(81));
        assertThat(getCards("Player3", RegionType.LIBRARY).size(), is(81));
    }

    @Test
    void getEdge() {
    }

    @Test
    void setEdge() {
    }

    @Test
    void burnEdge() {
    }

    @Test
    void getPool() {
    }

    @Test
    void changePool() {
        assertThat(existingGame.getPool("Player1"), is(29));
        assertThat(existingGame.getValidPlayers().size(), is(5));
        existingGame.changePool("Player1", "Player1", -29);
        assertThat(existingGame.getPool("Player1"), is(0));
        assertThat(existingGame.getValidPlayers().size(), is(4));
    }

    @Test
    void getGlobalText() {
        assertThat(existingGame.getGlobalText(), is(""));
    }

    @Test
    void setGlobalText() {
        assertThat(existingGame.getGlobalText(), is(""));
        existingGame.setGlobalText("New text");
        assertThat(existingGame.getGlobalText(), is("New text"));
    }

    @Test
    void getPrivateNotes() {
        assertThat(existingGame.getPrivateNotes("Player2"), is(""));
    }

    @Test
    void setPrivateNotes() {
        assertThat(existingGame.getPrivateNotes("Player2"), is(""));
        existingGame.setPrivateNotes("Player2", "test notes");
        assertThat(existingGame.getPrivateNotes("Player2"), is("test notes"));
    }

    @Test
    void getLabel() {
        assertNull(existingGame.getCard("6").getNotes());
    }

    @Test
    void setLabel() {
        assertNull(existingGame.getCard("6").getNotes());
        existingGame.setLabel("Player5", "6", "corruption: 1", false);
        assertThat(existingGame.getCard("6").getNotes(), is("corruption: 1"));
        existingGame.setLabel("Player5", "6", "corruption: 2", true);
        assertThat(existingGame.getCard("6").getNotes(), is("corruption: 2"));
    }

    @Test
    void getVotes() {
    }

    @Test
    void random() {
    }

    @Test
    void flip() {
    }

    @Test
    void setVotes() {
    }

    @Test
    void contestCard() {
    }

    @Test
    void getContested() {
    }

    @Test
    void setLocked() {
    }

    @Test
    void unlockAll() {
        assertThat(getCards("Player1", RegionType.READY).stream().map(CardData::isLocked).toList(), contains(false, true, true));
        existingGame.getCard("37").setInfernal(true);
        existingGame.unlockAll("Player1");
        assertThat(getCards("Player1", RegionType.READY).stream().map(CardData::isLocked).toList(), contains(false, false, true));
        existingGame.burn("Player1", "37", "Player1", RegionType.READY, false);
        existingGame.unlockAll("Player1");
        assertThat(getCards("Player1", RegionType.ASH_HEAP).stream().map(CardData::isLocked).toList(), contains(false, false, false, false, false));
    }

    @Test
    void getCurrentTurn() {
        assertThat(existingGame.getCurrentTurn(), is("1.1"));
    }

    @Test
    void newTurn() {
        assertThat(existingGame.getCurrentTurn(), is("1.1"));
        existingGame.newTurn();
        assertThat(existingGame.getCurrentTurn(), is("1.2"));
        assertThat(existingGame.getPhase(), is(Phase.UNLOCK));
        existingGame.newTurn();
        assertThat(existingGame.getCurrentTurn(), is("1.3"));
        existingGame.newTurn();
        assertThat(existingGame.getCurrentTurn(), is("1.4"));
        existingGame.newTurn();
        assertThat(existingGame.getCurrentTurn(), is("1.5"));
        existingGame.newTurn();
        assertThat(existingGame.getCurrentTurn(), is("2.1"));
    }

    @Test
    void getPhase() {
        assertThat(existingGame.getPhase(), is(Phase.UNLOCK));
    }

    @Test
    void setPhase() {
        assertThat(existingGame.getPhase(), is(Phase.UNLOCK));
        existingGame.setPhase(Phase.INFLUENCE);
        assertThat(existingGame.getPhase(), is(Phase.INFLUENCE));
    }

    @Test
    void changeCapacity() {
        assertThat(existingGame.getCard("6").getCapacity(), is(2));
        existingGame.changeCapacity("Player5", "6", 2, true);
        assertThat(existingGame.getCard("6").getCapacity(), is(4));
        existingGame.changeCapacity("Player5", "6", -1, false);
        assertThat(existingGame.getCard("6").getCapacity(), is(3));
    }

    @Test
    void resetDisciplines() {
        assertThat(existingGame.getCard("415").getDisciplines(), containsInAnyOrder("obt", "pre", "CEL", "OBF", "QUI"));
        existingGame.setDisciplines("Player5", "415", List.of("dom", "FOR"), true);
        assertThat(existingGame.getCard("415").getDisciplines(), containsInAnyOrder("FOR", "dom"));
        existingGame.setDisciplines("Player5", "415", List.of("CEL", "OBF", "QUI", "obt", "pre"), false);
        assertThat(existingGame.getCard("415").getDisciplines(), containsInAnyOrder("obt", "pre", "CEL", "OBF", "QUI"));
    }

    @Test
    void setDisciplines() throws CommandException{
        assertThat(existingGame.getCard("415").getDisciplines(), containsInAnyOrder("obt", "pre", "CEL", "OBF", "QUI"));
        existingGame.setDisciplines("Player5", "415", Set.of("obt", "AUS"), Set.of("qui", "OBF"));
        assertThat(existingGame.getCard("415").getDisciplines(), containsInAnyOrder("OBT", "pre", "CEL", "qui", "AUS"));
        assertThrows(CommandException.class, () -> existingGame.setDisciplines("Player5", "415", Collections.emptySet(), Collections.emptySet()));
    }

    @Test
    void replacePlayer() {
        assertThat(existingGame.getPlayers(), contains("Player2", "Player4", "Player5", "Player3", "Player1"));
        assertThat(existingGame.data().getPlayers().keySet(), containsInAnyOrder("Player2", "Player4", "Player5", "Player3", "Player1"));
        assertThat(existingGame.getActivePlayer(), is("Player2"));
        existingGame.replacePlayer("Player2", "New Player");
        assertThat(existingGame.data().getPlayers().keySet(), not(hasItem("Player2")));
        assertThat(existingGame.data().getPlayers().keySet(), containsInAnyOrder("New Player", "Player4", "Player5", "Player3", "Player1"));
        assertThat(existingGame.getPlayers(), contains("New Player", "Player4", "Player5", "Player3", "Player1"));
        assertThat(existingGame.getActivePlayer(), is("New Player"));
    }

    @Test
    void setChoice() {
    }

    @Test
    void getChoices() {
    }

    @Test
    void setOrder() {
    }

    @Test
    void id() {
    }

    private List<String> toCardIds(List<CardData> cards) {
        return cards.stream().map(CardData::getId).toList();
    }

    private List<CardData> getCards(String player, RegionType regionType) {
        return existingGame.data().getPlayerRegion(player, regionType).getCards();
    }
}