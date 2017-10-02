package net.deckserver.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.deckserver.game.SummaryCard;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class DeckTest {

    private static Pattern countPattern = Pattern.compile("^");

    @Test
    public void sampleDeck() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SummaryCard> cards = objectMapper.readValue(Paths.get("src/test/resources/cards/summary.json").toFile(), new TypeReference<List<SummaryCard>>() {
        });
        assertNotNull(cards);
        assertFalse(cards.isEmpty());

        List<String> deckLines = Files.readAllLines(Paths.get("src/test/resources/player1/deck1.txt"));
        System.out.println(deckLines.size());
        deckLines.forEach(DeckTest::parseline);
    }

    private static DeckItem parseline(String deckLine) {
        System.out.println(deckLine);
        Matcher countMatcher = countPattern.matcher(deckLine.trim());
        if (countMatcher.matches()) {
            Integer count = 1;
            String card = "Dummy card";
            System.out.printf(count + " x " + card);
        }
        return null;
    }
}