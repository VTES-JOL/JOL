package net.deckserver.services;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.SiteNotesRepository;
import net.deckserver.ws.WebSocketRegistry;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.parser.Parser;

import java.util.List;

public class SiteNotesService extends PersistedService {

    private static final SiteNotesRepository siteNotesRepository = new SiteNotesRepository();
    private static final SiteNotesService INSTANCE = new SiteNotesService();

    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().escapeHtml(true).build();

    private String notes = "";

    private SiteNotesService() {
        super("SiteNotesService", 0);
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
        String updatedNotes = notes == null ? "" : notes;
        if (INSTANCE.jpaWriteThenMutate(
                em -> siteNotesRepository.save(em, updatedNotes),
                () -> INSTANCE.notes = updatedNotes)) {
            WebSocketRegistry.notifyInvalidate(List.of("main-notes"));
        }
    }

    public static void clear() {
        setNotes("");
    }

    public static PersistedService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void persist() {
        // write-through only, see setNotes()
    }

    @Override
    protected void load() {
        try (EntityManager em = JpaFactory.createEntityManager()) {
            notes = siteNotesRepository.load(em);
            logger.info("Loaded site notes from JPA");
        } catch (Exception e) {
            logger.error("JPA load failed for SiteNotesService", e);
        }
    }
}
