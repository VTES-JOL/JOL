package net.deckserver;

import net.deckserver.services.ParserService;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserServiceTest {

    @Test
    @SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
    @SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
    public void testParseCard() {

        String test = "[Mata Hari] is awesome, and turning that into a tooltip would be quite handy";
        String modified = ParserService.parseGlobalChat(test);
        assertEquals("[card:200955:Mata Hari] is awesome, and turning that into a tooltip would be quite handy", modified);

        test = "Nothing interesting here";
        modified = ParserService.parseGlobalChat(test);
        assertEquals(test, modified);

        test = "Multiple cards [Abactor] and also [Zip]";
        modified = ParserService.parseGlobalChat(test);
        assertEquals("Multiple cards [card:100004:Abactor] and also [card:201507:Zip]", modified);

        // Unresolved bracket is left verbatim; the &#39; entity is still decoded.
        test = "This [card not found] shouldn&#39;t work";
        modified = ParserService.parseGlobalChat(test);
        assertEquals("This [card not found] shouldn't work", modified);

        test = "Multiple cards [ Abactor ] and also [Zip  ]";
        modified = ParserService.parseGlobalChat(test);
        assertEquals("Multiple cards [card:100004:Abactor] and also [card:201507:Zip]", modified);

        test = "A link to [Cats' Guidance]";
        modified = ParserService.parseGlobalChat(test);
        assertEquals("A link to [card:100308:Cats' Guidance]", modified);

        test = "A link to [Theo Bell] and [Theo Bell (G2)] and [Theo Bell (ADV)] and [Theo Bell (G6)] should all work";
        modified = ParserService.parseGlobalChat(test);
        assertEquals("A link to [card:201362:Theo Bell] and [card:201362:Theo Bell] and [card:201363:Theo Bell:adv] and [card:201613:Theo Bell] should all work", modified);

        test = "A few emojis: :vampire: :shrug: :laughing: :joy: :sob:";
        modified = ParserService.parseGlobalChat(test);
        assertEquals("A few emojis: 🧛 🤷 😆 😂 😭", modified);
    }

    @Test
    @SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
    @SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
    public void testParseSymbols() {
        // Discipline case is preserved (inferior vs superior); (D) and {style} tokenise too.
        assertEquals(
                "gains [disc:pot] then [d] then [disc:POT] and [style:emphasis]",
                ParserService.parseGlobalChat("gains [pot] then (D) then [POT] and {emphasis}"));

        // A non-discipline bracket that is also not a card is untouched.
        assertEquals("[not a thing]", ParserService.parseGlobalChat("[not a thing]"));
    }

    @Test
    @SetEnvironmentVariable(key = "JOL_DATA", value = "src/test/resources/data")
    @SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
    public void testEntityDecode() {
        // Output is rendered as plain text now — the entities sanitizeText adds
        // are decoded back. &amp; is undone last.
        assertEquals("say \"hi\" & ping @Player1",
                ParserService.parseGlobalChat("say &#34;hi&#34; &amp; ping &#64;Player1"));

        // Legacy data encoded more than the obvious few: + as &#43;, = as &#61;,
        // backtick as &#96;, and emoji as hex references.
        assertEquals("pool rich +1",
                ParserService.parseGlobalChat("pool rich &#43;1"));
        assertEquals("British humor = best humor",
                ParserService.parseGlobalChat("British humor &#61; best humor"));
        assertEquals("run `npm test`",
                ParserService.parseGlobalChat("run &#96;npm test&#96;"));
        assertEquals("nice 🥲",
                ParserService.parseGlobalChat("nice &#x1f972;"));

        // A literally-typed reference (sanitised to &amp;#43;) is NOT decoded.
        assertEquals("literally &#43; here",
                ParserService.parseGlobalChat("literally &amp;#43; here"));
    }
}
