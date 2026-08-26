package net.deckserver.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class SiteNotesServiceTest {

    @Test
    void setsAndRendersMarkdownNotes() {
        SiteNotesService.setNotes("**bold** notice");
        assertThat(SiteNotesService.getRawNotes(), equalTo("**bold** notice"));
        assertThat(SiteNotesService.getNotesHtml(), containsString("<strong>bold</strong>"));
    }

    @Test
    void clearResetsNotes() {
        SiteNotesService.setNotes("temporary");
        SiteNotesService.clear();
        assertThat(SiteNotesService.getRawNotes(), equalTo(""));
        assertThat(SiteNotesService.getNotesHtml(), equalTo(""));
    }
}
