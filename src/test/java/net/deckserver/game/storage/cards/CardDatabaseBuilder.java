package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.storage.cards.importer.CryptImporter;
import net.deckserver.game.storage.cards.importer.LibraryImporter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class CardDatabaseBuilder {

    @Test
    public void buildCardDatabase() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Path basePath = Paths.get("src/test/resources/data/cards");
        Path libraryPath = basePath.resolve("vteslib.csv");
        Path cryptPath = basePath.resolve("vtescrypt.csv");

        LibraryImporter libraryImporter = new LibraryImporter(libraryPath);
        CryptImporter cryptImporter = new CryptImporter(cryptPath);

        List<LibraryCard> libraryCards = libraryImporter.read();
        List<CryptCard> cryptCards = cryptImporter.read();
        List<SummaryCard> summaryCards = new ArrayList<>();

        libraryCards.forEach(libraryCard -> {
            summaryCards.add(new SummaryCard(libraryCard));
        });

        cryptCards.forEach(cryptCard -> {
            summaryCards.add(new SummaryCard(cryptCard));
        });

        mapper.writeValue(basePath.resolve("cards.json").toFile(), summaryCards);
    }
}
