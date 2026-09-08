package net.deckserver.game.model;

import net.deckserver.game.enums.Clan;
import net.deckserver.game.enums.Path;
import net.deckserver.game.enums.RegionType;
import net.deckserver.game.enums.Sect;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.game.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract test: every command string the React client actually builds (not
 * user-typed free text — see {@code CommandForm}'s own Command field, which
 * is out of scope here) must be accepted by {@link DoCommand#doCommand}.
 *
 * <p>Each test below is traced to the client builder it mirrors — the exact
 * keyword order and token shape is copied from that source, so a change to
 * either side that breaks the contract shows up as a failure here rather
 * than as a live "Invalid card position" / NPE in someone's game. The client
 * side of the same contract (that each builder emits exactly this string for
 * given inputs) is covered separately by
 * {@code src/main/webui/src/pages/game/cardCommands.test.ts},
 * {@code useCounterBump.test.ts}, and (for the canned buttons) manual
 * comparison against {@code QuickCommandModal.tsx}.
 *
 * <p>Uses the same bare {@code GameData}/{@code CardData} fixture style as
 * {@link DoCommandTest} — see that class for the full command-by-command
 * behavioral suite this one intentionally does not duplicate.
 */
@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
class UiCommandContractTest {

    private GameData data;
    private JolGame game;
    private DoCommand doCommand;
    private PlayerData player1;
    private PlayerData player2;

    @BeforeEach
    void setUp() {
        data = new GameData("g1", "Test Game");
        player1 = new PlayerData("Player1");
        player2 = new PlayerData("Player2");
        data.addPlayer(player1);
        data.addPlayer(player2);
        game = new JolGame("g1", data);
        data.setCurrentPlayer(player1);
        doCommand = new DoCommand(game, new GameModel(game.getName(), game));
    }

    private CardData addCard(PlayerData owner, RegionType region, String name) {
        CardData card = new CardData("900001", owner);
        card.setName(name);
        owner.getRegion(region).addCard(card, false);
        data.getCards().put(card.getId(), card);
        return card;
    }

    private CardData ready(PlayerData owner, String name) {
        return addCard(owner, RegionType.READY, name);
    }

    /** Submits exactly as the client would in one round trip — including a `;`-joined batch, like the "Ousted prey!" quick command. */
    private void submit(String actor, String command) throws CommandException {
        for (String piece : command.split(";")) {
            doCommand.doCommand(actor, piece.trim());
        }
    }

    // ── cardActions (cardCommands.ts) — simple card-state toggles ──────────

    @Test
    void lock_unlock_hide_reveal_contest() throws CommandException {
        ready(player1, "Vamp");

        assertDoesNotThrow(() -> submit("Player1", "lock Player1 ready 1"));
        assertDoesNotThrow(() -> submit("Player1", "unlock Player1 ready 1"));
        assertDoesNotThrow(() -> submit("Player1", "hide Player1 ready 1"));
        assertDoesNotThrow(() -> submit("Player1", "reveal Player1 ready 1"));
        assertDoesNotThrow(() -> submit("Player1", "contest Player1 ready 1"));
        assertDoesNotThrow(() -> submit("Player1", "contest Player1 ready 1 clear"));
    }

    @Test
    void reveal_fromNonDefaultRegion() throws CommandException {
        // cardActions.reveal(makeCtx({ regionCommandKey: 'torpor', coordinate: '2' }))
        addCard(player1, RegionType.TORPOR, "Torpid Vamp 1");
        CardData target = addCard(player1, RegionType.TORPOR, "Torpid Vamp 2");
        target.setFaceDown(true);

        submit("Player1", "reveal Player1 torpor 2");

        assertFalse(target.isFaceDown());
    }

    @Test
    void bleed_hunt_goAnarch_leaveTorpor_lockWithChat() throws CommandException {
        // These all send `lock <player> <region> <coord>` as the command and a
        // separate fixed chat line — the command half is what's under test.
        ready(player1, "Bleeder");
        assertDoesNotThrow(() -> submit("Player1", "lock Player1 ready 1"));
    }

    @Test
    void torpor_movesToControllersOwnTorpor() throws CommandException {
        // cardActions.torpor: `move <player> <region> <coord> <player> torpor`
        CardData card = ready(player1, "Torporing Vamp");

        submit("Player1", "move Player1 ready 1 Player1 torpor");

        assertEquals(RegionType.TORPOR, card.getRegion().getType());
    }

    @Test
    void banish_playerAndCoordinateOnly() throws CommandException {
        // cardActions.banish: `banish <player> <coord>`
        CardData card = ready(player1, "Banished");

        submit("Player1", "banish Player1 1");

        assertEquals(RegionType.UNCONTROLLED, card.getRegion().getType());
    }

    @Test
    void burn_playerRegionCoordinate() throws CommandException {
        CardData card = ready(player1, "Burned");

        submit("Player1", "burn Player1 ready 1");

        assertEquals(RegionType.ASH_HEAP, card.getRegion().getType());
    }

    @Test
    void influence_coordinateOnly() throws CommandException {
        CardData card = addCard(player1, RegionType.UNCONTROLLED, "Influenced");

        submit("Player1", "influence 1");

        assertEquals(RegionType.READY, card.getRegion().getType());
    }

    @Test
    void moveHand_moveReady_moveUncontrolled_noControllerName() throws CommandException {
        CardData toHand = addCard(player1, RegionType.ASH_HEAP, "To Hand");
        submit("Player1", "move ashheap 1 hand");
        assertEquals(RegionType.HAND, toHand.getRegion().getType());

        CardData toReady = addCard(player1, RegionType.ASH_HEAP, "To Ready");
        submit("Player1", "move ashheap 1 ready");
        assertEquals(RegionType.READY, toReady.getRegion().getType());

        // toReady already sits at ready-position 1 from the assertion above.
        CardData toUncontrolled = ready(player1, "To Uncontrolled");
        submit("Player1", "move ready 2 inactive");
        assertEquals(RegionType.UNCONTROLLED, toUncontrolled.getRegion().getType());
    }

    @Test
    void moveLibrary_withAndWithoutTop() throws CommandException {
        CardData card1 = addCard(player1, RegionType.ASH_HEAP, "L1");
        submit("Player1", "move ashheap 1 library");
        assertEquals(RegionType.LIBRARY, card1.getRegion().getType());

        CardData card2 = addCard(player1, RegionType.ASH_HEAP, "L2");
        submit("Player1", "move ashheap 1 library top");
        assertEquals(RegionType.LIBRARY, card2.getRegion().getType());
    }

    @Test
    void removeFromGame_rfgKeyword() throws CommandException {
        CardData card = addCard(player1, RegionType.ASH_HEAP, "Rfg Me");

        submit("Player1", "rfg Player1 ashheap 1");

        assertEquals(RegionType.REMOVED_FROM_GAME, card.getRegion().getType());
    }

    @Test
    void movePredator_movePrey() throws CommandException {
        // 2-player predator/prey degenerates (both point at the other), but the
        // command shape — `move <player> <region> <coord> predator|prey` — is
        // exactly what's under test here; see DoCommandTest for a 3-player
        // check that the right neighbour is picked.
        CardData toPredator = ready(player1, "To Predator");
        submit("Player1", "move Player1 ready 1 predator");
        assertEquals("Player2", toPredator.getRegion().getOwner());

        CardData toPrey = ready(player1, "To Prey");
        submit("Player1", "move Player1 ready 1 prey");
        assertEquals("Player2", toPrey.getRegion().getOwner());
    }

    @Test
    void addCounter_removeCounter_bloodKeyword() throws CommandException {
        CardData card = ready(player1, "Bloodied");

        submit("Player1", "blood Player1 ready 1 +1");
        assertEquals(1, card.getCounters());

        submit("Player1", "blood Player1 ready 1 -1");
        assertEquals(0, card.getCounters());
    }

    @Test
    void transferToCard_transferToPool_noControllerName() throws CommandException {
        CardData card = ready(player1, "Xfer Vamp");

        submit("Player1", "transfer ready 1 +1");
        assertEquals(1, card.getCounters());
        assertEquals(29, game.getPool("Player1"));

        submit("Player1", "transfer ready 1 -1");
        assertEquals(0, card.getCounters());
        assertEquals(30, game.getPool("Player1"));
    }

    @Test
    void label_freeText() throws CommandException {
        CardData card = ready(player1, "Labeled");

        submit("Player1", "label Player1 ready 1 my note");

        assertEquals("my note", card.getNotes());
    }

    @Test
    void clan_path_takeFirstUnderscoreSegment() throws CommandException {
        // cardActions.clan/path split the AttrSelect key on '_' before sending —
        // the command itself only ever sees the first segment.
        CardData card = ready(player1, "Attr Vamp");

        submit("Player1", "clan Player1 ready 1 brujah");
        assertEquals(Clan.BRUJAH, card.getClan());

        submit("Player1", "path Player1 ready 1 death");
        assertEquals(Path.DEATH_AND_THE_SOUL, card.getPath());
    }

    @Test
    void sect_fullKeyVerbatim() throws CommandException {
        CardData card = ready(player1, "Sect Vamp");

        submit("Player1", "sect Player1 ready 1 independent");

        assertEquals(Sect.INDEPENDENT, card.getSect());
    }

    // ── play / discard / hand-label (PlayCardModal via cardCommands.ts) ────

    @Test
    void play_minimalFromHand_defaultsToAshHeapAndDraws() throws CommandException {
        // buildPlayCommand(ctx, null, null, null, false) -> "play hand 1 draw"
        CardData card = addCard(player1, RegionType.HAND, "Played Card");
        CardData replacement = addCard(player1, RegionType.LIBRARY, "Replacement");

        submit("Player1", "play hand 1 draw");

        assertEquals(RegionType.ASH_HEAP, card.getRegion().getType());
        assertEquals(RegionType.HAND, replacement.getRegion().getType());
    }

    @Test
    void play_withDisciplines() throws CommandException {
        // buildPlayCommand(ctx, ['aus','dom'], null, null, true) -> "play hand 1 @ aus,dom"
        CardData card = addCard(player1, RegionType.HAND, "Combat Card");

        submit("Player1", "play hand 1 @ aus,dom");

        assertEquals(RegionType.ASH_HEAP, card.getRegion().getType());
    }

    @Test
    void play_toReadyRfgOrInactiveTarget() throws CommandException {
        CardData toReady = addCard(player1, RegionType.HAND, "To Ready");
        submit("Player1", "play hand 1 ready");
        assertEquals(RegionType.READY, toReady.getRegion().getType());

        CardData toRfg = addCard(player1, RegionType.HAND, "To Rfg");
        submit("Player1", "play hand 1 rfg");
        assertEquals(RegionType.REMOVED_FROM_GAME, toRfg.getRegion().getType());

        CardData toInactive = addCard(player1, RegionType.HAND, "To Inactive");
        submit("Player1", "play hand 1 inactive");
        assertEquals(RegionType.UNCONTROLLED, toInactive.getRegion().getType());
    }

    @Test
    void play_withResolvedTargetPicker() throws CommandException {
        // pickedTarget is appended verbatim, e.g. "Player2 ready 1"
        CardData minion = ready(player2, "Target Minion");
        CardData card = addCard(player1, RegionType.HAND, "Targeted Card");

        submit("Player1", "play hand 1 Player2 ready 1");

        assertSame(minion, card.getParent());
    }

    @Test
    void play_fromTableRegion_noTrailingDraw() throws CommandException {
        // buildPlayCommand never appends `draw` for a non-hand source region.
        CardData card = ready(player1, "Replayed Card");

        submit("Player1", "play ready 1 @ aus ready");

        assertEquals(RegionType.READY, card.getRegion().getType());
    }

    @Test
    void discard_withAndWithoutReplace() throws CommandException {
        CardData card = addCard(player1, RegionType.HAND, "Discarded");
        submit("Player1", "discard 1");
        assertEquals(RegionType.ASH_HEAP, card.getRegion().getType());

        CardData card2 = addCard(player1, RegionType.HAND, "Discarded 2");
        CardData replacement = addCard(player1, RegionType.LIBRARY, "Replacement");
        submit("Player1", "discard 1 draw");
        assertEquals(RegionType.ASH_HEAP, card2.getRegion().getType());
        assertEquals(RegionType.HAND, replacement.getRegion().getType());
    }

    @Test
    void handLabel_hardcodedHandRegion() throws CommandException {
        // buildHandLabelCommand always says "hand", even for a research-area card.
        CardData card = addCard(player1, RegionType.HAND, "Noted Card");

        submit("Player1", "label Player1 hand 1 a note");

        assertEquals("a note", card.getNotes());
    }

    // ── QuickCommandModal.tsx — canned buttons, fixed strings ───────────────

    @Test
    void quickCommand_unlockEdgeOpen() throws CommandException {
        assertDoesNotThrow(() -> submit("Player1", "unlock"));
        assertDoesNotThrow(() -> submit("Player1", "edge"));
        assertDoesNotThrow(() -> submit("Player1", "edge burn"));
        assertDoesNotThrow(() -> submit("Player1", "open"));
    }

    @Test
    void quickCommand_oustedPreyIsASemicolonBatch() throws CommandException {
        // send('vp +1; pool +6') — GameModel.submit splits on ';' before
        // dispatching each piece; `submit()` here replicates that split.
        submit("Player1", "vp +1; pool +6");

        assertEquals(1.0, game.getVictoryPoints("Player1"), 0.001);
        assertEquals(36, game.getPool("Player1"));
    }

    @Test
    void quickCommand_libraryHandShortcuts() throws CommandException {
        addCard(player1, RegionType.LIBRARY, "L1");
        assertDoesNotThrow(() -> submit("Player1", "draw"));

        addCard(player1, RegionType.HAND, "H1");
        assertDoesNotThrow(() -> submit("Player1", "discard random"));

        addCard(player1, RegionType.LIBRARY, "L2");
        addCard(player1, RegionType.LIBRARY, "L3");
        assertDoesNotThrow(() -> submit("Player1", "shuffle"));
    }

    @Test
    void quickCommand_cryptShortcuts() throws CommandException {
        addCard(player1, RegionType.CRYPT, "C1");
        assertDoesNotThrow(() -> submit("Player1", "draw crypt"));

        addCard(player1, RegionType.CRYPT, "C2");
        addCard(player1, RegionType.CRYPT, "C3");
        assertDoesNotThrow(() -> submit("Player1", "shuffle crypt"));
    }

    @Test
    void quickCommand_poolButtons() throws CommandException {
        // [-6..-1] render as `pool ${n}` (n already carries its own sign);
        // [1..6] render as `pool +${n}`.
        submit("Player1", "pool -6");
        assertEquals(24, game.getPool("Player1"));

        submit("Player1", "pool +6");
        assertEquals(30, game.getPool("Player1"));
    }
}
