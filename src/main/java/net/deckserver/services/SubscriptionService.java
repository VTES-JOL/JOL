package net.deckserver.services;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.SubscriptionRepository;
import net.deckserver.push.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SubscriptionService extends PersistedService {

    private static final int MAX_CONSECUTIVE_FAILURES = 5;
    private static final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();
    private static final SubscriptionService INSTANCE = new SubscriptionService();
    private final Map<String, List<Subscription>> subscriptions = new HashMap<>();

    private SubscriptionService() {
        super("SubscriptionService", 0);
        load();
    }

    public static synchronized void addSubscription(String playerName, Subscription subscription) {
        INSTANCE.jpaWriteThenMutate(
                em -> subscriptionRepository.upsert(em, playerName, subscription),
                () -> {
                    List<Subscription> playerSubscriptions = INSTANCE.subscriptions.computeIfAbsent(playerName, name -> new ArrayList<>());
                    playerSubscriptions.removeIf(existing -> existing.getEndpoint().equals(subscription.getEndpoint()));
                    playerSubscriptions.add(subscription);
                });
    }

    public static synchronized void removeSubscription(String playerName, String endpoint) {
        INSTANCE.jpaWriteThenMutate(
                em -> subscriptionRepository.delete(em, playerName, endpoint),
                () -> {
                    List<Subscription> playerSubscriptions = INSTANCE.subscriptions.get(playerName);
                    if (playerSubscriptions != null) {
                        playerSubscriptions.removeIf(existing -> existing.getEndpoint().equals(endpoint));
                    }
                });
    }

    public static synchronized List<Subscription> getSubscriptions(String playerName) {
        return List.copyOf(INSTANCE.subscriptions.getOrDefault(playerName, List.of()));
    }

    public static synchronized boolean hasSubscriptions(String playerName) {
        return !INSTANCE.subscriptions.getOrDefault(playerName, List.of()).isEmpty();
    }

    public static synchronized void recordSuccess(String playerName, String endpoint) {
        findSubscription(playerName, endpoint).ifPresent(sub -> {
            int previousFailureCount = sub.getFailureCount();
            INSTANCE.jpaWriteWithRollback(
                    () -> sub.setFailureCount(0),
                    em -> subscriptionRepository.upsert(em, playerName, sub),
                    () -> sub.setFailureCount(previousFailureCount));
        });
    }

    /**
     * Records a failed send against the subscription. Once a subscription has failed
     * MAX_CONSECUTIVE_FAILURES times in a row (auth errors, timeouts, etc. that don't come
     * back as a clean 404/410), it's removed rather than retried forever.
     *
     * @return true if the subscription was removed as a result of this failure
     */
    public static synchronized boolean recordFailure(String playerName, String endpoint) {
        Optional<Subscription> found = findSubscription(playerName, endpoint);
        if (found.isEmpty()) return false;
        Subscription subscription = found.get();
        int previousFailureCount = subscription.getFailureCount();
        int updatedFailureCount = previousFailureCount + 1;
        if (updatedFailureCount >= MAX_CONSECUTIVE_FAILURES) {
            if (INSTANCE.jpaWriteThenMutate(
                    em -> subscriptionRepository.delete(em, playerName, endpoint),
                    () -> {
                        List<Subscription> playerSubscriptions = INSTANCE.subscriptions.get(playerName);
                        if (playerSubscriptions != null) {
                            playerSubscriptions.removeIf(existing -> existing.getEndpoint().equals(endpoint));
                        }
                    })) {
                return true;
            }
            subscription.setFailureCount(previousFailureCount);
            return false;
        }
        INSTANCE.jpaWriteWithRollback(
                () -> subscription.setFailureCount(updatedFailureCount),
                em -> subscriptionRepository.upsert(em, playerName, subscription),
                () -> subscription.setFailureCount(previousFailureCount));
        return false;
    }

    private static Optional<Subscription> findSubscription(String playerName, String endpoint) {
        return INSTANCE.subscriptions.getOrDefault(playerName, List.of()).stream()
                .filter(sub -> sub.getEndpoint().equals(endpoint))
                .findFirst();
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        // write-through only, see addSubscription()/removeSubscription()/recordSuccess()/recordFailure()
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            subscriptions.putAll(subscriptionRepository.findAll(em));
            logger.info("Loaded subscriptions for {} players from JPA", subscriptions.size());
        } catch (Exception e) {
            logger.error("JPA load failed for SubscriptionService", e);
        }
    }
}
