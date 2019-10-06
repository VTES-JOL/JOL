package net.deckserver;

import net.deckserver.dwr.model.ChatParser;
import net.deckserver.game.storage.cards.CardSearch;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChatParserTest {

    @Test
    public void testParseCard() throws Exception {

        System.setProperty("JOL_DATA", "src/test/resources");

        String test = "[Mata Hari] is awesome, and turning that into a tooltip would be quite handy";

        String modified = ChatParser.parseText(test);

        assertEquals("<a class='card-name' data-card-id='km86'>Mata Hari</a> is awesome, and turning that into a tooltip would be quite handy", modified);

        test = "Nothing interesting here";

        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [Abactor] and also [Zip]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' data-card-id='pr59'>Abactor</a> and also <a class='card-name' data-card-id='bh144'>Zip</a>", modified);

        test = "This [card not found] shouldn&#39;t work";
        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [ Abactor ] and also [Zip  ]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' data-card-id='pr59'>Abactor</a> and also <a class='card-name' data-card-id='bh144'>Zip</a>", modified);

        test = "A link to [Cats' Guidance]";
        modified = ChatParser.parseText(test);

        assertEquals("A link to <a class='card-name' data-card-id='jy74'>Cats' Guidance</a>", modified);
    }
}
