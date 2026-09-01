package net.deckserver.game.cards;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Forces the {@link CardRegistry} CSV parse at boot rather than lazily on the
 * first card lookup. Low priority so it runs before the {@code @Startup}
 * service singletons (priority 2500) whose {@code load()} touches the registry.
 */
@ApplicationScoped
public class CardRegistryStartup {

    void onStart(@Observes @Priority(1) StartupEvent ev) {
        CardRegistry.bootstrap();
    }
}
