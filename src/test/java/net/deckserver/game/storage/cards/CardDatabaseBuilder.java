package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.storage.cards.importer.CryptImporter;
import net.deckserver.game.storage.cards.importer.LibraryImporter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Tag("Builder")
@Tag("CardDatabase")
public class CardDatabaseBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CardDatabaseBuilder.class);

    private static List<SummaryCard> getSummaryCards(Path basePath) throws Exception {
        Path libraryPath = basePath.resolve("vteslib.csv");
        Path cryptPath = basePath.resolve("vtescrypt.csv");

        LibraryImporter libraryImporter = new LibraryImporter(libraryPath);
        CryptImporter cryptImporter = new CryptImporter(cryptPath);

        List<LibraryCard> libraryCards = libraryImporter.read();
        List<CryptCard> cryptCards = cryptImporter.read();
        List<SummaryCard> summaryCards = new ArrayList<>();

        libraryCards.forEach(libraryCard -> summaryCards.add(new SummaryCard(libraryCard)));
        cryptCards.forEach(cryptCard -> summaryCards.add(new SummaryCard(cryptCard)));
        return summaryCards;
    }

    @Test
    public void buildCardDatabase() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Path basePath = Paths.get("src/test/resources/data/cards");
        List<SummaryCard> summaryCards = getSummaryCards(basePath);

        Path staticPath = Paths.get("/Users/shannon/static");
        Path jsonPath = staticPath.resolve("json");
        Path htmlPath = staticPath.resolve("html");

        try {
            for (SummaryCard summaryCard : summaryCards) {
                String htmlText = summaryCard.getHtmlText();
                String id = summaryCard.getId();
                Path htmlFilePath = htmlPath.resolve(id);
                Files.write(htmlFilePath, htmlText.getBytes(StandardCharsets.UTF_8));
                summaryCard.setHtmlText(null);

                Path jsonFilePath = jsonPath.resolve(id);
                mapper.writeValue(jsonFilePath.toFile(), summaryCard);

                summaryCard.setOriginalText(null);
                summaryCard.setModes(null);
                summaryCard.setMultiMode(null);
                summaryCard.setPreamble(null);
                summaryCard.setDoNotReplace(null);
            }
        } catch (IOException e) {
            logger.error("Unable to write file", e);
        }

        mapper.writeValue(Paths.get("src/main/resources/cards.json").toFile(), summaryCards);
    }
}
