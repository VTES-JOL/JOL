package net.deckserver.services;

import net.deckserver.rest.bean.ImportPreviewBean;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Plain unit test — the import service and the registries it uses are static
 * and load {@code csv/core/*.csv} from the working dir, no DB.
 */
class DeckImportServiceTest {

    private static String id(String name) {
        return CardSearchService.autocomplete(name).stream()
                .filter(c -> c.name().equalsIgnoreCase(name)).findFirst().orElseThrow().id();
    }

    @Test
    void previewsPlainJolTextWithResolvedCardsAndErrors() {
        ImportPreviewBean preview = DeckImportService.preview("""
                2 Govern the Unaligned
                3 Definitely Not A Real Card
                1 Deflection
                """);

        assertThat(preview.format(), equalTo("jol"));
        assertThat(preview.resolved(), hasSize(2));
        assertThat(preview.resolved().stream().map(r -> r.card().name()).toList(),
                containsInAnyOrder("Govern the Unaligned", "Deflection"));
        assertThat(preview.errors(), hasSize(1));
        assertThat(preview.errors().get(0).line(), containsStringIgnoringCase("Definitely Not A Real Card"));
    }

    @Test
    void previewsKrcgJsonWithMetaNameAndComments() {
        String json = """
                {
                  "name": "Imported Weenie",
                  "comments": "  a description  ",
                  "crypt":   { "count": 2, "cards": [ { "id": "%s", "count": 2, "name": "Aabbt Kindred" } ] },
                  "library": { "count": 3, "cards": [
                     { "type": "Action", "count": 3, "cards": [ { "id": "%s", "count": 3, "name": "Govern the Unaligned" } ] }
                  ] }
                }
                """.formatted(id("Aabbt Kindred"), id("Govern the Unaligned"));

        ImportPreviewBean preview = DeckImportService.preview(json);

        assertThat(preview.format(), equalTo("krcg"));
        assertThat(preview.deckName(), equalTo("Imported Weenie"));
        assertThat(preview.deckDescription(), equalTo("a description"));
        assertThat(preview.resolved(), hasSize(2));
        assertThat(preview.errors(), is(empty()));
        assertThat(preview.resolved().stream().filter(r -> r.card().crypt()).findFirst().orElseThrow().count(), equalTo(2));
    }

    @Test
    void krcgUnknownIdBecomesAnError() {
        ImportPreviewBean preview = DeckImportService.preview("""
                { "crypt": { "cards": [ { "id": "999999999", "count": 1 } ] }, "library": { "cards": [] } }
                """);

        assertThat(preview.format(), equalTo("krcg"));
        assertThat(preview.resolved(), is(empty()));
        assertThat(preview.errors(), hasSize(1));
        assertThat(preview.errors().get(0).reason(), containsString("999999999"));
    }

    @Test
    void buildContentsEmitsCryptThenBlankLineThenLibrary() {
        String contents = DeckImportService.buildContents(List.of(
                new DeckImportService.Entry(id("Aabbt Kindred"), 2),
                new DeckImportService.Entry(id("Govern the Unaligned"), 4)));

        assertThat(contents, equalTo("2 x Aabbt Kindred\n\n4 x Govern the Unaligned\n"));
    }

    @Test
    void buildContentsSkipsUnknownIds() {
        String contents = DeckImportService.buildContents(List.of(
                new DeckImportService.Entry("999999999", 3),
                new DeckImportService.Entry(id("Govern the Unaligned"), 1)));

        assertThat(contents, equalTo("\n1 x Govern the Unaligned\n"));
    }
}
