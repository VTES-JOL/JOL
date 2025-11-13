package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.deckserver.game.storage.cards.importer.CryptImporter;
import net.deckserver.game.storage.cards.importer.LibraryImporter;
import net.deckserver.services.CardService;
import net.deckserver.services.ParserService;
import net.deckserver.storage.json.cards.CardSummary;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Tag("Builder")
@Tag("CardDatabase")
public class CardDatabaseBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CardDatabaseBuilder.class);
    private static final String LIBRARY_FILE = "vteslib";
    private static final String CRYPT_FILE = "vtescrypt";
    private static final String PLAYTEST = "_playtest";

    @Test
    public void buildCardDatabase() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        List<SummaryCard> coreCards = getCoreCards(Paths.get("csv/core"));
        List<SummaryCard> playTestCards = new ArrayList<>(); //getPlaytestCards(Paths.get("csv/playtest"));

        List<SummaryCard> summaryCards = Stream.of(coreCards, playTestCards).flatMap(List::stream).toList();

        Path imagesPath = Paths.get("images");
        Path outputPath = Paths.get("static");

        assert imagesPath.toFile().exists() : "Images path does not exist";

        List<CardSummary> cardSummaries = summaryCards.stream().map(SummaryCard::toCardSummary).toList();
        CardService.refresh(cardSummaries);

        for (SummaryCard summaryCard : summaryCards) {
            String id = summaryCard.getId();
            String outputPrefix = summaryCard.isPlayTest() ? "secured/" : "";

            summaryCard.getNames().forEach(name -> {
                assert CardService.findCardExact(name, true).isPresent() : String.format("Card %s does not exist", name);
            });

            // Process images
            String inputPrefix = summaryCard.isPlayTest() ? "playtest" : "core";
            Path inputImagePath = imagesPath.resolve(inputPrefix).resolve(generateImageName(summaryCard));
            Path outputImagePath = outputPath.resolve(outputPrefix).resolve("images").resolve(id);
            assert inputImagePath.toFile().exists() : String.format("Image %s does not exist - %s", inputImagePath, summaryCard.getDisplayName());
            Files.createDirectories(outputImagePath.getParent());
            Files.copy(inputImagePath, outputImagePath, StandardCopyOption.REPLACE_EXISTING);

            // Process HTML
            String htmlText = ParserService.parseSymbols(summaryCard.getHtmlText());
            Path outputHtmlPath = outputPath.resolve(outputPrefix).resolve("html").resolve(id);
            Files.createDirectories(outputHtmlPath.getParent());
            Files.writeString(outputHtmlPath, htmlText);

            // Process JSON
            Path outputJsonPath = outputPath.resolve(outputPrefix).resolve("json").resolve(id);
            Files.createDirectories(outputJsonPath.getParent());
            mapper.writeValue(outputJsonPath.toFile(), summaryCard);

            // Clear unneeded fields
            summaryCard.setOriginalText(null);
            summaryCard.setModes(null);
            summaryCard.setMultiMode(null);
            summaryCard.setPreamble(null);
            summaryCard.setDoNotReplace(null);
            summaryCard.setCost(null);
            summaryCard.setBurnOption(null);
            summaryCard.setHtmlText(null);
        }

        // Output complete cards.json
        mapper.writeValue(Paths.get("static/secured/cards.json").toFile(), summaryCards);

    }

    private List<SummaryCard> getCoreCards(Path basePath) throws Exception {
        LibraryImporter libraryImporter = new LibraryImporter(basePath, LIBRARY_FILE);
        CryptImporter cryptImporter = new CryptImporter(basePath, CRYPT_FILE);

        List<LibraryCard> libraryCards = libraryImporter.read();
        List<CryptCard> cryptCards = cryptImporter.read();
        List<SummaryCard> summaryCards = new ArrayList<>();

        libraryCards.forEach(libraryCard -> summaryCards.add(new SummaryCard(libraryCard)));
        cryptCards.forEach(cryptCard -> summaryCards.add(new SummaryCard(cryptCard)));

        return summaryCards;
    }

    private List<SummaryCard> getPlaytestCards(Path basePath) throws Exception {
        LibraryImporter libraryImporter = new LibraryImporter(basePath, LIBRARY_FILE + PLAYTEST, true);
        CryptImporter cryptImporter = new CryptImporter(basePath, CRYPT_FILE + PLAYTEST, true);

        List<LibraryCard> libraryCards = libraryImporter.read();
        List<CryptCard> cryptCards = cryptImporter.read();
        List<SummaryCard> summaryCards = new ArrayList<>();

        libraryCards.forEach(libraryCard -> summaryCards.add(new SummaryCard(libraryCard)));
        cryptCards.forEach(cryptCard -> summaryCards.add(new SummaryCard(cryptCard)));

        return summaryCards;
    }

    private String generateImageName(SummaryCard card) {
        String name = card.getDisplayName().toLowerCase()
                .replaceAll("œ", "oe")
                .replaceAll("[áäã]", "a")
                .replaceAll("[èéëêě]", "e")
                .replaceAll("[íî]", "i")
                .replaceAll("[öóøõ]", "o")
                .replaceAll("[üú]", "u")
                .replaceAll("[çč]", "c")
                .replaceAll("[ł]", "l")
                .replaceAll("[ñń]", "n")
                .replaceAll("[ż]", "z")
                .replaceAll(" ", "")
                .replaceAll("\\W", "");
        String group = Optional.ofNullable(card.getGroup())
                .map(s -> String.format("g%s", s.toLowerCase()))
                .map(s -> s.equals("gany") ? "any" : s)
                .orElse("");
        String adv = card.getAdvanced() != null && card.getAdvanced() ? "adv" : "";
        String imageName = String.format("%s%s%s.jpg", name, group, adv).toLowerCase();
        logger.debug("Found {} for card {} [{}]", imageName, card.getDisplayName(), card.getId());
        return imageName;
    }
}
