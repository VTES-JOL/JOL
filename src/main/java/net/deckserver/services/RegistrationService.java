package net.deckserver.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.RegistrationRepository;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.game.RegistrationSummary;
import net.deckserver.storage.json.system.RegistrationStatus;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class RegistrationService extends PersistedService {

    private static final Predicate<RegistrationStatus> IS_REGISTERED = status -> status.getDeckId() != null;
    private static final RegistrationRepository registrationRepository = new RegistrationRepository();
    private final static RegistrationService INSTANCE = new RegistrationService();
    private final Table<String, String, RegistrationStatus> registrations = HashBasedTable.create();
    private final LoadingCache<String, RegistrationSummary> summaryMap = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .refreshAfterWrite(30, TimeUnit.SECONDS)
            .build(RegistrationService::generateSummary);

    private RegistrationService() {
        super("RegistrationService", 0);
        load();
    }

    public static void put(String gameName, String playerName, RegistrationStatus registration) {
        INSTANCE.jpaWriteThenMutate(
                em -> registrationRepository.save(em, gameName, playerName, registration),
                () -> INSTANCE.registrations.put(gameName, playerName, registration));
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

    public static Set<String> getPlayers(String gameName) {
        return INSTANCE.registrations.row(gameName).keySet();
    }

    public static void removePlayer(String gameName, String playerName) {
        INSTANCE.jpaWriteThenMutate(
                em -> registrationRepository.delete(em, gameName, playerName),
                () -> INSTANCE.registrations.remove(gameName, playerName));
    }

    public static boolean isInGame(String gameName, String playerName) {
        return INSTANCE.registrations.contains(gameName, playerName);
    }

    public static boolean isRegistered(String gameName, String playerName) {
        return INSTANCE.registrations.contains(gameName, playerName) && Objects.requireNonNull(INSTANCE.registrations.get(gameName, playerName)).getDeckId() != null;
    }

    public static boolean isInvited(String gameName, String playerName) {
        return INSTANCE.registrations.contains(gameName, playerName);
    }

    public static synchronized void clearRegistrations(String gameName) {
        INSTANCE.jpaWriteThenMutate(
                em -> registrationRepository.deleteAllForGame(em, gameName),
                () -> INSTANCE.registrations.row(gameName).clear());
    }

    public static synchronized Map<String, RegistrationStatus> getPlayerRegistrations(String playerName) {
        return INSTANCE.registrations.column(playerName);
    }

    public static synchronized Map<String, RegistrationStatus> getGameRegistrations(String gameName) {
        return INSTANCE.registrations.row(gameName);
    }

    public static synchronized Set<String> getPlayerGames(String player) {
        return getPlayerRegistrations(player).keySet();
    }

    public static synchronized void invitePlayer(String gameName, String playerName) {
        if (!RegistrationService.isInvited(gameName, playerName)) {
            RegistrationStatus status = new RegistrationStatus(OffsetDateTime.now());
            INSTANCE.jpaWriteThenMutate(
                    em -> registrationRepository.save(em, gameName, playerName, status),
                    () -> INSTANCE.registrations.put(gameName, playerName, status));
        }
    }

    public static void registerDeck(String gameName, String playerName, String deckId, String deckName, String summary, ExtendedDeck deck) {
        RegistrationStatus registrationStatus = new RegistrationStatus(deckId);
        registrationStatus.setSummary(summary);
        registrationStatus.setDeckName(deckName);
        if (deck != null) {
            try {
                registrationStatus.setDeckContent(objectMapper.writeValueAsString(deck));
            } catch (JsonProcessingException e) {
                INSTANCE.logger.error("Failed to serialize deck content for registration {}/{}", gameName, playerName, e);
            }
        }
        INSTANCE.jpaWriteThenMutate(
                em -> registrationRepository.save(em, gameName, playerName, registrationStatus),
                () -> INSTANCE.registrations.put(gameName, playerName, registrationStatus));
    }

    public static RegistrationSummary getSummary(String gameName) {
        return INSTANCE.summaryMap.get(gameName);
    }

    private static RegistrationSummary generateSummary(String gameName) {
        RegistrationSummary summary = new RegistrationSummary();
        summary.setName(gameName);
        getGameRegistrations(gameName).forEach((key, value) -> summary.getPlayers().put(key, value.getSummary()));
        OffsetDateTime timestamp = OffsetDateTime.now().minusDays(5);
        timestamp = getGameRegistrations(gameName).values().stream().map(RegistrationStatus::getTimestamp).max(Comparator.naturalOrder()).orElse(timestamp);
        summary.setTimestamp(timestamp);
        return summary;
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        // all mutations are write-through; no background flush needed
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            registrationRepository.findAll(em).forEach(entity ->
                    registrations.put(entity.getGameName(), entity.getPlayerName(),
                            entity.toRegistrationStatus()));
            logger.info("Loaded {} registrations from JPA", registrations.size());
        } catch (Exception e) {
            logger.error("JPA load failed for RegistrationService", e);
        }
    }
}
