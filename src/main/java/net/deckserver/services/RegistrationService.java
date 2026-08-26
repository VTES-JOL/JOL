package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.RegistrationRepository;
import net.deckserver.storage.json.game.RegistrationSummary;
import net.deckserver.storage.json.system.RegistrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Singleton
@Startup
public class RegistrationService extends PersistedService {

    private static final Predicate<RegistrationStatus> IS_REGISTERED = status -> status.getDeckId() != null;
    private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);
    private static final RegistrationRepository registrationRepository = new RegistrationRepository();
    private static RegistrationService instance() {
        return resolve(RegistrationService.class, RegistrationService::new);
    }
    private final Table<String, String, RegistrationStatus> registrations = HashBasedTable.create();
    private final LoadingCache<String, RegistrationSummary> summaryMap = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .refreshAfterWrite(30, TimeUnit.SECONDS)
            .build(RegistrationService::generateSummary);

    RegistrationService() {
        super("RegistrationService", 0);
    }

    public static synchronized void put(String gameName, String playerName, RegistrationStatus registration) {
        instance().jpaWriteThenMutate(
                em -> registrationRepository.save(em, gameName, playerName, registration),
                () -> instance().registrations.put(gameName, playerName, registration));
    }

    public static synchronized long getRegisteredPlayerCount(String gameName) {
        return instance().registrations.row(gameName).values().stream().filter(IS_REGISTERED).count();
    }

    public static synchronized RegistrationStatus getRegistration(String gameName, String playerName) {
        return instance().registrations.get(gameName, playerName);
    }

    public static synchronized Set<String> getRegisteredGames(String playerName) {
        return new HashSet<>(instance().registrations.column(playerName).keySet());
    }

    public static synchronized Set<String> getPlayers(String gameName) {
        return new HashSet<>(instance().registrations.row(gameName).keySet());
    }

    public static synchronized Set<String> getRegisteredGameNames() {
        return new HashSet<>(instance().registrations.rowKeySet());
    }

    public static synchronized void removePlayer(String gameName, String playerName) {
        instance().jpaWriteThenMutate(
                em -> registrationRepository.delete(em, gameName, playerName),
                () -> instance().registrations.remove(gameName, playerName));
    }

    public static synchronized boolean isInGame(String gameName, String playerName) {
        return instance().registrations.contains(gameName, playerName);
    }

    public static synchronized boolean isRegistered(String gameName, String playerName) {
        return instance().registrations.contains(gameName, playerName) && Objects.requireNonNull(instance().registrations.get(gameName, playerName)).getDeckId() != null;
    }

    public static synchronized boolean isInvited(String gameName, String playerName) {
        return instance().registrations.contains(gameName, playerName);
    }

    public static synchronized void clearRegistrations(String gameName) {
        instance().jpaWriteThenMutate(
                em -> registrationRepository.deleteAllForGame(em, gameName),
                () -> instance().registrations.row(gameName).clear());
    }

    public static synchronized Map<String, RegistrationStatus> getPlayerRegistrations(String playerName) {
        return Map.copyOf(instance().registrations.column(playerName));
    }

    public static synchronized Map<String, RegistrationStatus> getGameRegistrations(String gameName) {
        return Map.copyOf(instance().registrations.row(gameName));
    }

    public static synchronized Set<String> getPlayerGames(String player) {
        return getPlayerRegistrations(player).keySet();
    }

    public static synchronized void invitePlayer(String gameName, String playerName) {
        if (!RegistrationService.isInvited(gameName, playerName)) {
            RegistrationStatus status = new RegistrationStatus(OffsetDateTime.now());
            instance().jpaWriteThenMutate(
                    em -> registrationRepository.save(em, gameName, playerName, status),
                    () -> instance().registrations.put(gameName, playerName, status));
        }
    }

    public static synchronized void registerDeck(String gameName, String playerName, String deckId, String deckName, String summary, String deckContent) {
        RegistrationStatus registrationStatus = new RegistrationStatus(deckId);
        registrationStatus.setSummary(summary);
        registrationStatus.setDeckName(deckName);
        registrationStatus.setDeckContent(deckContent);
        instance().jpaWriteThenMutate(
                em -> registrationRepository.save(em, gameName, playerName, registrationStatus),
                () -> instance().registrations.put(gameName, playerName, registrationStatus));
    }

    public static RegistrationSummary getSummary(String gameName) {
        return instance().summaryMap.get(gameName);
    }

    private static RegistrationSummary generateSummary(String gameName) {
        RegistrationSummary summary = new RegistrationSummary();
        summary.setName(gameName);
        getGameRegistrations(gameName).forEach((key, value) -> summary.getPlayers().put(key, value.getSummary()));
        OffsetDateTime timestamp = OffsetDateTime.now().minusDays(5);
        timestamp = getGameRegistrations(gameName).values().stream().map(RegistrationStatus::getTimestamp)
                .filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(timestamp);
        summary.setTimestamp(timestamp);
        return summary;
    }

    public static PersistedService getInstance() {
        return instance();
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
