package net.deckserver;

import net.deckserver.dwr.model.ChatParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static org.junit.Assert.assertEquals;

public class ChatParserTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testParseCard() throws Exception {

        String test = "[Mata Hari] is awesome, and turning that into a tooltip would be quite handy";

        String modified = ChatParser.parseText(test);

        assertEquals("<a class='card-name' title='km86'>Mata Hari</a> is awesome, and turning that into a tooltip would be quite handy", modified);

        test = "Nothing interesting here";

        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [Abactor] and also [Zip]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' title='pr59'>Abactor</a> and also <a class='card-name' title='bh144'>Zip</a>", modified);

        test = "This [card not found] shouldn't work";
        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [ Abactor ] and also [Zip  ]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' title='pr59'>Abactor</a> and also <a class='card-name' title='bh144'>Zip</a>", modified);

    }
}
