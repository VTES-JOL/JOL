package net.deckserver;

import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ChatParserTest {

    private static final Pattern pattern = Pattern.compile("\\[(.*?)\\]");

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void testParseCard() throws Exception {
        final CardSearch search = new CardSearch(Paths.get("src/test/resources/cards/cards.json"));

        String test = "[Mata Hari] is awesome, and turning that into a tooltip would be quite handy";

        String modified = parseText(search, test);

        assertEquals("<a class='card-name' title='km86'>Mata Hari</a> is awesome, and turning that into a tooltip would be quite handy", modified);

        test = "Nothing interesting here";

        modified = parseText(search, test);
        assertEquals(test, modified);

        test = "Multiple cards [Abactor] and also [Zip]";
        modified = parseText(search, test);

        assertEquals("Multiple cards <a class='card-name' title='pr59'>Abactor</a> and also <a class='card-name' title='bh144'>Zip</a>", modified);
    }

    private String parseText(CardSearch search, String text) {
        Matcher matcher = pattern.matcher(text);

        StringBuffer sb = new StringBuffer(text.length());
        while (matcher.find()) {
            for (int x = 1; x <= matcher.groupCount(); x++) {
                String match = matcher.group(x);
                assertNotNull(match);
                CardEntry card = search.findCard(match);
                assertNotNull(card);
                matcher.appendReplacement(sb, generateCardLink(card));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String generateCardLink(CardEntry card) {
        return "<a class='card-name' title='" + card.getCardId() + "'>" + card.getName() + "</a>";
    }
}
