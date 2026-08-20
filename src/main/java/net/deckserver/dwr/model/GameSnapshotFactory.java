package net.deckserver.dwr.model;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.bean.CardSnapshot;
import net.deckserver.dwr.bean.GameSnapshot;
import net.deckserver.dwr.bean.PlayerSnapshot;
import net.deckserver.dwr.bean.RegionSnapshot;
import net.deckserver.game.enums.Phase;
import net.deckserver.game.enums.RegionType;
import net.deckserver.game.ui.CardDetail;
import net.deckserver.services.CardService;
import net.deckserver.services.ChatService;
import net.deckserver.services.GameService;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.game.CardData;

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

        List<CardData> cards = game.data().getPlayerRegion(regionOwner, type).getCards();
        List<CardSnapshot> cardSnapshots = cards.stream()
                .map(card -> {
                    boolean visible = regionVisible || !card.getOwnerName().equals(regionOwner);
                    return visible ? buildVisibleCard(card) : hiddenCard(card);
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
    private static CardSnapshot buildVisibleCard(CardData card) {
        CardDetail detail = new CardDetail(card);
        CardSummary summary = CardService.get(detail.getCardId());
        List<CardSnapshot> nested = card.getCards().stream()
                .map(GameSnapshotFactory::buildVisibleCard)
                .toList();
        return CardSnapshot.builder()
                .id(card.getId())
                .visible(true)
                .counters(card.getCounters())
                .cardId(detail.getCardId())
                .name(summary.getDisplayName())
                .advanced(summary.isAdvanced())
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
                .typeClass(summary.getTypeClass())
                .clanClasses(summary.getClanClass())
                .hasBlood(summary.hasBlood())
                .hasLife(summary.hasLife())
                .cards(nested)
                .build();
    }

    // Same algorithm as GameView.colorIsDark (private there) — duplicated
    // rather than shared since GameView is left untouched.
    private static boolean colorIsDark(String bgColor) {
        String color = (bgColor.charAt(0) == '#') ? bgColor.substring(1, 7) : bgColor;
        int r = Integer.parseInt(color.substring(0, 2), 16);
        int g = Integer.parseInt(color.substring(2, 4), 16);
        int b = Integer.parseInt(color.substring(4, 6), 16);
        return ((r * 0.299) + (g * 0.587) + (b * 0.114)) <= 186;
    }
}
