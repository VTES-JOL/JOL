package net.deckserver.rest;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class JolApplication extends ResourceConfig {

    public JolApplication() {
        packages("net.deckserver.rest");
    }
}
