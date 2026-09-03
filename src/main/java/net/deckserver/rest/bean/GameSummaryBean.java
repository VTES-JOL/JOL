package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.model.JolGame;
import net.deckserver.services.GameService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * One active game as shown on the Watch → Active Games tab. Beyond the
 * name/turn/timestamp the old table needed, this now also projects the live,
 * in-memory game state ({@link JolGame}) the spectator card shows at a glance:
 * format, round, active player, edge holder, and per-player pool / victory
 * points / ousted status in seating order.
 *
 * <p>All of this is a handful of field reads off the authoritative in-memory
 * {@code GameData} — no database, no snapshot. The tab polls, so each poll
 * returns fresh values.
 */
@Getter
public class GameSummaryBean {

    private final String gameName;
    private final String gameId;
    /** Turn label, e.g. {@code "Ludwig 5.2"}. */
    private final String turn;
    /** Round number (the integer before the dot in the raw turn), for "furthest along" sorting. */
    private final int round;
    private final String timestamp;
    private final String format;
    private final String activePlayer;
    private final String edge;
    private final List<PlayerSummary> players;

    public GameSummaryBean(String gameName) {
        JolGame game = GameService.getGameByName(gameName);
        this.gameName = gameName;
        this.gameId = GameService.get(gameName).getId();
        this.turn = game.getTurnLabel();
        this.round = parseRound(game.getCurrentTurn());
        this.timestamp = JolAdmin.getGameTimeStamp(gameName).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.format = JolAdmin.getFormat(gameName);
        this.activePlayer = game.getActivePlayer();
        this.edge = game.getEdge();
        this.players = game.getPlayers().stream()
                .map(name -> new PlayerSummary(
                        name,
                        game.getPool(name),
                        game.getVictoryPoints(name),
                        game.isOusted(name)))
                .toList();
    }

    private static int parseRound(String rawTurn) {
        try {
            return Integer.parseInt(rawTurn.split("\\.")[0]);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public record PlayerSummary(String name, int pool, double vp, boolean ousted) {}
}
