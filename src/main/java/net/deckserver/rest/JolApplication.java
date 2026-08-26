package net.deckserver.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Just the base path — Quarkus REST auto-discovers every @Path resource on
 * the classpath by itself (unlike Jersey's ResourceConfig, which this used
 * to extend and needed an explicit packages("net.deckserver.rest") scan).
 */
@ApplicationPath("/api")
public class JolApplication extends Application {
}
