package deckserver.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.SummaryCard;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CardFileTest {

    private Path cardPath;

    public CardFileTest(String cardLocation) {
        cardPath = Paths.get(cardLocation);
    }

    private List<SummaryCard> read() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(cardPath.toFile(), new TypeReference<List<SummaryCard>>() {
        });
    }

    public static void main(String[] args) throws IOException {
        CardFileTest cardFileTest = new CardFileTest("src/test/resources/cards/summary.json");
        List<SummaryCard> summaries = cardFileTest.read();
        summaries.stream().filter(card -> card.getNames().contains("Carlton Van Wyk (Hunter)")).map(SummaryCard::getText).forEach(System.out::println);
    }
}