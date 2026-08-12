package net.deckserver.rest;

import net.deckserver.JolAdmin;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.TournamentFormat;
import net.deckserver.services.DeckService;
import net.deckserver.services.GameService;
import net.deckserver.services.PlayerService;
import net.deckserver.services.TournamentService;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.CardSimple;
import net.deckserver.storage.json.system.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Path("/tournament")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentResource extends BaseResource {

    private static final Logger log = LoggerFactory.getLogger(TournamentResource.class);

    /** Replaces DS.createTournament() */
    @POST
    public boolean createTournament(CreateTournamentRequest body) {
        if (!JolAdmin.isTournamentAdmin(username())) return false;
        try {
            String originalName = body.originalName() != null ? body.originalName() : "";
            boolean isRename = !originalName.isEmpty() && !originalName.equals(body.tourName());

            // Resolve the existing definition: by current name, or by original name when renaming
            TournamentDefinition current = TournamentService.getTournament(body.tourName());
            if (current == null && isRename) {
                current = TournamentService.getTournament(originalName);
            }

            GameStatus existingStatus = Optional.ofNullable(current)
                    .map(TournamentDefinition::getStatus)
                    .orElse(GameStatus.EDIT);
            String lookupName = (current != null && isRename) ? originalName : body.tourName();
            if (current != null && existingStatus == GameStatus.STARTING) {
                GameFormat newFormat = GameFormat.from(body.gameFormat());
                if (!newFormat.equals(current.getDeckFormat())) {
                    TournamentService.clearRegistrations(lookupName);
                }
            }

            Set<TournamentRegistration> existingRegs = current != null
                    ? new HashSet<>(current.getRegistrations())
                    : new HashSet<>();

            TournamentDefinition def = TournamentDefinitionCreator.newTourDef()
                    .withName(body.tourName())
                    .withRegStart(OffsetDateTime.of(LocalDate.parse(body.regStart()), LocalTime.MIDNIGHT, ZoneOffset.UTC))
                    .withRegEnd(OffsetDateTime.of(LocalDate.parse(body.regEnd()), LocalTime.MIDNIGHT, ZoneOffset.UTC))
                    .withPlayStart(OffsetDateTime.of(LocalDate.parse(body.playStart()), LocalTime.MIDNIGHT, ZoneOffset.UTC))
                    .withPlayEnd(OffsetDateTime.of(LocalDate.parse(body.playEnd()), LocalTime.MIDNIGHT, ZoneOffset.UTC))
                    .withTourFormat(TournamentFormat.valueOf(body.tourFormat()))
                    .withDeckFormat(GameFormat.from(body.gameFormat()))
                    .withRules(body.rules())
                    .withStatus(existingStatus)
                    .withSpecRules(body.specRulesCon(), body.specRules())
                    .withNumberOfRounds(Integer.parseInt(body.numberOfRounds()))
                    .withReqId(Boolean.parseBoolean(body.reqId()))
                    .withRegistrations(existingRegs)
                    .getTourDef();

            // Preserve id, rounds and finals from the existing entry so deck file paths and
            // seating data survive both normal saves and renames
            if (current != null) {
                def.setId(current.getId());
                def.setRounds(current.getRounds());
                def.setFinals(current.getFinals());
            }

            // Remove the old entry before inserting under the new name
            if (isRename) {
                TournamentService.removeTournament(originalName);
            }

            TournamentService.createTournament(def);
            return true;
        } catch (Exception e) {
            log.error("Failed to create tournament '{}'", body.tourName(), e);
            return false;
        }
    }

    /** Publish tournament: moves status from EDIT to STARTING */
    @POST
    @Path("{name}/publish")
    public boolean publishTournament(@PathParam("name") String tourName) {
        if (!JolAdmin.isTournamentAdmin(username())) return false;
        TournamentDefinition def = TournamentService.getTournament(tourName);
        if (def == null || !def.getStatus().equals(GameStatus.EDIT)) return false;
        TournamentService.setTournamentStatus(tourName, GameStatus.STARTING);
        TournamentService.save();
        return true;
    }

    /** Round summary with live pool data for the read-only active view */
    @GET
    @Path("{name}/round-summary")
    public Map<Integer, Map<Integer, List<PlayerRoundSummary>>> getRoundSummary(@PathParam("name") String tourName) {
        TournamentDefinition def = TournamentService.getTournament(tourName);
        Map<Integer, Map<Integer, List<PlayerRoundSummary>>> result = new HashMap<>();
        if (def.getRounds() == null) return result;
        def.getRounds().forEach((round, tables) -> {
            Map<Integer, List<PlayerRoundSummary>> tableMap = new HashMap<>();
            tables.forEach((table, players) -> {
                String gameName = String.format("%s: Round %d - Table %d", tourName, round, table);
                JolGame game = null;
                try { game = GameService.getGameByName(gameName); } catch (Exception ignored) {}
                JolGame finalGame = game;
                List<PlayerRoundSummary> summaries = players.stream()
                        .map(tp -> {
                            int pool = 30;
                            float vp = tp.getVp();
                            if (finalGame != null) {
                                try { pool = finalGame.getPool(tp.getName()); } catch (Exception ignored) {}
                                try { vp = (float) finalGame.getVictoryPoints(tp.getName()); } catch (Exception ignored) {}
                            }
                            return new PlayerRoundSummary(tp.getName(), vp, tp.isGw(), pool);
                        })
                        .collect(Collectors.toList());
                tableMap.put(table, summaries);
            });
            result.put(round, tableMap);
        });
        return result;
    }

    /** Close a finished table game, snapshot live VP/GW into tournament data, then end the game. */
    @POST
    @Path("{name}/round/{round}/table/{table}/close")
    public boolean closeTableGame(
            @PathParam("name") String tourName,
            @PathParam("round") int round,
            @PathParam("table") int table) {
        if (!JolAdmin.isTournamentAdmin(username())) return false;
        TournamentDefinition def = TournamentService.getTournament(tourName);
        if (def == null) return false;

        String gameName = String.format("%s: Round %d - Table %d", tourName, round, table);
        JolGame game = null;
        try { game = GameService.getGameByName(gameName); } catch (Exception ignored) {}
        if (game == null) return false;

        List<TournamentPlayer> players = def.getPlayers(round, table);
        if (players == null || players.isEmpty()) return false;

        // Guard: only close when every player is at 0 pool
        final JolGame g = game;
        boolean allDone = players.stream().allMatch(tp -> {
            try { return g.getPool(tp.getName()) <= 0; } catch (Exception ignored) { return true; }
        });
        if (!allDone) return false;

        // Determine GW winner — same logic as JolAdmin.endGame
        String gwWinner = null;
        double topVP = 0.0;
        for (TournamentPlayer tp : players) {
            double vp;
            try { vp = g.getVictoryPoints(tp.getName()); } catch (Exception ignored) { vp = 0; }
            if (vp >= 2.0) {
                if (gwWinner == null) {
                    gwWinner = tp.getName();
                    topVP = vp;
                } else if (vp > topVP) {
                    gwWinner = tp.getName();
                    topVP = vp;
                } else {
                    gwWinner = null;
                }
            }
        }

        // Snapshot live VP and GW into the stored TournamentPlayer records
        String finalGwWinner = gwWinner;
        for (TournamentPlayer tp : players) {
            double vp;
            try { vp = g.getVictoryPoints(tp.getName()); } catch (Exception ignored) { vp = tp.getVp(); }
            tp.setVp((float) vp);
            tp.setGw(tp.getName().equals(finalGwWinner));
        }
        TournamentService.save();

        // End game via standard flow (also writes GameHistory)
        JolAdmin.endGame(gameName, true);
        return true;
    }

    /** Replaces DS.loadTournamentDetails() */
    @GET
    @Path("{name}/details")
    public TournamentDetails loadTournamentDetails(@PathParam("name") String tourName) {
        return new TournamentDetails(TournamentService.getTournament(tourName));
    }

    /** Replaces DS.getRoundsForTournament() */
    @GET
    @Path("{name}/rounds")
    public Map<Integer, Map<Integer, List<TournamentPlayer>>> getRounds(@PathParam("name") String tourName) {
        return TournamentService.getTournament(tourName).getRounds();
    }

    /** Replaces DS.getRoundsForTournamentCsv() */
    @GET
    @Path("{name}/rounds/csv")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRoundsCsv(@PathParam("name") String tourName) {
        return RoundsDetails.exportPastGamesAsCsv(TournamentService.getTournament(tourName).getRounds());
    }

    /** Replaces DS.getTournamentPlayers() — registered players with decks (used for table creation) */
    @GET
    @Path("{name}/players")
    public List<TournamentRegistration> getTournamentPlayers(@PathParam("name") String tourName) {
        return TournamentService.getRegistrations(tourName).stream()
                .filter(p -> p.getDeck() != null)
                .collect(Collectors.toList());
    }

    /** All registered players regardless of deck status — used for seating assignment */
    @GET
    @Path("{name}/registered")
    public List<TournamentRegistration> getAllRegisteredPlayers(@PathParam("name") String tourName) {
        return new ArrayList<>(TournamentService.getRegistrations(tourName));
    }

    /** Replaces DS.createTournamentTables() */
    @POST
    @Path("{name}/tables")
    public void createTournamentTables(@PathParam("name") String tourName) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        TournamentService.createTournamentTables(tourName);
    }

    /** Replaces DS.saveTables() */
    @PUT
    @Path("{name}/rounds")
    public void saveTables(@PathParam("name") String tourName, Map<Integer, Map<Integer, List<String>>> rounds) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        Map<Integer, Map<Integer, List<TournamentPlayer>>> config = new HashMap<>();
        rounds.forEach((round, tableMap) -> {
            Map<Integer, List<TournamentPlayer>> tables = new HashMap<>();
            tableMap.forEach((table, players) -> {
                List<TournamentPlayer> playerList = players.stream()
                        .filter(name -> name != null && !name.isEmpty())
                        .map(name -> { TournamentPlayer tp = new TournamentPlayer(); tp.setName(name); return tp; })
                        .collect(Collectors.toList());
                tables.put(table, playerList);
            });
            config.put(round, tables);
        });
        TournamentDefinition tournament = TournamentService.getTournament(tourName);
        if (tournament == null) return;
        tournament.setRounds(config);
        TournamentService.save();
    }

    /** Replaces DS.importTables() */
    @POST
    @Path("{name}/rounds/import")
    public void importTables(@PathParam("name") String tourName, ImportTablesRequest body) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        try {
            TournamentService.importRoundsFromCsv(tourName, body.csvData());
        } catch (Exception e) {
            throw new RuntimeException("Failed to import tournament tables from CSV", e);
        }
    }

    /** Replaces DS.createFinalTable() */
    @POST
    @Path("{name}/final")
    public void createFinalTable(@PathParam("name") String tourName) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        TournamentService.createFinal(tourName);
    }

    /** Replaces DS.setFinalSeeding() */
    @PUT
    @Path("{name}/seeding")
    public void setFinalSeeding(@PathParam("name") String tourName, List<String> seeding) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        TournamentService.getTournament(tourName).getFinals().setSeeding(seeding);
    }

    /** Replaces DS.loadFinalSeeding() */
    @GET
    @Path("{name}/seeding")
    public List<String> loadFinalSeeding(@PathParam("name") String tourName) {
        return TournamentService.getTournament(tourName).getFinals().getSeeding();
    }

    /** Replaces DS.closeTournament() */
    @POST
    @Path("{name}/close")
    public void closeTournament(@PathParam("name") String tourName) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        TournamentService.setTournamentStatus(tourName, GameStatus.CLOSED);
    }

    /** Replaces DS.joinTournament() */
    @POST
    @Path("{name}/join")
    public Map<String, Object> joinTournament(@PathParam("name") String tourName) {
        String playerName = username();
        String veknId = PlayerService.get(playerName).getVeknId();
        TournamentService.joinTournament(tourName, playerName, veknId);
        return update(playerName);
    }

    /** Replaces DS.leaveTournament() */
    @POST
    @Path("{name}/leave")
    public Map<String, Object> leaveTournament(@PathParam("name") String tourName) {
        String playerName = username();
        TournamentService.leaveTournament(tourName, playerName);
        return update(playerName);
    }

    /** Replaces DS.registerTournamentDeck() */
    @POST
    @Path("{name}/deck")
    public Map<String, Object> registerTournamentDeck(@PathParam("name") String tournament, RegisterDeckRequest body) {
        String playerName = username();
        DeckInfo deckInfo = DeckService.get(playerName, body.deckName());
        ExtendedDeck deck = DeckService.getDeck(deckInfo.getDeckId());
        TournamentService.registerDeck(tournament, playerName, deck);
        return update(playerName);
    }

    /** Replaces DS.resetTables() */
    @DELETE
    @Path("{name}/rounds")
    public void resetTables(@PathParam("name") String tourName) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        TournamentService.getTournament(tourName).resetRounds();
    }

    /** Replaces DS.getFinalPlayers() */
    @GET
    @Path("{name}/final-players")
    public List<TournamentRegistration> getFinalPlayers(@PathParam("name") String tourName) {
        List<TournamentRegistration> tournamentPlayers = getTournamentPlayers(tourName);
        List<String> seeding = TournamentService.getTournament(tourName).getFinals().getSeeding();
        return seeding.stream()
                .map(name -> tournamentPlayers.stream()
                        .filter(p -> Objects.equals(p.getPlayer(), name))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Replaces DS.saveFinal() */
    @PUT
    @Path("{name}/final-players")
    public void saveFinal(@PathParam("name") String tourName, List<String> players) {
        if (!JolAdmin.isTournamentAdmin(username())) return;
        if (!GameService.existsGame(String.format("%s: Final Table", tourName))) {
            TournamentFinals finals = new TournamentFinals();
            finals.setSeeding(players);
            TournamentService.getTournament(tourName).setFinals(finals);
        }
    }

    /** Replaces DS.getRegDelta() */
    @GET
    @Path("{name}/round-delta")
    public Map<Integer, List<TournamentRegistration>> getRegDelta(@PathParam("name") String tourName,
                                                                   @QueryParam("round") Integer roundNumber) {
        HashMap<Integer, List<TournamentRegistration>> result = new HashMap<>();
        Map<Integer, List<TournamentPlayer>> roundTables = getRounds(tourName).get(roundNumber);
        if (roundTables == null) {
            result.put(roundNumber, new ArrayList<>(getTournamentPlayers(tourName)));
            return result;
        }
        List<TournamentRegistration> tablePlayers = new ArrayList<>();
        roundTables.values().forEach(tps -> tablePlayers.addAll(tps.stream().map(p -> {
            TournamentRegistration tr = new TournamentRegistration();
            tr.setPlayer(p.getName());
            return tr;
        }).collect(Collectors.toSet())));
        List<TournamentRegistration> regPlayers = new ArrayList<>(getTournamentPlayers(tourName));
        regPlayers.removeAll(tablePlayers);
        result.put(roundNumber, regPlayers);
        return result;
    }

    /** Returns registered players not yet placed in the finals seeding (used by DS.getFinalDelta()) */
    @GET
    @Path("{name}/final-delta")
    public List<TournamentRegistration> getFinalDelta(@PathParam("name") String tourName) {
        List<TournamentRegistration> regPlayers = new ArrayList<>(getTournamentPlayers(tourName));
        List<String> seeding = TournamentService.getTournament(tourName).getFinals().getSeeding();
        if (seeding != null) {
            regPlayers.removeIf(r -> seeding.contains(r.getPlayer()));
        }
        return regPlayers;
    }

    /** Replaces DS.loadCrypt() */
    @GET
    @Path("{name}/crypt")
    public List<CardSimple> loadCrypt(@PathParam("name") String tourName, @QueryParam("player") String player) {
        TournamentRegistration reg = TournamentService.getRegistrations(tourName, player)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return TournamentService.getRandomCrypt(tourName, reg.getDeck());
    }

    /** Replaces DS.cryptCount() */
    @GET
    @Path("{name}/crypt-count")
    public String cryptCount(@PathParam("name") String tourName, @QueryParam("player") String player) {
        TournamentRegistration reg = TournamentService.getRegistrations(tourName, player)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return String.valueOf(TournamentService.getCryptCount(tourName, reg.getDeck()) - 3);
    }

    /** Replaces DS.tournamentAlreadyActive() */
    @GET
    @Path("{name}/status")
    public boolean tournamentAlreadyActive(@PathParam("name") String tourName) {
        return TournamentService.getTournament(tourName).getStatus().equals(GameStatus.ACTIVE);
    }

    /** Replaces DS.gameAlreadyStarted() */
    @GET
    @Path("{name}/game-started")
    public boolean gameAlreadyStarted(@PathParam("name") String tourName) {
        try {
            GameService.getGameByName(tourName);
        } catch (NullPointerException ex) {
            return false;
        }
        return true;
    }

    /** Replaces DS.getTournamentRounds() */
    @GET
    @Path("{name}/rounds-count")
    public int[] getTournamentRounds(@PathParam("name") String tourName) {
        return IntStream.range(1, TournamentService.getTournament(tourName).getNumberOfRounds() + 1).toArray();
    }

    /** Aggregated standings for finalist selection: VP and GW totalled across all rounds, competition-ranked. */
    @GET
    @Path("{name}/standings")
    public List<PlayerStanding> getStandings(@PathParam("name") String tourName) {
        TournamentDefinition def = TournamentService.getTournament(tourName);
        if (def == null || def.getRounds() == null || def.getRounds().isEmpty()) return List.of();

        Map<String, Float> vpTotals = new LinkedHashMap<>();
        Map<String, Integer> gwCounts = new LinkedHashMap<>();

        def.getRegistrations().forEach(reg -> {
            vpTotals.put(reg.getPlayer(), 0f);
            gwCounts.put(reg.getPlayer(), 0);
        });
        def.getRounds().values().forEach(tables -> tables.values().forEach(players -> players.forEach(tp -> {
            vpTotals.merge(tp.getName(), tp.getVp(), Float::sum);
            if (tp.isGw()) gwCounts.merge(tp.getName(), 1, Integer::sum);
        })));

        Map<String, String> veknByPlayer = def.getRegistrations().stream()
                .collect(Collectors.toMap(TournamentRegistration::getPlayer, r -> r.getVekn() != null ? r.getVekn() : "", (a, b) -> a));

        List<String> sorted = new ArrayList<>(vpTotals.keySet());
        sorted.sort(Comparator
                .<String>comparingInt(p -> -gwCounts.getOrDefault(p, 0))
                .thenComparingDouble(p -> -vpTotals.getOrDefault(p, 0f)));

        List<PlayerStanding> standings = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                String prev = sorted.get(i - 1), curr = sorted.get(i);
                boolean sameTier = gwCounts.getOrDefault(prev, 0).equals(gwCounts.getOrDefault(curr, 0))
                        && Float.compare(vpTotals.getOrDefault(prev, 0f), vpTotals.getOrDefault(curr, 0f)) == 0;
                if (!sameTier) rank = i + 1;
            }
            String player = sorted.get(i);
            standings.add(new PlayerStanding(player, veknByPlayer.getOrDefault(player, ""),
                    gwCounts.getOrDefault(player, 0), vpTotals.getOrDefault(player, 0f), rank));
        }
        return standings;
    }

    public record CreateTournamentRequest(String tourName, String regStart, String regEnd, String playStart,
                                          String playEnd, String tourFormat, String gameFormat, String[] rules,
                                          String specRulesCon, String[] specRules, String numberOfRounds, String reqId,
                                          String originalName) {}
    public record ImportTablesRequest(String csvData) {}
    public record RegisterDeckRequest(String deckName) {}
    public record PlayerRoundSummary(String name, float vp, boolean gw, int pool) {}
    public record PlayerStanding(String player, String vekn, int gw, float vp, int rank) {}
}
