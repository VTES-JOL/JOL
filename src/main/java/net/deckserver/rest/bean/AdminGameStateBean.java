package net.deckserver.rest.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.game.model.JolGame;
import net.deckserver.services.GameService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Live in-memory state of one active game for the admin Games tab: seating
 * order, current player, edge holder, per-player pool / victory points /
 * ousted status, plus each player's last-access timestamp in this game (the
 * signal an admin weighs when deciding whether to replace someone).
 *
 * <p>All field reads off the authoritative {@link JolGame} / activity
 * services — no database, no snapshot.
 */
@Getter
public class AdminGameStateBean {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final String gameId;
    private final String gameName;
    private final String format;
    /** Turn label, e.g. {@code "Ludwig 5.2"}. */
    private final String turn;
    /** Round number (integer before the dot in the raw turn). */
    private final int round;
    private final String activePlayer;
    private final String edge;
    /** Last recorded activity time for the game as a whole. */
    private final String gameTimestamp;
    private final List<PlayerState> players;

    public AdminGameStateBean(String gameName) {
        JolGame game = GameService.getGameByName(gameName);
        this.gameId = GameService.get(gameName).getId();
        this.gameName = gameName;
        this.format = JolAdmin.getFormat(gameName);
        this.turn = game.getTurnLabel();
        this.round = parseRound(game.getCurrentTurn());
        this.activePlayer = game.getActivePlayer();
        this.edge = game.getEdge();
        this.gameTimestamp = JolAdmin.getGameTimeStamp(gameName).format(ISO);
        List<String> seating = game.getPlayers();
        this.players = IntStream.range(0, seating.size())
                .mapToObj(i -> {
                    String name = seating.get(i);
                    return new PlayerState(
                            name,
                            i + 1,
                            game.getPool(name),
                            game.getVictoryPoints(name),
                            game.isOusted(name),
                            JolAdmin.getPlayerAccess(name, gameName).format(ISO));
                })
                .toList();
    }

    private static int parseRound(String rawTurn) {
        try {
            return Integer.parseInt(rawTurn.split("\\.")[0]);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public record PlayerState(String name, int seat, int pool, double vp, boolean ousted, String lastAccess) {}
}
