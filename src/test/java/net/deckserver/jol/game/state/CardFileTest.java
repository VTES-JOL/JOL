package net.deckserver.jol.game.state;

import net.deckserver.jol.game.cards.CardEntry;
import net.deckserver.jol.game.cards.search.file.FileCardRepository;
import org.junit.Test;
import org.slf4j.Logger;

import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.getLogger;

public class CardFileTest {

    private static final Logger logger = getLogger(CardFileTest.class);

    private static final Charset iso8859 = Charset.forName("ISO-8859-15");
    private static final Charset utf8 = Charset.forName("UTF-8");


    @Test
    public void CardMap() throws Exception {
        FileCardRepository repository = new FileCardRepository("src/test/resources/cards/cardlist.properties", "src/test/resources/cards/cardlist.txt");
        assertNotNull(repository);
        String cardId = repository.getId("Francisco Domingo de Polonia");
        assertEquals("sw21", cardId);
        cardId = repository.getId("Ambrosio Luis Monçada, Plenipotentiary");
        assertEquals("sw1", cardId);
        cardId = repository.getId("Ambrosio Luis Moncada, Plenipotentiary");
        assertEquals("sw1", cardId);
        CardEntry entry = repository.findById(cardId);
        assertTrue(entry.isCrypt());
        assertEquals("Vampire", entry.getType());
        assertEquals("2", entry.getGroup());
    }
}
