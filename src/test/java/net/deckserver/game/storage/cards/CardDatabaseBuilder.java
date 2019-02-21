package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.storage.cards.importer.CryptImporter;
import net.deckserver.game.storage.cards.importer.LibraryImporter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CardDatabaseBuilder {

    // \s\((Wraith|Mage|Hunter|Bane Mummy|Mummy|Changeling|Goblin)\)$
    @Test
    public void buildCardDatabase() throws Exception {
        Path basePath = Paths.get("src/test/resources/cards");
        Path keyPath = basePath.resolve("cardlist.properties");
        Path libraryPath = basePath.resolve("vteslib.csv");
        Path cryptPath = basePath.resolve("vtescrypt.csv");

        Properties keyProperties = new Properties();
        Map<String, String> keys = new HashMap<>();
        try (FileReader keyReader = new FileReader(keyPath.toFile())) {
            keyProperties.load(keyReader);
            for (String key: keyProperties.stringPropertyNames()) {
                keys.put(keyProperties.getProperty(key), key);
            }
        }

        LibraryImporter libraryImporter = new LibraryImporter(libraryPath);
        CryptImporter cryptImporter = new CryptImporter(cryptPath);

        List<LibraryCard> libraryCards = libraryImporter.read();
        List<CryptCard> cryptCards = cryptImporter.read();
        List<SummaryCard> summaryCards = new ArrayList<>();

        assertThat(libraryCards.size(), is (2212));
        assertThat(cryptCards.size(), is (1519));

        libraryCards.forEach(card -> card.setKey(keys.get(card.getDisplayName())));

        cryptCards.forEach(card -> card.setKey(keys.get(card.getDisplayName())));

        libraryCards.forEach(libraryCard -> {
            summaryCards.add(new SummaryCard(libraryCard));
        });

        cryptCards.forEach(cryptCard -> {
            summaryCards.add(new SummaryCard(cryptCard));
        });


        // Index on displayName
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(basePath.resolve("cards.json").toFile(), summaryCards);
    }
}
