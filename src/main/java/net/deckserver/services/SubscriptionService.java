package net.deckserver.services;

import com.fasterxml.jackson.core.type.TypeReference;
import net.deckserver.push.Subscription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionService extends PersistedService {

    private static final Path PERSISTENCE_PATH = DataPaths.path("subscriptions.json");
    private static final SubscriptionService INSTANCE = new SubscriptionService();
    private final Map<String, List<Subscription>> subscriptions = new HashMap<>();

    private SubscriptionService() {
        super("SubscriptionService", 5);
        load();
    }

    public static synchronized void addSubscription(String playerName, Subscription subscription) {
        List<Subscription> playerSubscriptions = INSTANCE.subscriptions.computeIfAbsent(playerName, name -> new ArrayList<>());
        playerSubscriptions.removeIf(existing -> existing.getEndpoint().equals(subscription.getEndpoint()));
        playerSubscriptions.add(subscription);
    }

    public static synchronized void removeSubscription(String playerName, String endpoint) {
        List<Subscription> playerSubscriptions = INSTANCE.subscriptions.get(playerName);
        if (playerSubscriptions != null) {
            playerSubscriptions.removeIf(existing -> existing.getEndpoint().equals(endpoint));
        }
    }

    public static synchronized List<Subscription> getSubscriptions(String playerName) {
        return List.copyOf(INSTANCE.subscriptions.getOrDefault(playerName, List.of()));
    }

    public static synchronized boolean hasSubscriptions(String playerName) {
        return !INSTANCE.subscriptions.getOrDefault(playerName, List.of()).isEmpty();
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected synchronized void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            logger.debug("Persisting subscriptions for {} players", subscriptions.size());
            objectMapper.writeValue(PERSISTENCE_PATH.toFile(), subscriptions);
            logger.debug("Successfully persisted subscription data");
        } catch (IOException e) {
            logger.error("Unable to save subscription data", e);
        }
    }

    @Override
    protected void load() {
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing subscription file found");
            return;
        }

        try {
            Map<String, List<Subscription>> loaded = objectMapper.readValue(PERSISTENCE_PATH.toFile(), new TypeReference<>() {
            });
            subscriptions.putAll(loaded);
            logger.info("Loaded subscriptions for {} players", subscriptions.size());
        } catch (IOException e) {
            logger.error("Unable to load subscriptions.", e);
        }
    }
}
