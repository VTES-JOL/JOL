package net.deckserver.services;

import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.deckserver.storage.json.system.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class RegistrationService extends PersistedService {

    private static final Predicate<RegistrationStatus> IS_REGISTERED = status -> status.getDeckId() != null;
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private static final Path PERSISTENCE_PATH = Paths.get(System.getenv("JOL_DATA"), "registrations.json");
    private final static RegistrationService INSTANCE = new RegistrationService();
    private final Table<String, String, RegistrationStatus> registrations = HashBasedTable.create();

    private RegistrationService() {
        super("RegistrationService", 1);
        load();
    }

    public static void put(String gameName, String playerName, RegistrationStatus registration) {
        INSTANCE.registrations.put(gameName, playerName, registration);
    }

    public static RegistrationStatus get(String gameName, String playerName) {
        return INSTANCE.registrations.get(gameName, playerName);
    }

    public static long getRegisteredPlayerCount(String gameName) {
        return INSTANCE.registrations.row(gameName).values().stream().filter(IS_REGISTERED).count();
    }

    public static RegistrationStatus getRegistration(String gameName, String playerName) {
        return INSTANCE.registrations.get(gameName, playerName);
    }

    public static Set<String> getRegisteredGames(String playerName) {
        return INSTANCE.registrations.column(playerName).keySet();
    }

    public static synchronized Set<String> getPlayers(String gameName) {
        return INSTANCE.registrations.row(gameName).keySet();
    }

    public static void remove(String gameName, String playerName) {
        INSTANCE.registrations.remove(gameName, playerName);
    }

    public static synchronized boolean isInGame(String gameName, String playerName) {
        return INSTANCE.registrations.contains(gameName, playerName);
    }

    public static boolean isRegistered(String gameName, String playerName) {
        return INSTANCE.registrations.contains(gameName, playerName) && Objects.requireNonNull(INSTANCE.registrations.get(gameName, playerName)).getDeckId() != null;
    }

    public static void remove(String gameName) {
        INSTANCE.registrations.row(gameName).clear();
    }

    public static Map<String, RegistrationStatus> getPlayerRegistrations(String playerName) {
        return INSTANCE.registrations.column(playerName);
    }

    public static Map<String, RegistrationStatus> getGameRegistrations(String gameName) {
        return INSTANCE.registrations.row(gameName);
    }

    public static Set<String> getPlayerGames(String player) {
        return getPlayerRegistrations(player).keySet();
    }

    public static void invitePlayer(String gameName, String playerName) {
        INSTANCE.registrations.put(gameName, playerName, new RegistrationStatus(OffsetDateTime.now()));
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting {} registrations", registrations.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), registrations);
            logger.debug("Successfully persisted registrations");
        } catch (IOException e) {
            logger.error("Unable to save registrations", e);
        }
    }

    @Override
    protected void load() {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing registrations file found");
            return;
        }

        try {
            MapType registrationMapType = typeFactory.constructMapType(Map.class, String.class, RegistrationStatus.class);
            Map<String, Map<String, RegistrationStatus>> registrationsMap = objectMapper.readValue(PERSISTENCE_PATH.toFile(), typeFactory.constructMapType(ConcurrentHashMap.class, typeFactory.constructType(String.class), registrationMapType));
            registrationsMap.forEach((gameId, gameMap) -> {
                gameMap.forEach((playerId, registration) -> {
                    registrations.put(gameId, playerId, registration);
                });
            });
            logger.info("Loaded {} registrations", registrationsMap.size());
        } catch (IOException e) {
            logger.error("Unable to registrations", e);
        }

    }
}
