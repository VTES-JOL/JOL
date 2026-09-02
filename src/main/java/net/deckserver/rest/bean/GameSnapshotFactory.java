package net.deckserver.rest.bean;

import net.deckserver.JolAdmin;
import net.deckserver.game.cards.Card;
import net.deckserver.game.cards.CardRegistry;
import net.deckserver.game.cards.CryptCard;
import net.deckserver.game.cards.LibraryCard;
import net.deckserver.game.enums.Phase;
import net.deckserver.game.enums.RegionType;
import net.deckserver.game.model.GameModel;
import net.deckserver.game.model.JolGame;
import net.deckserver.game.ui.CardDetail;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.JudgeService;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.game.CardData;
import net.deckserver.storage.json.game.JudgeRequestData;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

/**
 * Builds a viewer-aware {@link GameSnapshot} tree directly from {@link JolGame}
 * — the React game page's replacement for GameView.create()'s hand/state HTML
 * fragments. Deliberately independent of GameView: no dirty-flag tracking (see
 * GameSnapshot's javadoc), no shared mutable state, safe to call from any
 * number of concurrent requests.
 *
 * <p>The hidden-card visibility rule below replicates a genuinely subtle piece
 * of region.jsp/card.jsp behavior, traced carefully since getting it wrong
 * either leaks an opponent's hidden card identity (security) or incorrectly
 * hides something legacy shows (correctness):
 * <ul>
 *   <li>A card directly in a region is visible if the region itself is visible
 *       to the viewer ({@link RegionType#isVisible}), or the region's hand is
 *       "open" (revealed to everyone), or the card's owner differs from the
 *       region's owning player (a "foreign" card — e.g. a stolen vampire —
 *       sitting in someone else's region is always shown).</li>
 *   <li>Once a card is visible, every card nested under it (equipment, allies,
 *       blood counters, ...) is visible unconditionally, regardless of that
 *       nested card's own owner — card.jsp's own recursive include never
 *       re-applies the owner/region check, only the top-level region.jsp call
 *       does.</li>
 *   <li>A hidden card renders as a bare placeholder: only its id and counter
 *       count cross the wire, nothing else identifying — matching
 *       card-hidden.jsp, which never recurses into a hidden card's children.</li>
 * </ul>
 */
public class GameSnapshotFactory {

    public static GameSnapshot build(GameModel model, String viewer, String status) {
        JolGame game = GameService.getGameByName(model.getName());
        boolean isPlayer = model.getPlayers().contains(viewer);
        boolean isAdmin = !isPlayer && JolAdmin.getOwner(model.getName()).equals(viewer);
        boolean isJudge = !isPlayer && JolAdmin.isJudge(viewer);

        if (isPlayer) {
            JolAdmin.recordPlayerAccess(viewer, model.getName());
        }

        List<String> pinged = JolAdmin.getPings(model.getName());
        List<PlayerSnapshot> players = game.getPlayers().stream()
                .map(playerName -> buildPlayer(game, playerName, viewer, pinged))
                .toList();

        Phase phase = game.getPhase();
        List<String> phases = new ArrayList<>();
        boolean show = false;
        for (Phase p : Phase.values()) {
            if (phase.equals(p)) show = true;
            if (show) phases.add(p.getDescription());
        }

        String edgeColor = PlayerService.get(viewer).getEdgeColor();

        JudgeRequestData openRequest = JudgeService.getOpenForGame(game.id());
        JudgeRequestBean judgeRequest = openRequest == null ? null
                : JudgeRequestBean.of(openRequest, viewer, isJudge && !openRequest.isTournament());

        return GameSnapshot.builder()
                .id(game.id())
                .name(model.getName())
                .players(players)
                .currentPlayer(game.getActivePlayer())
                .edgePlayer(game.getEdge())
                .turn(game.data().getTurn())
                .turnLabel(game.getTurnLabel())
                .phase(phase.getDescription())
                .phases(phases)
                .turns(ChatService.getTurns(game.id()))
                .pingOptions(JolAdmin.getPingList(model.getName()))
                .player(isPlayer)
                .admin(isAdmin)
                .judge(isJudge)
                .globalNotes(game.getGlobalText())
                .privateNotes(isPlayer ? game.getPrivateNotes(viewer) : null)
                .edgeColor(edgeColor)
                .edgeTextColor(colorIsDark(edgeColor) ? "white" : "black")
                .status(status)
                .stamp(OffsetDateTime.now().format(ISO_OFFSET_DATE_TIME))
                .judgeRequest(judgeRequest)
                .build();
    }

    private static PlayerSnapshot buildPlayer(JolGame game, String playerName, String viewer, List<String> pinged) {
        List<RegionSnapshot> regions = new ArrayList<>();
        for (RegionType type : RegionType.values()) {
            RegionSnapshot region = buildRegion(game, playerName, viewer, type);
            if (!region.getCards().isEmpty()) {
                regions.add(region);
            }
        }
        return PlayerSnapshot.builder()
                .name(playerName)
                .pool(game.getPool(playerName))
                .victoryPoints(game.getVictoryPoints(playerName))
                .active(playerName.equals(game.getActivePlayer()))
                .edge(playerName.equals(game.getEdge()))
                .pinged(pinged.contains(playerName))
                .regions(regions)
                .build();
    }

