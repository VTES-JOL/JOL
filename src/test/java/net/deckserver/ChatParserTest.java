package net.deckserver;

import net.deckserver.dwr.model.ChatParser;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatParserTest {

    @Test
    @SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
    public void testParseCard() throws Exception {


        String test = "[Mata Hari] is awesome, and turning that into a tooltip would be quite handy";

        String modified = ChatParser.parseText(test);

        assertEquals("<a class='card-name' data-card-id='200955' data-secured='false'>Mata Hari</a> is awesome, and turning that into a tooltip would be quite handy", modified);

        test = "Nothing interesting here";

        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [Abactor] and also [Zip]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' data-card-id='100004' data-secured='false'>Abactor</a> and also <a class='card-name' data-card-id='201507' data-secured='false'>Zip</a>", modified);

        test = "This [card not found] shouldn&#39;t work";
        modified = ChatParser.parseText(test);
        assertEquals(test, modified);

        test = "Multiple cards [ Abactor ] and also [Zip  ]";
        modified = ChatParser.parseText(test);

        assertEquals("Multiple cards <a class='card-name' data-card-id='100004' data-secured='false'>Abactor</a> and also <a class='card-name' data-card-id='201507' data-secured='false'>Zip</a>", modified);

        test = "A link to [Cats' Guidance]";
        modified = ChatParser.parseText(test);

        assertEquals("A link to <a class='card-name' data-card-id='100308' data-secured='false'>Cats' Guidance</a>", modified);

        test = "A link to [Theo Bell] and [Theo Bell (G2)] and [Theo Bell (ADV)] and [Theo Bell (G6)] should all work";
        modified = ChatParser.parseText(test);

        //2023/05/13; this test does not work:
        assertEquals("A link to <a class='card-name' data-card-id='201362' data-secured='false'>Theo Bell</a> and <a class='card-name' data-card-id='201362' data-secured='false'>Theo Bell</a> and <a class='card-name' data-card-id='201363' data-secured='false'>Theo Bell <i class='icon adv'></i></a> and <a class='card-name' data-card-id='201613' data-secured='false'>Theo Bell</a> should all work", modified);
    }
}
