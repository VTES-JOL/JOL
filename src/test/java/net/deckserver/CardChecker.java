package net.deckserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Ignore
public class CardChecker {

    @Test
    public void loadCards() throws Exception {
        String basePath = "src/test/resources";
        Path keyFile = Paths.get(basePath, "cards", "base.prop");
        Path textFile = Paths.get(basePath, "cards", "base.txt");
        assertTrue(keyFile.toFile().exists());
        assertTrue(textFile.toFile().exists());
        List<String> keys = Files.readAllLines(keyFile, Charset.forName("ISO-8859-1"));
        List<String> text = Files.readAllLines(textFile, Charset.forName("ISO-8859-1"));

        List<String> names = text.stream().filter(s -> s.startsWith("Name:")).map(s -> s.replaceAll("^Name: ", "")).map(String::toLowerCase).collect(Collectors.toList());
        assertThat(names.size(), is(3723));

        // Map of Names -> keys
        Map<String, String> nameKeys = keys.stream().map(s -> s.split("=")).collect(Collectors.toMap(s -> s[1].toLowerCase(), s -> s[0]));

        names.forEach(name -> {
            assertTrue("missing key " + name, nameKeys.containsKey(name));

        });

        CardSearch cardSearch = new CardSearch(keys, text);

        cardSearch.export(new File(basePath, "cards/cards.json"));
    }
}
