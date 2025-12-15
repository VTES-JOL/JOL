package net.deckserver.services;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.deckserver.game.enums.GameStatus;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class TournamentService extends PersistedService {

    private static final BiFunction<TournamentDefinition, String, Boolean> CONTAINS_PLAYER = (tournament, player) -> tournament.getRegistrations().stream().map(TournamentRegistration::getPlayer).toList().contains(player);
    private static final Predicate<TournamentDefinition> REGISTRATIONS_OPEN = t -> t.getRegistrationStart().isBefore(OffsetDateTime.now()) && t.getRegistrationEnd().isAfter(OffsetDateTime.now());
    private static final Predicate<TournamentDefinition> PLAY_OPEN = t -> t.getPlayStarts().isBefore(OffsetDateTime.now()) && t.getPlayEnds().isAfter(OffsetDateTime.now());
    private static final Predicate<TournamentDefinition> IS_STARTING = t -> t.getStatus().equals(GameStatus.STARTING);
    private static final Predicate<TournamentDefinition> IS_ACTIVE = t -> t.getStatus().equals(GameStatus.ACTIVE);
    private static final Logger logger = LoggerFactory.getLogger(TournamentService.class);
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "tournaments.json");
    private static final TournamentService INSTANCE = new TournamentService();
    private final Map<String, TournamentDefinition> tournaments = new HashMap<>();

    private TournamentService() {
        super("TournamentService", 10);
        load();
    }

    public static List<TournamentMetadata> getOpenTournaments() {
        return INSTANCE.tournaments.values().stream()
                .filter(REGISTRATIONS_OPEN)
                .map(TournamentMetadata::new)
                .toList();
    }

    public static List<TournamentMetadata> getOpenTournaments(String playerName) {
        return INSTANCE.tournaments.values().stream()
                .filter(REGISTRATIONS_OPEN)
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

    public static List<TournamentMetadata> getTournamentsReadyToStart() {
        return INSTANCE.tournaments.values().stream()
                .filter(PLAY_OPEN)
                .filter(IS_STARTING)
                .map(TournamentMetadata::new)
                .toList();
    }

    public static TournamentMetadata getMetadata(String tournamentName) {
        return Optional.ofNullable(INSTANCE.tournaments.get(tournamentName))
                .map(TournamentMetadata::new)
                .orElseThrow(() -> new IllegalArgumentException("No such tournament: " + tournamentName));
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
                Path deckPath = Paths.get(System.getenv("JOL_DATA"), "tournaments", def.getId(), reg.getDeck() + ".json");
                try {
                    Files.delete(deckPath);
                } catch (IOException e) {
                    logger.error("Unable to delete deck file");
                }
                def.getRegistrations().remove(reg);
            });
        }
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
                Path deckPath = Paths.get(System.getenv("JOL_DATA"), "tournaments", definition.getId(), deckId + ".json");
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

    public static ExtendedDeck getTournamentDeck(String name, String deckId) {
        TournamentDefinition definition = INSTANCE.tournaments.get(name);
        Path deckPath = Paths.get(System.getenv("JOL_DATA"), "tournaments", definition.getId(), deckId + ".json");
        try {
            return objectMapper.readValue(deckPath.toFile(), ExtendedDeck.class);
        } catch (IOException e) {
            logger.error("Unable to read tournament deck");
            return null;
        }
    }

    public static void startTournament(String tournamentName) {
        INSTANCE.tournaments.get(tournamentName).setStatus(GameStatus.ACTIVE);
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
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
