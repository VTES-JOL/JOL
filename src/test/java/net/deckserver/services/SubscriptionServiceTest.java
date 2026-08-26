package net.deckserver.services;

import net.deckserver.push.Subscription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class SubscriptionServiceTest {

    private static Subscription subscription(String endpoint) {
        Subscription sub = new Subscription();
        sub.setEndpoint(endpoint);
        sub.setAuth("auth-key");
        sub.setKey("p256dh-key");
        return sub;
    }

    @Test
    void addsAndRemovesASubscription() {
        assertThat(SubscriptionService.hasSubscriptions("Player1"), is(false));

        SubscriptionService.addSubscription("Player1", subscription("https://push.example/a"));
        assertThat(SubscriptionService.hasSubscriptions("Player1"), is(true));
        assertThat(SubscriptionService.getSubscriptions("Player1"), hasSize(1));

        SubscriptionService.removeSubscription("Player1", "https://push.example/a");
        assertThat(SubscriptionService.hasSubscriptions("Player1"), is(false));
    }

    @Test
    void removesSubscriptionAfterRepeatedFailures() {
        String endpoint = "https://push.example/b";
        SubscriptionService.addSubscription("Player2", subscription(endpoint));

        boolean removed = false;
        for (int i = 0; i < 5; i++) {
            removed = SubscriptionService.recordFailure("Player2", endpoint);
        }
        assertThat(removed, is(true));
        assertThat(SubscriptionService.getSubscriptions("Player2"), not(hasItem(hasProperty("endpoint", equalTo(endpoint)))));
    }

    @Test
    void recordSuccessResetsFailureCount() {
        String endpoint = "https://push.example/c";
        SubscriptionService.addSubscription("Player3", subscription(endpoint));

        SubscriptionService.recordFailure("Player3", endpoint);
        SubscriptionService.recordSuccess("Player3", endpoint);

        List<Subscription> subs = SubscriptionService.getSubscriptions("Player3");
        assertThat(subs.getFirst().getFailureCount(), equalTo(0));
    }
}
