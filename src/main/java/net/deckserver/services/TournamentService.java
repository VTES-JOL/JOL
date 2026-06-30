package net.deckserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.EntityManager;
import net.deckserver.dwr.model.JolGame;
import net.deckserver.game.enums.GameFormat;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.game.enums.Visibility;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.entity.TournamentRegistrationEntity;
import net.deckserver.jpa.repository.TournamentRepository;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.CardSimple;
import net.deckserver.storage.json.game.GameData;
import net.deckserver.storage.json.system.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final TournamentRepository tournamentRepository = new TournamentRepository();
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
                .filter(t -> t.getStatus().equals(GameStatus.STARTING))
                .map(TournamentMetadata::new)
                .toList();
    }

    public static List<TournamentMetadata> getTournamentsWithStatus(List<GameStatus> status) {
        return INSTANCE.tournaments.values().stream()
                .filter(t -> status.contains(t.getStatus()))
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
            INSTANCE.jpaWrite(em -> tournamentRepository.save(em, def));
        }
    }

    public static void leaveTournament(String tournament, String playerName) {
        TournamentDefinition def = INSTANCE.tournaments.get(tournament);
        Optional<TournamentRegistration> registration = def.getRegistration(playerName);
        if (def.isOpenForRegistration()) {
            registration.ifPresent(reg -> {
                def.getRegistrations().remove(reg);
                INSTANCE.jpaWrite(em -> tournamentRepository.save(em, def));
            });
        }
    }

    public static void clearRegistrations(String tournamentName) {
        TournamentDefinition def = INSTANCE.tournaments.get(tournamentName);
        if (def == null) return;
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
            reg.setDeck(deckId);
            try {
                reg.setDeckContent(objectMapper.writeValueAsString(deck));
            } catch (JsonProcessingException e) {
                logger.error("Unable to serialize tournament deck {} for {}", deckId, player, e);
            }
            INSTANCE.jpaWrite(em -> tournamentRepository.save(em, definition));
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
        if (INSTANCE.testModeEnabled) {
            Path deckPath = DataPaths.path("tournaments", definition.getId(), deckId + ".json");
            try {
                return objectMapper.readValue(deckPath.toFile(), ExtendedDeck.class);
            } catch (IOException e) {
                logger.error("Unable to read tournament deck file {} {}", name, deckId);
                return null;
            }
        }
        return definition.getRegistrations().stream()
                .filter(r -> deckId.equals(r.getDeck()))
                .map(r -> {
                    try {
                        return objectMapper.readValue(r.getDeckContent(), ExtendedDeck.class);
                    } catch (Exception e) {
                        logger.error("Unable to deserialize tournament deck {} {}", name, deckId, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static List<CardSimple> getRandomCrypt(String tourName, String deck) {
        List<CardSimple> cryptlist = new ArrayList<>();
        getTournamentDeck(tourName, deck).getDeck().getCrypt().getCards().forEach(cardCount ->
                cryptlist.addAll(Collections.nCopies(cardCount.getCount(),
                        new CardSimple(String.valueOf(cardCount.getId()), cardCount.getName(), null, null))));
        Collections.shuffle(cryptlist);
        return cryptlist.stream().limit(3).collect(Collectors.toList());
    }

    public static int getCryptCount(String tourName, String deck) {
        List<String> cryptlist = new ArrayList<>();
        getTournamentDeck(tourName, deck).getDeck().getCrypt().getCards().forEach(cardCount ->
                cryptlist.addAll(Collections.nCopies(cardCount.getCount(), String.valueOf(cardCount.getId()))));
        return cryptlist.size();
    }

    public static void startTournament(String tournamentName) {
        TournamentDefinition def = INSTANCE.tournaments.get(tournamentName);
        def.setStatus(GameStatus.ACTIVE);
        INSTANCE.jpaWrite(em -> tournamentRepository.save(em, def));
    }

    public static void setReadyToStart(String tournamentName) {
        TournamentDefinition def = INSTANCE.tournaments.get(tournamentName);
        def.setStatus(GameStatus.STARTING);
        INSTANCE.jpaWrite(em -> tournamentRepository.save(em, def));
    }

    public static void setTournamentStatus(String tournamentName, GameStatus status) {
        TournamentDefinition def = INSTANCE.tournaments.get(tournamentName);
        def.setStatus(status);
        INSTANCE.jpaWrite(em -> tournamentRepository.save(em, def));
    }

    public static void createTournament(TournamentDefinition tournamentDefinition) {
        INSTANCE.tournaments.put(tournamentDefinition.getName(), tournamentDefinition);
        INSTANCE.jpaWrite(em -> tournamentRepository.save(em, tournamentDefinition));
    }

    public static void removeTournament(String name) {
        TournamentDefinition def = INSTANCE.tournaments.remove(name);
        if (def != null) {
            INSTANCE.jpaWrite(em -> tournamentRepository.delete(em, def.getId()));
        }
    }

    public static TournamentDefinition getTournament(String nameOfTournament) {
        return INSTANCE.tournaments.get(nameOfTournament);
    }

    public static void importRoundsFromCsv(String tourName, String csvData) throws IOException {
        TournamentDefinition tournament = INSTANCE.tournaments.get(tourName);
        if (tournament == null) return;

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Round", "Table", "Player")
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build();

        Map<Integer, Map<Integer, List<TournamentPlayer>>> rounds = new HashMap<>();
        try (CSVParser parser = CSVParser.parse(new StringReader(csvData), format)) {
            for (CSVRecord record : parser) {
                int round = Integer.parseInt(record.get("Round"));
                int table = Integer.parseInt(record.get("Table"));
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

        tournament.setRounds(rounds);
        INSTANCE.persist();
    }

    public static void save() {
        INSTANCE.persist();
    }

    public static void createTournamentTables(String tourName) {
        TournamentMetadata tournament = getTournamentReadyToStart(tourName);
        if (!tournament.isRoundsConfig())
            return;
        String tournamentName = tournament.getName();
        for (int round = 1; round <= tournament.getNumberOfRounds(); round++) {
            for (int table = 1; table <= tournament.getNumberOfTables(); table++) {
                String gameName = String.format("%s: Round %d - Table %d", tournamentName, round, table);
                String gameId = UUID.randomUUID().toString();
                GameService.create(gameName, gameId, "SYSTEM", Visibility.PUBLIC, GameFormat.from(tournament.getDeckFormat()));
                JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));
                List<TournamentPlayer> players = getPlayers(tournamentName, round, table);
                for (TournamentPlayer player : players) {
                    String playerName = player.getName();
                    TournamentRegistration registration = getRegistrations(tournamentName, playerName).orElseThrow();
                    String deckId = registration.getDeck();
                    ExtendedDeck deck = getTournamentDeck(tournamentName, deckId);
                    assert deck != null;
                    RegistrationService.registerDeck(gameName, playerName, deckId, deck.getDeck().getName(), deck.getStats().getSummary(), deck);
                    jolGame.addPlayer(playerName, deck.getDeck());
                }
                jolGame.startGame(players.stream().map(TournamentPlayer::getName).toList());
                String playerName = players.getFirst().getName();
                PlayerGameActivityService.pingPlayer(playerName, gameName);
                GameService.saveGame(jolGame);
                GameService.get(gameName).setStatus(GameStatus.ACTIVE);
                GameService.get(gameName).setTournamentName(tournamentName);
            }
        }
        startTournament(tournamentName);
    }

    public static void createFinal(String tourName) {
        TournamentMetadata tournament = getActiveTournament(tourName);
        String tournamentName = tournament.getName();
        List<String> seeding = tournament.getFinalsSeeding();
        String gameName = String.format("%s: Final Table", tournamentName);
        GameInfo gameInfo = GameService.get(gameName);
        if (gameInfo == null && !seeding.isEmpty()) {
            String gameId = UUID.randomUUID().toString();
            GameService.create(gameName, gameId, "SYSTEM", Visibility.PUBLIC, GameFormat.from(tournament.getDeckFormat()));
            JolGame jolGame = new JolGame(gameId, new GameData(gameId, gameName));
            for (String playerName : seeding) {
                String deckId = getRegistrations(tournamentName, playerName).map(TournamentRegistration::getDeck).orElseThrow();
                ExtendedDeck deck = getTournamentDeck(tournamentName, deckId);
                assert deck != null;
                RegistrationService.registerDeck(gameName, playerName, deckId, deck.getDeck().getName(), deck.getStats().getSummary(), deck);
                jolGame.addPlayer(playerName, deck.getDeck());
                NotificationService.pingPlayer(playerName, null, gameName);
            }
            jolGame.startGame(seeding);
            GameService.saveGame(jolGame);
            GameService.get(gameName).setStatus(GameStatus.ACTIVE);
            GameService.get(gameName).setTournamentName(tournamentName);
            GlobalChatService.chat("SYSTEM", String.format("Game %s started", gameName));
        }
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }
        logger.debug("Persisting {} tournaments via JPA", tournaments.size());
        jpaWrite(em -> tournaments.values().forEach(t -> tournamentRepository.save(em, t)));
    }

    @Override
    protected void load() {
        if (testModeEnabled) {
            loadFromFile();
            return;
        }
        try (EntityManager em = JpaFactory.createEntityManager()) {
            tournamentRepository.findAll(em).forEach(entity -> {
                List<TournamentRegistrationEntity> regs = tournamentRepository.findRegistrations(em, entity.getTournamentId());
                TournamentDefinition def = tournamentRepository.toDefinition(entity, regs);
                tournaments.put(def.getName(), def);
            });
            logger.info("Loaded {} tournaments from JPA", tournaments.size());
        } catch (Exception e) {
            logger.error("JPA load failed for TournamentService", e);
        }
    }

    private void loadFromFile() {
        Path path = DataPaths.path("tournaments.json");
        if (!Files.exists(path)) return;
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType collectionType = typeFactory.constructCollectionType(List.class, TournamentDefinition.class);
            List<TournamentDefinition> list = objectMapper.readValue(path.toFile(), collectionType);
            list.forEach(t -> tournaments.put(t.getName(), t));
            logger.info("Loaded {} tournaments from file", tournaments.size());
        } catch (IOException e) {
            logger.error("Unable to load tournaments from file", e);
        }
    }

    private void jpaWrite(java.util.function.Consumer<EntityManager> action) {
        if (testModeEnabled) return;
        try (EntityManager em = JpaFactory.createEntityManager()) {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("JPA write failed for TournamentService", e);
        }
    }
}