    private static RegionSnapshot buildRegion(JolGame game, String regionOwner, String viewer, RegionType type) {
        boolean hand = type == RegionType.HAND;
        boolean openHand = hand && Boolean.TRUE.equals(game.data().isPlayerOpenHand(regionOwner));
        boolean hiddenHand = hand && !openHand;
        boolean regionVisible = type.isVisible(regionOwner, viewer) || openHand;

        // Play-modal data (modes / replace rules / preamble / cost) is only
        // ever used from the viewer's own hand or research region — enrich just
        // those cards rather than bloating every snapshot card with it.
        boolean enrich = (type == RegionType.HAND || type == RegionType.RESEARCH) && regionOwner.equals(viewer);

        List<CardData> cards = game.data().getPlayerRegion(regionOwner, type).getCards();
        List<CardSnapshot> cardSnapshots = cards.stream()
                .map(card -> {
                    boolean visible = regionVisible || !card.getOwnerName().equals(regionOwner);
                    return visible ? buildVisibleCard(card, enrich) : hiddenCard(card);
                })
                .toList();

        return RegionSnapshot.builder()
                .type(type.name())
                .commandKey(type.xmlLabel().split(" ")[0])
                .label(regionLabel(type))
                .simple(RegionType.SIMPLE_REGIONS.contains(type))
                .openHand(openHand)
                .hiddenHand(hiddenHand)
                .cards(cardSnapshots)
                .build();
    }

    // player.jsp passes its own short labels per region.jsp include — these
    // deliberately differ from RegionType.description() (e.g. "Ready", not
    // "Ready region"); replicated verbatim rather than switched to description()
    // to avoid a visible label regression.
    private static String regionLabel(RegionType type) {
        return switch (type) {
            case READY -> "Ready";
            case TORPOR -> "Torpor";
            case UNCONTROLLED -> "Uncontrolled";
            case ASH_HEAP -> "Ash heap";
            case REMOVED_FROM_GAME -> "Removed from game";
            case RESEARCH -> "Research";
            case LIBRARY -> "Library";
            case CRYPT -> "Crypt";
            case HAND -> "Hand";
        };
    }

    private static CardSnapshot hiddenCard(CardData card) {
        return CardSnapshot.builder()
                .id(card.getId())
                .visible(false)
                .counters(card.getCounters())
                .cards(List.of())
                .build();
    }

    // Every card nested under a visible card is visible unconditionally —
    // see the class javadoc.
    private static CardSnapshot buildVisibleCard(CardData card, boolean enrich) {
        CardDetail detail = new CardDetail(card);
        Card definition = CardRegistry.findById(detail.getCardId());
        boolean advanced = definition instanceof CryptCard c && c.advanced();
        List<CardSnapshot> nested = card.getCards().stream()
                .map(nestedCard -> buildVisibleCard(nestedCard, enrich))
                .toList();
        CardSnapshot.CardSnapshotBuilder builder = CardSnapshot.builder()
                .id(card.getId())
                .visible(true)
                .counters(card.getCounters())
                .cardId(detail.getCardId())
                .name(definition != null ? definition.name() : card.getName())
                .advanced(advanced)
                .disciplines(detail.getDisciplines())
                .capacity(detail.getCapacity())
                .votes(detail.getVotes())
                .contested(detail.isContested())
                .locked(detail.isLocked())
                .infernal(detail.isInfernal())
                .playtest(detail.isPlaytest())
                .clan(detail.getClan() != null ? detail.getClan().toString() : null)
                .sect(detail.getSect() != null ? detail.getSect().toString() : null)
                .path(detail.getPath() != null ? detail.getPath().toString() : null)
                .label(detail.getLabel())
                .minion(detail.isMinion())
                .typeClass(definition != null ? definition.typeClass() : null)
                .clanClasses(definition != null ? definition.clanClasses() : null)
                .hasBlood(definition != null && definition.hasBlood())
                .hasLife(definition != null && definition.hasLife())
                .cards(nested);

        if (enrich && definition instanceof LibraryCard lib) {
            builder.modes(lib.playModes().stream().map(PlayModeBean::of).toList())
                    .multiMode(lib.multiMode())
                    .doNotReplace(lib.doNotReplace())
                    .preamble(lib.preamble())
                    .cost(formatCost(lib));
        }
        return builder.build();
    }

    /** Mirrors the old SummaryCard cost string: "<n> pool" / "<n> blood" / "<n> conviction". */
    private static String formatCost(LibraryCard lib) {
        if (lib.poolCost() != null) return costValue(lib.poolCost()) + " pool";
        if (lib.bloodCost() != null) return costValue(lib.bloodCost()) + " blood";
        if (lib.convictionCost() != null) return costValue(lib.convictionCost()) + " conviction";
        return null;
    }

    private static String costValue(int cost) {
        return cost < 0 ? "X" : String.valueOf(cost);
    }

    private static boolean colorIsDark(String bgColor) {
        String color = (bgColor.charAt(0) == '#') ? bgColor.substring(1, 7) : bgColor;
        int r = Integer.parseInt(color.substring(0, 2), 16);
        int g = Integer.parseInt(color.substring(2, 4), 16);
        int b = Integer.parseInt(color.substring(4, 6), 16);
        return ((r * 0.299) + (g * 0.587) + (b * 0.114)) <= 186;
    }
}
