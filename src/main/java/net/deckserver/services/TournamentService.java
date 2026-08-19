package net.deckserver.services;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.TournamentFormat;
import net.deckserver.game.enums.Visibility;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.CardSimple;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.system.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TournamentService extends PersistedService {

    private static final BiFunction<TournamentDefinition, String, Boolean> CONTAINS_PLAYER = (tournament, player) -> tournament.getRegistrations().stream().map(TournamentRegistration::getPlayer).toList().contains(player);
    private static final Predicate<TournamentDefinition> REGISTRATIONS_OPEN = t -> t.getRegistrationStart().isBefore(OffsetDateTime.now()) && t.getRegistrationEnd().isAfter(OffsetDateTime.now());
    private static final Predicate<TournamentDefinition> PLAY_OPEN = t -> t.getPlayStarts().isBefore(OffsetDateTime.now()) && t.getPlayEnds().plusMonths(1).isAfter(OffsetDateTime.now());
    private static final Predicate<TournamentDefinition> IS_STARTING = t -> t.getStatus().equals(GameStatus.STARTING);
    private static final Predicate<TournamentDefinition> IS_ACTIVE = t -> t.getStatus().equals(GameStatus.ACTIVE);
    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);
    private static final Path PERSISTENCE_PATH = DataPaths.path("tournaments.json");
    private static final TournamentService INSTANCE = new TournamentService();
    private final Map<String, TournamentDefinition> tournaments = new HashMap<>();

    private TournamentService() {
        super("TournamentService", 10);
        load();
    }

    public static List<TournamentMetadata> getOpenTournaments() {
        return INSTANCE.tournaments.values().stream()
                .filter(REGISTRATIONS_OPEN)
                .filter(IS_STARTING)
                .map(TournamentMetadata::new)
                .toList();
    }

    public static List<TournamentMetadata> getOpenTournaments(String playerName) {
        return INSTANCE.tournaments.values().stream()
                .filter(REGISTRATIONS_OPEN)
                .filter(IS_STARTING)
                .map(t -> new TournamentMetadata(t, playerName))
                .toList();
    }

    public static List<TournamentMetadata> getActiveTournaments() {
        return INSTANCE.tournaments.values().stream()
                .filter(PLAY_OPEN)
                .filter(IS_ACTIVE)
                .map(TournamentMetadata::new)
                .toList();
    }
    private static TournamentMetadata getActiveTournament(String tourName) {
        return INSTANCE.tournaments.values().stream()
                .filter(PLAY_OPEN)
                .filter(IS_ACTIVE)
                .filter(t -> t.getName().equals(tourName))
                .map(TournamentMetadata::new)
                .findFirst().get();
    }

    public static List<TournamentMetadata> getTournamentsReadyToStart() {
        return INSTANCE.tournaments.values().stream()
                .filter(PLAY_OPEN)
                .filter(IS_STARTING)
                .map(TournamentMetadata::new)
                .toList();
    }

    public static TournamentMetadata getTournamentReadyToStart(String tourName) {
        return INSTANCE.tournaments.values().stream()
                .filter(IS_STARTING)
                .filter(t -> t.getName().equals(tourName))
                .map(TournamentMetadata::new)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No STARTING tournament found with name: " + tourName));
    }

    public static List<TournamentMetadata> getTournamentsStarting() {
        return INSTANCE.tournaments.values().stream()
                .filter(t-> t.getStatus().equals(GameStatus.STARTING))
                .map(TournamentMetadata::new)
                .toList();
    }

    public static List<TournamentMetadata> getTournamentsWithStatus(List<GameStatus> status) {
        return INSTANCE.tournaments.values().stream()
                .filter(t-> status.contains(t.getStatus()))
                .map(TournamentMetadata::new)
                .toList();
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    public static void joinTournament(String game, String playerName, String vekn) {
        TournamentDefinition def = INSTANCE.tournaments.get(game);
        if (def.isOpenForRegistration()) {
            TournamentRegistration registration = def.getRegistration(playerName).orElseGet(() -> new TournamentRegistration(playerName, vekn));
            def.getRegistrations().add(registration);
        }
    }

    public static void leaveTournament(String tournament, String playerName) {
        TournamentDefinition def = INSTANCE.tournaments.get(tournament);
        Optional<TournamentRegistration> registration = def.getRegistration(playerName);
        if (def.isOpenForRegistration()) {
            registration.ifPresent(reg -> {
                // clean up deck file
                Path deckPath = DataPaths.path("tournaments", def.getId(), reg.getDeck() + ".json");
                try {
                    Files.delete(deckPath);
                } catch (IOException e) {
                    logger.error("Unable to delete deck file");
                }
                def.getRegistrations().remove(reg);
            });
        }
    }

    public static void clearRegistrations(String tournamentName) {
        TournamentDefinition def = INSTANCE.tournaments.get(tournamentName);
        if (def == null) return;
        def.getRegistrations().forEach(reg -> {
            if (reg.getDeck() != null) {
                Path deckPath = DataPaths.path("tournaments", def.getId(), reg.getDeck() + ".json");
                try {
                    Files.delete(deckPath);
                } catch (IOException e) {
                    logger.error("Unable to delete deck file for player {} in tournament {}", reg.getPlayer(), tournamentName);
                }
            }
        });
        def.getRegistrations().clear();
    }

    public static List<TournamentMetadata> getFinalsInvites(String playerName) {
        return INSTANCE.tournaments.values().stream()
                .filter(IS_ACTIVE)
                .filter(t -> t.getFinals().getSeeding().contains(playerName))
                .map(TournamentMetadata::new)
                .toList();
    }

    public static List<TournamentInviteStatus> getRegisteredTournaments(String playerName) {
        return INSTANCE.tournaments.values().stream()
                .filter(REGISTRATIONS_OPEN)
                .filter(t -> CONTAINS_PLAYER.apply(t, playerName))
                .map(t -> new TournamentInviteStatus(t, playerName))
                .toList();
    }

    public static void registerDeck(String tournament, String player, ExtendedDeck deck) {
        TournamentDefinition definition = INSTANCE.tournaments.get(tournament);
        definition.getRegistration(player).ifPresent(reg -> {
            String deckId = UUID.randomUUID().toString();
            try {
                Path deckPath = DataPaths.path("tournaments", definition.getId(), deckId + ".json");
                Files.createDirectories(deckPath.getParent());
                objectMapper.writeValue(deckPath.toFile(), deck);
                reg.setDeck(deckId);
            } catch (IOException e) {
                logger.error("Unable to save deck {}", deckId, e);
            }
        });
    }

    public static List<TournamentPlayer> getPlayers(String tournament, int round, int table) {
        return INSTANCE.tournaments.get(tournament).getPlayers(round, table);
    }

    public static Optional<TournamentRegistration> getRegistrations(String tournament, String player) {
        return INSTANCE.tournaments.get(tournament).getRegistration(player);
    }

    public static List<TournamentRegistration> getRegistrations(String tournament) {
        return Optional.ofNullable(INSTANCE.tournaments.get(tournament))
                .map(TournamentDefinition::getRegistrations)
                .orElse(new HashSet<>())
                .stream().sorted(Comparator.comparing(TournamentRegistration::getPlayer, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    public static ExtendedDeck getTournamentDeck(String name, String deckId) {
        TournamentDefinition definition = INSTANCE.tournaments.get(name);
        Path deckPath = DataPaths.path("tournaments", definition.getId(), deckId + ".json");
        try {
            return objectMapper.readValue(deckPath.toFile(), ExtendedDeck.class);
        } catch (IOException e) {
            logger.error("Unable to read tournament deck {} {}", name, deckId);
            return null;
        }
    }

    public static List<CardSimple> getRandomCrypt(String tourName, String deck) {
        List<CardSimple> cryptlist = new ArrayList<>();
        getTournamentDeck(tourName, deck).getDeck().getCrypt().getCards().forEach(cardCount -> cryptlist.addAll(Collections.nCopies(cardCount.getCount(), new CardSimple(String.valueOf(cardCount.getId()), cardCount.getName(), null, null))));
        Collections.shuffle(cryptlist);
        return cryptlist.stream().limit(3).collect(Collectors.toList());
    }
    public static int getCryptCount(String tourName, String deck) {
        List<String> cryptlist = new ArrayList<>();
        getTournamentDeck(tourName, deck).getDeck().getCrypt().getCards().forEach(cardCount -> cryptlist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId()))));
        return cryptlist.size();
    }

    public static void startTournament(String tournamentName) {
        INSTANCE.tournaments.get(tournamentName).setStatus(GameStatus.ACTIVE);
    }

    public static void setReadyToStart(String tournamentName) {
        INSTANCE.tournaments.get(tournamentName).setStatus(GameStatus.STARTING);
    }

    public static void setTournamentStatus(String tournamentName, GameStatus status) {
        INSTANCE.tournaments.get(tournamentName).setStatus(status);
    }

    public static void createTournament(TournamentDefinition tournamentDefinition) {
        INSTANCE.tournaments.put(tournamentDefinition.getName(), tournamentDefinition);
    }

    public static void removeTournament(String name) {
        INSTANCE.tournaments.remove(name);
    }

    public static TournamentDefinition getTournament(String nameOfTournament) {
        return INSTANCE.tournaments.get(nameOfTournament);
    }

    public static void importRoundsFromCsv(String tourName, String csvData) throws IOException {
        TournamentDefinition tournament = INSTANCE.tournaments.get(tourName);
        if (tournament == null) return;

        String cleaned = sanitizeCsvData(csvData);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Round", "Table", "Player")
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        Map<Integer, Map<Integer, List<TournamentPlayer>>> rounds = new HashMap<>();
        try (CSVParser parser = CSVParser.parse(new StringReader(cleaned), format)) {
            for (CSVRecord record : parser) {
                int round = parseIntColumn(record, "Round");
                int table = parseIntColumn(record, "Table");
                String playerName = record.get("Player");
                if (playerName == null || playerName.isEmpty()) continue;

                TournamentPlayer tp = new TournamentPlayer();
                tp.setName(playerName);
                rounds
                    .computeIfAbsent(round, r -> new HashMap<>())
                    .computeIfAbsent(table, t -> new ArrayList<>())
                    .add(tp);
            }
        }

        // Only clear out already-created games once the whole CSV has parsed
        // successfully, so a bad import leaves prior tournament state untouched.
        if (tournament.getStatus() == GameStatus.STARTING) {
            clearTournamentGames(tourName);
        }
        tournament.setRounds(rounds);
        INSTANCE.persist();
    }

    private static String sanitizeCsvData(String csvData) {
        String cleaned = StringUtils.stripStart(csvData, "﻿");
        return cleaned
                .replace('“', '"').replace('”', '"')
                .replace('‘', '\'').replace('’', '\'');
    }

    private static int parseIntColumn(CSVRecord record, String column) throws IOException {
        String raw = record.get(column);
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IOException(String.format(
                    "Invalid CSV data at record %d, column '%s': value '%s' is not a valid integer",
                    record.getRecordNumber(), column, raw));
        }
    }

    private static void clearTournamentGames(String tournamentName) {
        List<GameInfo> games = GameService.getGamesByTournament(tournamentName);
        for (GameInfo game : games) {
            GameService.remove(game.getName(), game.getId());
            RegistrationService.clearRegistrations(game.getName());
        }
        if (!games.isEmpty()) {
            logger.info("Cleared {} stale game(s) for tournament '{}' during round re-import", games.size(), tournamentName);
        }
    }

    public static void save() {
        INSTANCE.persist();
    }

    /**
     * Checks every player seated in any round/table against their tournament deck registration,
     * without creating anything. Used to fail fast (and name every affected player at once)
     * before {@link #createTournamentTables} starts creating games.
     * <p>
     * A {@code SINGLE_DECK} tournament uses one registration for the whole event, so a missing
     * deck fails identically in every round - each player is reported once, with no round number.
     * A {@code MULTI_DECK} tournament reports each round separately, since which round is affected
     * is meaningful information for the admin fixing it.
     */
    private static List<String> validateTournamentPlayerDecks(TournamentMetadata tournament) {
        String tournamentName = tournament.getName();
        boolean singleDeck = isSingleDeck(tournamentName);

        List<String> issues = new ArrayList<>();
        Set<String> checked = new LinkedHashSet<>();
        for (int round = 1; round <= tournament.getNumberOfRounds(); round++) {
            for (int table = 1; table <= tournament.getNumberOfTables(); table++) {
                List<TournamentPlayer> players = getPlayers(tournamentName, round, table);
                if (players == null) continue;
                int currentRound = round;
                for (TournamentPlayer player : players) {
                    String playerName = player.getName();
                    String dedupeKey = singleDeck ? playerName : currentRound + ":" + playerName;
                    if (!checked.add(dedupeKey)) continue;

                    describePlayerDeckIssue(tournamentName, playerName).ifPresent(reason -> issues.add(
                            singleDeck
                                    ? "%s (%s)".formatted(playerName, reason)
                                    : "Round %d: %s (%s)".formatted(currentRound, playerName, reason)));
                }
            }
        }
        return issues;
    }

    private static boolean isSingleDeck(String tournamentName) {
        TournamentDefinition definition = INSTANCE.tournaments.get(tournamentName);
        return definition == null || definition.getFormat() == TournamentFormat.SINGLE_DECK;
    }

    private static Optional<String> describePlayerDeckIssue(String tournamentName, String playerName) {
        Optional<TournamentRegistration> registration = getRegistrations(tournamentName, playerName);
        if (registration.isEmpty()) {
            return Optional.of("no registration found");
        }

        String deckId = registration.get().getDeck();
        if (deckId == null || deckId.isBlank()) {
            return Optional.of("no deck selected");
        }

        ExtendedDeck deck = getTournamentDeck(tournamentName, deckId);
        if (deck == null) {
            return Optional.of("deck file '%s' missing or unreadable".formatted(deckId));
        }
        if (deck.getDeck() == null) {
            return Optional.of("deck '%s' has no deck data".formatted(deckId));
        }
        if (deck.getStats() == null || deck.getStats().getSummary() == null) {
            return Optional.of("deck '%s' has no stats summary".formatted(deckId));
        }
        return Optional.empty();
    }

    public static void createTournamentTables(String tourName) {
        // Start tournaments
        TournamentMetadata tournament = getTournamentReadyToStart(tourName);

        // dont setup the tournament games if no rounds have been created
        if (!tournament.isRoundsConfig()) {
            return;
        }

        String tournamentName = tournament.getName();

        List<String> deckIssues = validateTournamentPlayerDecks(tournament);
        if (!deckIssues.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot create tournament tables for '%s'. The following players have deck issues: %s"
                            .formatted(tournamentName, String.join("; ", deckIssues)));
        }

        for (int round = 1; round <= tournament.getNumberOfRounds(); round++) {
            for (int table = 1; table <= tournament.getNumberOfTables(); table++) {
                List<TournamentPlayer> players = getPlayers(tournamentName, round, table);
                if (players == null || players.isEmpty()) {
                    logger.warn("Skipping tournament table creation for tournament '{}', round {}, table {} because no players were found",
                            tournamentName, round, table);
                    continue;
                }
                createTable(tournament, round, table, players);
            }
        }

        // Start tournament
        startTournament(tournamentName);
    }

    /**
     * Creates (or recreates) the game for a single round/table: a fresh {@code JolGame} is built,
     * registered, seeded with each player's tournament deck, and started. Shared by
     * {@link #createTournamentTables} (bulk creation while STARTING) and {@link #recreateTable}
     * (single-table rebuild on an ACTIVE tournament).
     */
    private static void createTable(TournamentMetadata tournament, int round, int table, List<TournamentPlayer> players) {
        String tournamentName = tournament.getName();
        String gameName = String.format("%s: Round %d - Table %d", tournamentName, round, table);
        String gameId = UUID.randomUUID().toString();

        // Create Game
        GameService.create(gameName, gameId, "SYSTEM", Visibility.PUBLIC, GameFormat.from(tournament.getDeckFormat()));
        // Link to tournament immediately so a partial failure below still leaves this
        // game discoverable (and thus clearable) by clearTournamentGames on re-import.
        GameService.get(gameName).setTournamentName(tournamentName);
        JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));

        for (TournamentPlayer player : players) {
            String playerName = player.getName();
            TournamentRegistration registration = getRegistrations(tournamentName, playerName).orElseThrow();
            String deckId = registration.getDeck();
            ExtendedDeck deck = getTournamentDeck(tournamentName, deckId);

            // Create Registration
            RegistrationService.registerDeck(gameName, playerName, deckId, deck.getDeck().getName(), deck.getStats().getSummary());

            // Add player and deck
            jolGame.addPlayer(playerName, deck.getDeck());

            // Copy deck into game folder
            Path gameDeckPath = DataPaths.path("games", gameId, deckId + ".json");
            Path tournamentDeckPath = DataPaths.path("tournaments", tournament.getId(), deckId + ".json");

            if (!Files.exists(gameDeckPath)) {
                try {
                    Files.copy(tournamentDeckPath, gameDeckPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    logger.error("Unable to copy tournament deck file '{}' to game deck file '{}'",
                            tournamentDeckPath, gameDeckPath, e);
                }
            }
        }

        // Set order and start
        jolGame.startGame(players.stream().map(TournamentPlayer::getName).toList());
        String playerName = players.getFirst().getName();
        PlayerGameActivityService.pingPlayer(playerName, gameName);

        // Save game
        GameService.saveGame(jolGame);

        // Update status
        GameService.get(gameName).setStatus(GameStatus.ACTIVE);
    }

    /**
     * Destructively rebuilds a single round/table on an ACTIVE tournament: deletes the existing
     * game (and its data files/registrations) for that table, overwrites just that table's seating,
     * and creates a brand new game from the supplied CSV. All other rounds/tables are untouched.
     * <p>
     * This is irreversible - callers (the admin UI) are expected to gate it behind a strong,
     * explicit confirmation before invoking it.
     */
    public static void recreateTable(String tourName, int round, int table, String csvData) throws IOException {
        TournamentDefinition definition = INSTANCE.tournaments.get(tourName);
        if (definition == null) {
            throw new IllegalStateException("No tournament found with name: " + tourName);
        }
        if (definition.getStatus() != GameStatus.ACTIVE) {
            throw new IllegalStateException("Tournament '%s' is not ACTIVE - cannot recreate a table".formatted(tourName));
        }

        List<TournamentPlayer> players = parseSingleTableCsv(csvData, round, table);
        if (players.isEmpty()) {
            throw new IllegalStateException("No players found in CSV data for round %d, table %d".formatted(round, table));
        }

        Set<String> newPlayerNames = new LinkedHashSet<>();
        for (TournamentPlayer player : players) {
            if (!newPlayerNames.add(player.getName())) {
                throw new IllegalStateException("Player '%s' appears more than once in the new table".formatted(player.getName()));
            }
        }

        // Reject if any of the new players are already seated at a different table in the same round
        Map<Integer, List<TournamentPlayer>> roundTables = definition.getRounds().getOrDefault(round, Map.of());
        List<String> conflicts = new ArrayList<>();
        roundTables.forEach((otherTable, seated) -> {
            if (otherTable.equals(table)) return;
            seated.forEach(tp -> {
                if (newPlayerNames.contains(tp.getName())) {
                    conflicts.add("%s (table %d)".formatted(tp.getName(), otherTable));
                }
            });
        });
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot recreate round %d, table %d: player(s) already seated elsewhere in this round: %s"
                            .formatted(round, table, String.join(", ", conflicts)));
        }

        List<String> deckIssues = new ArrayList<>();
        for (TournamentPlayer player : players) {
            describePlayerDeckIssue(tourName, player.getName())
                    .ifPresent(reason -> deckIssues.add("%s (%s)".formatted(player.getName(), reason)));
        }
        if (!deckIssues.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot recreate round %d, table %d for '%s'. The following players have deck issues: %s"
                            .formatted(round, table, tourName, String.join("; ", deckIssues)));
        }

        // Delete the existing game for this table, if any - data files and registrations included
        String gameName = String.format("%s: Round %d - Table %d", tourName, round, table);
        GameInfo existing = GameService.get(gameName);
        if (existing != null) {
            GameService.remove(gameName, existing.getId());
        }
        RegistrationService.clearRegistrations(gameName);

        // Overwrite just this round/table's seating - every other round/table is untouched
        definition.setTable(round, table, players);

        // Recreate the game
        createTable(new TournamentMetadata(definition), round, table, players);

        INSTANCE.persist();
    }

    private static List<TournamentPlayer> parseSingleTableCsv(String csvData, int round, int table) throws IOException {
        String cleaned = sanitizeCsvData(csvData);

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Round", "Table", "Player")
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        List<TournamentPlayer> players = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse(new StringReader(cleaned), format)) {
            for (CSVRecord record : parser) {
                int csvRound = parseIntColumn(record, "Round");
                int csvTable = parseIntColumn(record, "Table");
                if (csvRound != round || csvTable != table) {
                    throw new IOException(String.format(
                            "CSV row %d is for Round %d, Table %d but this action only recreates Round %d, Table %d",
                            record.getRecordNumber(), csvRound, csvTable, round, table));
                }

                String playerName = record.get("Player");
                if (playerName == null || playerName.isEmpty()) continue;

                TournamentPlayer tp = new TournamentPlayer();
                tp.setName(playerName);
                players.add(tp);
            }
        }
        return players;
    }

    public static void createFinal(String tourName) {
        // Start tournaments
        TournamentMetadata tournament = getActiveTournament(tourName);
        String tournamentName = tournament.getName();
        List<String> seeding = tournament.getFinalsSeeding();
        String gameName = String.format("%s: Final Table", tournamentName);
        GameInfo gameInfo = GameService.get(gameName);
        if (gameInfo == null && !seeding.isEmpty()) {
            String gameId = UUID.randomUUID().toString();
            GameService.create(gameName, gameId, "SYSTEM", Visibility.PUBLIC, GameFormat.from(tournament.getDeckFormat()));
            // Link to tournament immediately so a partial failure below still leaves this
            // game discoverable (and thus clearable) by clearTournamentGames on re-import.
            GameService.get(gameName).setTournamentName(tournamentName);
            JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));
            for (String playerName : seeding) {
                String deckId = getRegistrations(tournamentName, playerName).map(TournamentRegistration::getDeck).orElseThrow();
                ExtendedDeck deck = getTournamentDeck(tournamentName, deckId);
                assert deck != null;
                // Create Registration
                RegistrationService.registerDeck(gameName, playerName, deckId, deck.getDeck().getName(), deck.getStats().getSummary());
                // Add player and deck
                jolGame.addPlayer(playerName, deck.getDeck());
                NotificationService.pingPlayer(playerName, null, gameName);
            }
            // Set order and start
            jolGame.startGame(seeding);
            // Save game
            GameService.saveGame(jolGame);
            // Update status
            GameService.get(gameName).setStatus(GameStatus.ACTIVE);
            GlobalChatService.chat("SYSTEM", String.format("Game %s started", gameName));
        }
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting {} tournaments", tournaments.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), tournaments.values());
            logger.debug("Successfully persisted tournaments");
        } catch (IOException e) {
            logger.error("Unable to save tournaments");
        }
    }

    @Override
    protected void load() {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing tournaments file found");
            return;
        }

        try {
            CollectionType collectionType = typeFactory.constructCollectionType(List.class, TournamentDefinition.class);
            List<TournamentDefinition> tournamentsList = objectMapper.readValue(PERSISTENCE_PATH.toFile(), collectionType);
            tournamentsList.forEach(t -> tournaments.put(t.getName(), t));
            logger.info("Loaded {} tournaments", tournamentsList.size());
        } catch (IOException e) {
            logger.error("Unable to load tournaments", e);
        }
    }
}
