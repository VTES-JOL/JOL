package net.deckserver.game.model;

import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.game.cards.CryptCard;
import net.deckserver.game.enums.RegionType;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.ExitData;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coverage for every command {@link DoCommand#doCommand} dispatches on — one
 * (or a small group of) test(s) per {@code case} in its switch, checking both
 * the happy path (state changes the way the command's own log message claims)
 * and the documented failure path (a {@link CommandException} with a sane
 * cause, not a crash or a silent no-op where the source clearly means to
 * reject the input).
 *
 * <p>Fixture: a bare two-player game (no decks, no {@code startGame}) built
 * directly off {@code GameData}/{@code PlayerData}/{@code CardData} — enough
 * for {@code JolGame}'s mutators without the weight of a real deck/JPA/CDI
 * setup. {@code ENABLE_TEST_MODE} keeps every {@code ChatService} call (every
 * command logs through {@code JolGame.log}) a harmless in-memory no-op.
 */
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class DoCommandTest {

    private GameData data;
    private JolGame game;
    private DoCommand doCommand;
    private PlayerData alice;
    private PlayerData bob;

    /** Builds a fresh N-player game (seated in the given order) and returns it, without touching the shared fixture fields. */
    private static JolGame newGame(String gameId, String... playerNames) {
        GameData gameData = new GameData(gameId, "Test Game");
        for (String name : playerNames) {
            gameData.addPlayer(new PlayerData(name));
        }
        JolGame jolGame = new JolGame(gameId, gameData);
        gameData.setCurrentPlayer(gameData.getPlayer(playerNames[0]));
        return jolGame;
    }

    private static DoCommand commander(JolGame jolGame) {
        return new DoCommand(jolGame, new GameModel(jolGame.getName(), jolGame));
    }

    @BeforeEach
    void setUp() {
        game = newGame("g1", "Alice", "Bob");
        data = game.data();
        alice = data.getPlayer("Alice");
        bob = data.getPlayer("Bob");
        doCommand = commander(game);
    }

    /** Adds a freshly-created card straight into a region and registers it on the game, mirroring what deck setup would do. */
    private CardData addCard(PlayerData owner, RegionType region, String cardId, String name) {
        CardData card = new CardData(cardId, owner);
        card.setName(name);
        owner.getRegion(region).addCard(card, false);
        data.getCards().put(card.getId(), card);
        return card;
    }

    private CardData readyCard(PlayerData owner, String name) {
        return addCard(owner, RegionType.READY, "900001", name);
    }

    private void exec(String player, String command) throws CommandException {
        doCommand.doCommand(player, command);
    }

    // ── timeout ──────────────────────────────────────────────────────────

    @Test
    void timeout_secondDifferentPlayerConfirmsAndEndsGame() throws CommandException {
        exec("Alice", "timeout");
        assertFalse(game.isOusted("Alice"), "first request alone shouldn't end the game");

        exec("Bob", "timeout");

        assertTrue(game.isOusted("Alice"));
        assertTrue(game.isOusted("Bob"));
        assertEquals(0, game.getPool("Alice"));
        assertEquals(0.5, game.getVictoryPoints("Alice"), 0.001);
    }

    @Test
    void timeout_sameRequestorTwiceDoesNotConfirm() throws CommandException {
        exec("Alice", "timeout");
        exec("Alice", "timeout");
        assertFalse(game.isOusted("Alice"));
    }

    // ── vp ───────────────────────────────────────────────────────────────

    @Test
    void vp_selfAndOtherPlayerAndWithdraw() throws CommandException {
        exec("Alice", "vp +1");
        assertEquals(1.0, game.getVictoryPoints("Alice"), 0.001);

        exec("Alice", "vp bob +2");
        assertEquals(2.0, game.getVictoryPoints("Bob"), 0.001);

        exec("Alice", "vp withdraw");
        assertEquals(0, game.getPool("Alice"));
        // Withdrawal awards a full 1 VP (game-flow.md:167) — Alice had 1.0, now 2.0.
        assertEquals(2.0, game.getVictoryPoints("Alice"), 0.001);

        // Persisted exit record: a withdrawal credits the withdrawer themselves.
        ExitData exit = data.getExit("Alice");
        assertNotNull(exit);
        assertEquals(ExitData.Kind.WITHDRAW, exit.getKind());
        assertEquals("Alice", exit.getVpRecipient());
    }

    @Test
    void vp_noAmountThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "vp"));
    }

    // ── choose / tally ───────────────────────────────────────────────────

    @Test
    void choose_setsPlayerChoice() throws CommandException {
        exec("Alice", "choose heads");
        assertEquals("heads", alice.getChoice());
    }

    @Test
    void choose_noArgThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "choose"));
    }

    @Test
    void tally_neverThrows() {
        assertDoesNotThrow(() -> exec("Alice", "tally"));
    }

    // ── reveal / hide ────────────────────────────────────────────────────

    @Test
    void reveal_flipsFaceDownCardUp() throws CommandException {
        CardData card = readyCard(alice, "Hidden Vampire");
        card.setFaceDown(true);

        exec("Alice", "reveal alice ready 1");

        assertFalse(card.isFaceDown());
    }

    @Test
    void reveal_noArgsThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "reveal"));
    }

    @Test
    void hide_flipsFaceUpCardDown() throws CommandException {
        CardData card = readyCard(alice, "Visible Vampire");

        exec("Alice", "hide alice ready 1");

        assertTrue(card.isFaceDown());
    }

    @Test
    void hide_nonControllerThrows() {
        readyCard(bob, "Bob's Vampire");
        assertThrows(CommandException.class, () -> exec("Alice", "hide bob ready 1"));
    }

    @Test
    void hide_alreadyInTargetStateThrows() throws CommandException {
        readyCard(alice, "Already Hidden");
        exec("Alice", "hide alice ready 1");
        assertThrows(CommandException.class, () -> exec("Alice", "hide alice ready 1"));
    }

    // ── label ────────────────────────────────────────────────────────────

    @Test
    void label_setsAndClearsNote() throws CommandException {
        CardData card = readyCard(alice, "Labeled");

        exec("Alice", "label alice ready 1 Test Label");
        assertEquals("Test Label", card.getNotes());

        exec("Alice", "label alice ready 1");
        assertEquals("", card.getNotes());
    }

    // ── votes ────────────────────────────────────────────────────────────

    @Test
    void votes_numericAndPriscus() throws CommandException {
        CardData card = readyCard(alice, "Voter");

        exec("Alice", "votes alice ready 1 3");
        assertEquals("3", card.getVotes());

        exec("Alice", "votes alice ready 1 priscus");
        assertEquals("P", card.getVotes());

        exec("Alice", "votes alice ready 1 bogus");
        assertEquals("0", card.getVotes(), "non-numeric, non-priscus text should reset votes to 0");
    }

    @Test
    void votes_missingValueThrows() {
        readyCard(alice, "Voter");
        assertThrows(CommandException.class, () -> exec("Alice", "votes alice ready 1"));
    }

    // ── random / flip ────────────────────────────────────────────────────

    @Test
    void random_and_flip_neverThrow() {
        assertDoesNotThrow(() -> exec("Alice", "random 6"));
        assertDoesNotThrow(() -> exec("Alice", "flip"));
    }

    // ── discard ──────────────────────────────────────────────────────────

    @Test
    void discard_movesHandCardToAshHeap() throws CommandException {
        CardData card = addCard(alice, RegionType.HAND, "900002", "Hand Card");

        exec("Alice", "discard 1");

        assertEquals(RegionType.ASH_HEAP, card.getRegion().getType());
    }

    @Test
    void discard_withDrawReplacesFromLibrary() throws CommandException {
        addCard(alice, RegionType.HAND, "900002", "Hand Card");
        CardData libCard = addCard(alice, RegionType.LIBRARY, "900003", "Library Card");

        exec("Alice", "discard 1 draw");

        assertEquals(RegionType.HAND, libCard.getRegion().getType());
    }

    @Test
    void discard_missingPositionThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "discard"));
    }

    // ── draw ─────────────────────────────────────────────────────────────

    @Test
    void draw_libraryAndCrypt() throws CommandException {
        CardData lib1 = addCard(alice, RegionType.LIBRARY, "900002", "L1");
        CardData lib2 = addCard(alice, RegionType.LIBRARY, "900003", "L2");
        CardData crypt1 = addCard(alice, RegionType.CRYPT, "900004", "C1");

        exec("Alice", "draw 2");
        assertEquals(RegionType.HAND, lib1.getRegion().getType());
        assertEquals(RegionType.HAND, lib2.getRegion().getType());

        exec("Alice", "draw crypt 1");
        assertEquals(RegionType.UNCONTROLLED, crypt1.getRegion().getType());
    }

    @Test
    void draw_zeroOrNegativeThrows() {
        addCard(alice, RegionType.LIBRARY, "900002", "L1");
        assertThrows(CommandException.class, () -> exec("Alice", "draw 0"));
    }

    @Test
    void draw_moreThanAvailableThrows() {
        addCard(alice, RegionType.LIBRARY, "900002", "L1");
        assertThrows(CommandException.class, () -> exec("Alice", "draw 5"));
    }

    // ── edge ─────────────────────────────────────────────────────────────

    @Test
    void edge_takeAndBurn() throws CommandException {
        exec("Alice", "edge alice");
        assertEquals("Alice", game.getEdge());

        exec("Alice", "edge burn");
        assertEquals("no one", game.getEdge());
    }

    // ── play ─────────────────────────────────────────────────────────────

    @Test
    void play_vampKeywordRejected() {
        assertThrows(CommandException.class, () -> exec("Alice", "play vamp"));
    }

    @Test
    void play_defaultsToAshHeap() throws CommandException {
        CardData card = addCard(alice, RegionType.HAND, "900002", "Some Card");

        exec("Alice", "play 1");

        assertEquals(RegionType.ASH_HEAP, card.getRegion().getType());
    }

    @Test
    void play_ontoTargetCardNestsIt() throws CommandException {
        CardData minion = readyCard(alice, "Target Minion");
        CardData card = addCard(alice, RegionType.HAND, "900002", "Targeted Card");

        exec("Alice", "play 1 alice ready 1");

        assertSame(minion, card.getParent());
    }

    @Test
    void play_faceDownAndDrawFlags() throws CommandException {
        CardData card = addCard(alice, RegionType.HAND, "900002", "FD Card");
        CardData replacement = addCard(alice, RegionType.LIBRARY, "900003", "Replacement");

        exec("Alice", "play 1 facedown draw");

        assertTrue(card.isFaceDown());
        assertEquals(RegionType.HAND, replacement.getRegion().getType());
    }

    // ── influence ────────────────────────────────────────────────────────

    @Test
    void influence_movesUncontrolledToReady() throws CommandException {
        CardData card = addCard(alice, RegionType.UNCONTROLLED, "900002", "Uninfluenced Vamp");

        exec("Alice", "influence 1");

        assertEquals(RegionType.READY, card.getRegion().getType());
    }

    // ── move ─────────────────────────────────────────────────────────────

    @Test
    void move_toRegionWithNoTargetCard() throws CommandException {
        CardData card = readyCard(alice, "Mover");

        exec("Alice", "move 1 alice torpor");

        assertEquals(RegionType.TORPOR, card.getRegion().getType());
    }

    @Test
    void move_ontoAnotherCardNestsIt() throws CommandException {
        CardData source = readyCard(alice, "Mover2");
        CardData target = readyCard(alice, "Target2");

        exec("Alice", "move 1 alice ready 2");

        assertSame(target, source.getParent());
    }

    @Test
    void move_predatorAndPreyFlags() throws CommandException {
        JolGame threePlayerGame = newGame("g2", "Alice", "Bob", "Carol");
        DoCommand threePlayerCommand = commander(threePlayerGame);
        GameData threePlayerData = threePlayerGame.data();
        PlayerData bobData = threePlayerData.getPlayer("Bob");
        CardData toPredator = addCardTo(threePlayerData, bobData, RegionType.READY, "Bob's card 1");
        CardData toPrey = addCardTo(threePlayerData, bobData, RegionType.READY, "Bob's card 2");

        assertEquals("Alice", threePlayerGame.getPredatorOf("Bob"));
        assertEquals("Carol", threePlayerGame.getPreyOf("Bob"));

        threePlayerCommand.doCommand("Bob", "move 1 predator torpor");
        assertEquals("Alice", toPredator.getRegion().getOwner());
        assertEquals(RegionType.TORPOR, toPredator.getRegion().getType());

        threePlayerCommand.doCommand("Bob", "move 1 prey torpor");
        assertEquals("Carol", toPrey.getRegion().getOwner());
    }

    private CardData addCardTo(GameData gameData, PlayerData owner, RegionType region, String name) {
        CardData card = new CardData("900001", owner);
        card.setName(name);
        owner.getRegion(region).addCard(card, false);
        gameData.getCards().put(card.getId(), card);
        return card;
    }

    @Test
    void move_invalidSourcePositionThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "move 99 alice torpor"));
    }

    @Test
    void move_cardOntoItselfThrows() {
        readyCard(alice, "Self Mover");
        // position 1 as both source and destination target
        assertThrows(CommandException.class, () -> exec("Alice", "move 1 alice ready 1"));
    }

    // ── burn ─────────────────────────────────────────────────────────────

    @Test
    void burn_movesToAshHeapAndClearsState() throws CommandException {
        CardData card = readyCard(alice, "Burn Me");
        card.setFaceDown(true);
        card.setLocked(true);
        card.setCounters(3);

        exec("Alice", "burn 1");

        assertEquals(RegionType.ASH_HEAP, card.getRegion().getType());
        assertFalse(card.isFaceDown());
        assertFalse(card.isLocked());
        assertEquals(0, card.getCounters());
    }

    @Test
    void burn_invalidPositionThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "burn 99"));
    }

    // ── banish ───────────────────────────────────────────────────────────

    @Test
    void banish_movesReadyCardToUncontrolled() throws CommandException {
        CardData card = readyCard(alice, "Banished");

        exec("Alice", "banish 1");

        assertEquals(RegionType.UNCONTROLLED, card.getRegion().getType());
        assertEquals("Alice", card.getRegion().getOwner());
    }

    @Test
    void banish_invalidPositionThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "banish 99"));
    }

    // ── pool ─────────────────────────────────────────────────────────────

    @Test
    void pool_selfAndOtherPlayerAndOusting() throws CommandException {
        exec("Alice", "pool +5");
        assertEquals(35, game.getPool("Alice"));

        exec("Alice", "pool bob -10");
        assertEquals(20, game.getPool("Bob"));

        exec("Alice", "pool bob -30");
        assertTrue(game.isOusted("Bob"));

        // Persisted exit record: an oust credits the ousted seat's predator at
        // the moment of the oust (heads-up → the other player).
        ExitData exit = data.getExit("Bob");
        assertNotNull(exit);
        assertEquals(ExitData.Kind.OUST, exit.getKind());
        assertEquals("Alice", exit.getVpRecipient());
    }

    @Test
    void pool_oustExitRecordCreditsPredatorNotTheDamageDealer() throws CommandException {
        JolGame threeWay = newGame("g3", "Alice", "Bob", "Carol");
        DoCommand commander = commander(threeWay);
        // Ring is Alice → Bob → Carol → Alice, so Carol's predator is Bob.
        // Alice deals the lethal pool loss but the VP belongs to the position (Bob).
        commander.doCommand("Alice", "pool carol -100");

        assertTrue(threeWay.isOusted("Carol"));
        ExitData exit = threeWay.data().getExit("Carol");
        assertNotNull(exit);
        assertEquals(ExitData.Kind.OUST, exit.getKind());
        assertEquals("Bob", exit.getVpRecipient());
    }

    @Test
    void pool_restoringAnOustedSeatClearsItsExitRecord() throws CommandException {
        exec("Alice", "pool bob -100");
        assertNotNull(data.getExit("Bob"));

        // Bob's pool is now -70; needs to end strictly above 0 to come back in.
        exec("Alice", "pool bob +100");
        assertFalse(game.isOusted("Bob"));
        assertNull(data.getExit("Bob"), "a seat back in the game must not keep an exit record");
    }

    @Test
    void pool_zeroAmountThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "pool"));
    }

    // ── declare / pass / resolve (response window, rules R1) ──────────────

    @Test
    void declare_bleed_opensWindowAwaitingThePrey() throws CommandException {
        exec("Alice", "declare bleed");

        var pa = data.getPendingAction();
        assertNotNull(pa);
        assertEquals(net.deckserver.storage.json.game.PendingActionData.Type.BLEED, pa.getType());
        assertEquals("Alice", pa.getActor());
        assertEquals("Bob", pa.getTargetPlayer(), "a bleed with no explicit target defaults to the actor's prey");
        assertEquals(List.of("Bob"), pa.getAwaiting());
        assertTrue(pa.getPassed().isEmpty());
    }

    @Test
    void declare_hunt_isInformationalOnly() throws CommandException {
        exec("Alice", "declare hunt");
        var pa = data.getPendingAction();
        assertNotNull(pa);
        assertTrue(pa.getAwaiting().isEmpty(), "hunt / go-anarch / leave-torpor open a banner but nobody owes a response");
    }

    @Test
    void pass_movesResponderFromAwaitingToPassed() throws CommandException {
        exec("Alice", "declare bleed");
        exec("Bob", "pass");

        var pa = data.getPendingAction();
        assertNotNull(pa);
        assertTrue(pa.getAwaiting().isEmpty());
        assertEquals(List.of("Bob"), pa.getPassed());
    }

    @Test
    void resolve_byActorClearsTheWindow_othersCannot() throws CommandException {
        exec("Alice", "declare bleed");
        exec("Bob", "resolve");
        assertNotNull(data.getPendingAction(), "only the actor may resolve their own window");

        exec("Alice", "resolve");
        assertNull(data.getPendingAction());
    }

    @Test
    void declare_secondDeclareAutoResolvesTheFirst() throws CommandException {
        exec("Alice", "declare bleed");
        String firstId = data.getPendingAction().getId();
        exec("Alice", "declare hunt");

        var pa = data.getPendingAction();
        assertNotNull(pa);
        assertNotEquals(firstId, pa.getId(), "a table never has two open windows");
        assertEquals(net.deckserver.storage.json.game.PendingActionData.Type.HUNT, pa.getType());
    }

    @Test
    void autoResolvePending_clearsTheWindowOnTurnEnd() throws CommandException {
        exec("Alice", "declare bleed");
        game.autoResolvePending("turn end");
        assertNull(data.getPendingAction());
    }

    // ── blood ────────────────────────────────────────────────────────────

    @Test
    void blood_addAndClampAtZero() throws CommandException {
        CardData card = readyCard(alice, "Bloodied");

        exec("Alice", "blood alice ready 1 +2");
        assertEquals(2, card.getCounters());

        exec("Alice", "blood alice ready 1 -5");
        assertEquals(0, card.getCounters(), "removing more blood than present must clamp at 0, not go negative");
    }

    @Test
    void blood_zeroAmountThrows() {
        readyCard(alice, "Bloodied");
        assertThrows(CommandException.class, () -> exec("Alice", "blood alice ready 1 +0"));
    }

    /**
     * Regression test: {@code blood} now uses the greedy {@code findCardData}
     * overload, like every sibling command (capacity/disc/sect/…), so a
     * missing or out-of-range position rejects with a CommandException
     * instead of leaving {@code targetCard} null for {@code changeCounters}
     * to NPE on.
     */
    @Test
    void blood_missingOrInvalidPositionThrows() {
        readyCard(alice, "Bloodied");
        assertThrows(CommandException.class, () -> exec("Alice", "blood alice ready +1"));
        assertThrows(CommandException.class, () -> exec("Alice", "blood alice ready 99 +1"));
    }

    // ── contest ──────────────────────────────────────────────────────────

    @Test
    void contest_marksAndClears() throws CommandException {
        CardData card = readyCard(alice, "Contested");

        exec("Alice", "contest alice ready 1");
        assertTrue(card.isContested());

        exec("Alice", "contest alice ready 1 clear");
        assertFalse(card.isContested());
    }

    // ── disc ─────────────────────────────────────────────────────────────

    @Test
    void disc_resetToCardDefinition() throws CommandException {
        CardData card = addCard(alice, RegionType.READY, "200001", "Aabbt Kindred");

        exec("Alice", "disc alice ready 1 reset");

        Card definition = CardRegistry.findById("200001");
        List<String> expected = ((CryptCard) definition).disciplines();
        assertEquals(expected, card.getDisciplines());
    }

    @Test
    void disc_addAndRemove() throws CommandException {
        CardData card = readyCard(alice, "Disc Vamp");

        exec("Alice", "disc alice ready 1 +cel");
        assertTrue(card.getDisciplines().stream().anyMatch("cel"::equalsIgnoreCase));

        exec("Alice", "disc alice ready 1 -cel");
        assertTrue(card.getDisciplines().stream().noneMatch("cel"::equalsIgnoreCase));
    }

    @Test
    void disc_unknownDisciplineThrows() {
        readyCard(alice, "Disc Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "disc alice ready 1 +bogus"));
    }

    @Test
    void disc_missingPrefixThrows() {
        readyCard(alice, "Disc Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "disc alice ready 1 xcel"));
    }

    // ── capacity ─────────────────────────────────────────────────────────

    @Test
    void capacity_raiseAndClampAtZero() throws CommandException {
        CardData card = readyCard(alice, "Cap Vamp");
        card.setCapacity(5);

        exec("Alice", "capacity alice ready 1 +2");
        assertEquals(7, card.getCapacity());

        exec("Alice", "capacity alice ready 1 -100");
        assertEquals(0, card.getCapacity());
    }

    @Test
    void capacity_zeroAmountThrows() {
        readyCard(alice, "Cap Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "capacity alice ready 1 +0"));
    }

    // ── lock / unlock ────────────────────────────────────────────────────

    @Test
    void lock_thenAlreadyLockedThrows() throws CommandException {
        CardData card = readyCard(alice, "Locker");

        exec("Alice", "lock alice ready 1");
        assertTrue(card.isLocked());

        assertThrows(CommandException.class, () -> exec("Alice", "lock alice ready 1"));
    }

    @Test
    void unlock_singleCard() throws CommandException {
        CardData card = readyCard(alice, "Locker");
        card.setLocked(true);

        exec("Alice", "unlock alice ready 1");

        assertFalse(card.isLocked());
    }

    @Test
    void unlock_allCardsForPlayer() throws CommandException {
        CardData card1 = readyCard(alice, "Locker1");
        CardData card2 = addCard(alice, RegionType.TORPOR, "900002", "Locker2");
        card1.setLocked(true);
        card2.setLocked(true);

        exec("Alice", "unlock alice");

        assertFalse(card1.isLocked());
        assertFalse(card2.isLocked());
    }

    // ── order ────────────────────────────────────────────────────────────

    @Test
    void order_reversesSeating() throws CommandException {
        exec("Alice", "order 2 1");
        assertEquals(List.of("Bob", "Alice"), game.getPlayers());
    }

    @Test
    void order_outOfRangeIndexThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "order 5 1"));
    }

    // ── show ─────────────────────────────────────────────────────────────

    @Test
    void show_defaultsToSelf() throws CommandException {
        addCard(alice, RegionType.LIBRARY, "900002", "L1");

        exec("Alice", "show library 1");

        assertNotNull(alice.getNotes());
        assertTrue(alice.getNotes().contains("L1"));
        assertNull(bob.getNotes());
    }

    @Test
    void show_allSendsToEveryPlayer() throws CommandException {
        addCard(alice, RegionType.LIBRARY, "900002", "L1");

        exec("Alice", "show library 1 all");

        assertTrue(bob.getNotes().contains("L1"));
    }

    // ── shuffle ──────────────────────────────────────────────────────────

    @Test
    void shuffle_keepsSameCards() throws CommandException {
        addCard(alice, RegionType.LIBRARY, "900002", "L1");
        addCard(alice, RegionType.LIBRARY, "900003", "L2");

        assertDoesNotThrow(() -> exec("Alice", "shuffle library"));

        assertEquals(2, alice.getRegion(RegionType.LIBRARY).getCards().size());
    }

    // ── transfer ─────────────────────────────────────────────────────────

    @Test
    void transfer_ontoAndOffCard() throws CommandException {
        CardData card = readyCard(alice, "Xfer Vamp");

        exec("Alice", "transfer ready 1 +3");
        assertEquals(3, card.getCounters());
        assertEquals(27, game.getPool("Alice"));

        exec("Alice", "transfer ready 1 -1");
        assertEquals(2, card.getCounters());
        assertEquals(28, game.getPool("Alice"));
    }

    @Test
    void transfer_zeroAmountThrows() {
        readyCard(alice, "Xfer Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "transfer ready 1 +0"));
    }

    @Test
    void transfer_moreThanPoolThrows() {
        readyCard(alice, "Xfer Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "transfer ready 1 +1000"));
    }

    @Test
    void transfer_moreThanCardCountersThrows() {
        readyCard(alice, "Xfer Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "transfer ready 1 -5"));
    }

    // ── rfg ──────────────────────────────────────────────────────────────

    @Test
    void rfg_movesFromAshHeap() throws CommandException {
        CardData card = addCard(alice, RegionType.ASH_HEAP, "900002", "Rfg Me");

        exec("Alice", "rfg 1");

        assertEquals(RegionType.REMOVED_FROM_GAME, card.getRegion().getType());
    }

    @Test
    void rfg_invalidPositionThrows() {
        assertThrows(CommandException.class, () -> exec("Alice", "rfg 99"));
    }

    // ── path / sect / clan ───────────────────────────────────────────────

    @Test
    void path_setsAndClears() throws CommandException {
        CardData card = readyCard(alice, "Path Vamp");

        exec("Alice", "path alice ready 1 caine");
        assertEquals(net.deckserver.game.enums.Path.CAINE, card.getPath());

        exec("Alice", "path alice ready 1");
        assertEquals(net.deckserver.game.enums.Path.NONE, card.getPath());
    }

    @Test
    void path_invalidNameThrows() {
        readyCard(alice, "Path Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "path alice ready 1 zzz"));
    }

    @Test
    void sect_setsAndClears() throws CommandException {
        CardData card = readyCard(alice, "Sect Vamp");

        exec("Alice", "sect alice ready 1 camarilla");
        assertEquals(net.deckserver.game.enums.Sect.CAMARILLA, card.getSect());

        exec("Alice", "sect alice ready 1");
        assertEquals(net.deckserver.game.enums.Sect.NONE, card.getSect());
    }

    @Test
    void sect_invalidNameThrows() {
        readyCard(alice, "Sect Vamp");
        assertThrows(CommandException.class, () -> exec("Alice", "sect alice ready 1 zzz"));
    }

    @Test
    void clan_setsMultiWordClanAndClears() throws CommandException {
        CardData card = readyCard(alice, "Clan Vamp");

        exec("Alice", "clan alice ready 1 brujah antitribu");
        assertEquals(net.deckserver.game.enums.Clan.BRUJAH_ANTITRIBU, card.getClan());

        exec("Alice", "clan alice ready 1");
        assertEquals(net.deckserver.game.enums.Clan.NONE, card.getClan());
    }

    // ── open ─────────────────────────────────────────────────────────────

    @Test
    void open_togglesOpenHand() throws CommandException {
        exec("Alice", "open");
        assertTrue(data.isPlayerOpenHand("Alice"));

        exec("Alice", "open");
        assertFalse(data.isPlayerOpenHand("Alice"));
    }

    // ── unrecognized command ─────────────────────────────────────────────

    @Test
    void unknownCommand_isSilentlyIgnored() {
        // No `default` branch in the switch — matches current (arguably
        // surprising) behavior: a typo'd command is a silent no-op, not a
        // rejected command.
        assertDoesNotThrow(() -> exec("Alice", "bogus 1 2 3"));
    }

    // ── doMessage ────────────────────────────────────────────────────────

    @Test
    void doMessage_emptyReturnsError_nonEmptySucceeds() {
        assertEquals("No message received", doCommand.doMessage("Alice", "", false));
        assertNull(doCommand.doMessage("Alice", "Hello table", false));
    }

    // ── normalizeReadyOrder (D35b) ───────────────────────────────────────

    @Test
    void normalizeReadyOrder_movesPermanentsAfterMinions_stably() {
        CardData perm1 = addCard(alice, RegionType.READY, "900010", "Powerbase");
        CardData minA = addCard(alice, RegionType.READY, "900011", "Minion A");
        CardData perm2 = addCard(alice, RegionType.READY, "900012", "Location");
        CardData minB = addCard(alice, RegionType.READY, "900013", "Minion B");
        minA.setMinion(true);
        minB.setMinion(true);
        // perm1 / perm2 keep the default minion=false

        game.normalizeReadyOrder();

        assertEquals(
                java.util.List.of(minA, minB, perm1, perm2),
                alice.getRegion(RegionType.READY).getCards(),
                "minions first (insertion order kept), then permanents (insertion order kept)");
    }
}
