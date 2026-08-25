package net.deckserver.services;

import net.deckserver.ws.WebSocketRegistry;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SiteNotesService extends PersistedService {

    private static final Path PERSISTENCE_PATH = DataPaths.path("site-notes.md");
    private static final SiteNotesService INSTANCE = new SiteNotesService();

    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().escapeHtml(true).build();

    private String notes = "";

    private SiteNotesService() {
        super("SiteNotesService", 5);
        load();
    }

    public static String getRawNotes() {
        return INSTANCE.notes;
    }

    public static String getNotesHtml() {
        if (INSTANCE.notes.isBlank()) {
            return "";
        }
        Node document = MARKDOWN_PARSER.parse(INSTANCE.notes);
        return HTML_RENDERER.render(document);
    }

    public static void setNotes(String notes) {
        INSTANCE.notes = notes == null ? "" : notes;
        WebSocketRegistry.notifyInvalidate(List.of("main-notes"));
    }

    public static void clear() {
        setNotes("");
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        if (shouldSkipPersistence()) {
            logger.debug("Skipping persistence - {} mode", isTestModeEnabled() ? "test" : "shutdown");
            return;
        }

        try {
            Files.writeString(PERSISTENCE_PATH, notes);
            logger.debug("Successfully persisted site notes");
        } catch (IOException e) {
            logger.error("Unable to save site notes", e);
        }
    }

    @Override
    protected void load() {
        if (!Files.exists(PERSISTENCE_PATH)) {
            logger.info("No existing site notes file found");
            return;
        }

        try {
            notes = Files.readString(PERSISTENCE_PATH);
            logger.info("Loaded site notes");
        } catch (IOException e) {
            logger.error("Unable to load site notes.", e);
        }
    }
}
