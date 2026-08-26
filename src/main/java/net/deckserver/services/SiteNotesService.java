package net.deckserver.services;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;

import jakarta.persistence.EntityManager;
import net.deckserver.jpa.JpaFactory;
import net.deckserver.jpa.repository.SiteNotesRepository;
import net.deckserver.ws.WebSocketRegistry;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.parser.Parser;

import java.util.List;

@Singleton
@Startup
public class SiteNotesService extends PersistedService {

    private static final SiteNotesRepository siteNotesRepository = new SiteNotesRepository();
    private static SiteNotesService instance() {
        return resolve(SiteNotesService.class, SiteNotesService::new);
    }

    private static final Parser MARKDOWN_PARSER = Parser.builder().build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().escapeHtml(true).build();

    private String notes = "";

    SiteNotesService() {
        super("SiteNotesService", 0);
    }

    public static String getRawNotes() {
        return instance().notes;
    }

    public static String getNotesHtml() {
        if (instance().notes.isBlank()) {
            return "";
        }
        Node document = MARKDOWN_PARSER.parse(instance().notes);
        return HTML_RENDERER.render(document);
    }

    public static void setNotes(String notes) {
        String updatedNotes = notes == null ? "" : notes;
        if (instance().jpaWriteThenMutate(
                em -> siteNotesRepository.save(em, updatedNotes),
                () -> instance().notes = updatedNotes)) {
            WebSocketRegistry.notifyInvalidate(List.of("main-notes"));
        }
    }

    public static void clear() {
        setNotes("");
    }

    public static PersistedService getInstance() {
        return instance();
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
