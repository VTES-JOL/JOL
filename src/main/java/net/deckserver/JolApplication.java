package net.deckserver;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/rest")
public class JolApplication extends ResourceConfig {
    public JolApplication() {
        packages("net.deckserver.rest");
    }
}
