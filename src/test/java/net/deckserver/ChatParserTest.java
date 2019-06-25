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

        assertEquals("<a class='card-name' title='km86'>Mata Hari</a> is awesome, and turning that into a tooltip would be quite handy", modified);

        test = "Nothing interesting here";

        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [Abactor] and also [Zip]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' title='pr59'>Abactor</a> and also <a class='card-name' title='bh144'>Zip</a>", modified);

        test = "This [card not found] shouldn&#39;t work";
        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [ Abactor ] and also [Zip  ]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' title='pr59'>Abactor</a> and also <a class='card-name' title='bh144'>Zip</a>", modified);

        test = "A link to [Cats' Guidance]";
        modified = ChatParser.parseText(test);

        assertEquals("A link to <a class='card-name' title='jy74'>Cats' Guidance</a>", modified);

        test = "Name: Acrobatics<br/>Cardtype: Combat<br/>Cost: 1 blood<br/>Discipline: Celerity<br/>[cel] Additional strike.\\n[CEL] Strike: dodge, with an additional strike.";
        modified = ChatParser.parseText(test);

        assertEquals("Name: Acrobatics<br />Cardtype: Combat<br />Cost: 1 blood<br />Discipline: Celerity<br /><span class='discipline cel'></span> Additional strike.\\n<span class='discipline CEL'></span> Strike: dodge, with an additional strike.", modified);
    }
}
