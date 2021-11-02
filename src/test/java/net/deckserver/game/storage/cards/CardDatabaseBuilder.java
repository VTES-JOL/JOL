package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.storage.cards.importer.CryptImporter;
import net.deckserver.game.storage.cards.importer.LibraryImporter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class CardDatabaseBuilder {

    // \s\((Wraith|Mage|Hunter|Bane Mummy|Mummy|Changeling|Goblin)\)$
    @Test
    public void buildCardDatabase() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Path basePath = Paths.get("src/test/resources/data/cards");
        Path keyPath = basePath.resolve("cardlist.properties");
        Path libraryPath = basePath.resolve("vteslib.csv");
        Path cryptPath = basePath.resolve("vtescrypt.csv");
        Path amaranthPath = basePath.resolve("amaranth.json");

        Properties keyProperties = new Properties();
        Map<String, String> keys = new HashMap<>();
        Map<String, String> amaranthKeys = new HashMap<>();

        final Set<String> remainingKeys;
        try (FileReader keyReader = new FileReader(keyPath.toFile())) {
            keyProperties.load(keyReader);
            remainingKeys = new HashSet<>(keyProperties.stringPropertyNames());
            for (String key : remainingKeys) {
                keys.put(keyProperties.getProperty(key), key);
            }
        }

        LibraryImporter libraryImporter = new LibraryImporter(libraryPath);
        CryptImporter cryptImporter = new CryptImporter(cryptPath);

        List<LibraryCard> libraryCards = libraryImporter.read();
        List<CryptCard> cryptCards = cryptImporter.read();
        List<SummaryCard> summaryCards = new ArrayList<>();

        assertThat(libraryCards.size(), is(2219));
        assertThat(cryptCards.size(), is(1580));

        // Uncomment when Ke fixes cards
        //JsonNode amaranthNode = mapper.readTree(new URL("http://amaranth.vtes.co.nz/api/cards"));
        JsonNode amaranthNode = mapper.readTree(amaranthPath.toFile());

        JsonNode rootNode = amaranthNode.get("result");
        for (JsonNode cardNode : rootNode) {
            String name = cardNode.get("name").asText();
            String id = cardNode.get("id").asText();
            amaranthKeys.put(name, id);
            amaranthKeys.put(StringUtils.stripAccents(name).toLowerCase(), id);
        }

        for (LibraryCard card : libraryCards) {
            String key = keys.get(card.getDisplayName());
            String amaranthId = amaranthKeys.get(card.getDisplayName());
            if (amaranthId == null) {
                amaranthId = amaranthKeys.get(StringUtils.stripAccents(card.getDisplayName()).toLowerCase());
            }
            assertNotNull("Missing jolId for " + card.getDisplayName(), key);
            //assertNotNull("Missing amaranth id for " + card.getDisplayName(), amaranthId);
            card.setKey(key);
            card.setJolId(amaranthId);
            remainingKeys.remove(key);
        }

        for (CryptCard card : cryptCards) {
            String key = keys.get(card.getDisplayName());
            String amaranthId = amaranthKeys.get(card.getDisplayName());
            if (amaranthId == null) {
                amaranthId = amaranthKeys.get(StringUtils.stripAccents(card.getDisplayName()).toLowerCase());
            }
            assertNotNull("Missing jolId for " + card.getDisplayName(), key);
            //assertNotNull("Missing amaranth id for " + card.getDisplayName(), amaranthId);
            card.setJolId(key);
            card.setAmaranthId(amaranthId);
            remainingKeys.remove(key);
        }

        libraryCards.forEach(libraryCard -> {
            summaryCards.add(new SummaryCard(libraryCard));
        });

        cryptCards.forEach(cryptCard -> {
            summaryCards.add(new SummaryCard(cryptCard));
        });

        assertThat(remainingKeys.size(), is(0));

        mapper.writeValue(basePath.resolve("cards.json").toFile(), summaryCards);
    }
}
