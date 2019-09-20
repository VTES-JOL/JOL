package net.deckserver.dwr.model;

import net.deckserver.game.storage.cards.CardEntry;
import net.deckserver.game.storage.cards.CardSearch;
import org.owasp.html.Sanitizers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatParser {

    private static final Pattern MARKUP_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final List<String> disciplineSet = Arrays.asList("ani", "obe", "cel", "dom", "dem", "for", "san", "thn", "vic", "pro", "chi", "val", "mel", "nec", "obf", "pot", "qui", "pre", "ser", "tha", "aus", "vis", "abo", "myt", "dai", "spi", "tem", "obt", "str", "mal", "flight");

    public static String parseText(String text) {
        String sanitizedMessage = Sanitizers.LINKS.sanitize(text);
        String parsedForCards = parseTextForCards(sanitizedMessage);
        return parseTextForDisciplines(parsedForCards);
    }

    private static String parseTextForCards(String text) {
        Matcher matcher = MARKUP_PATTERN.matcher(text);

        StringBuffer sb = new StringBuffer(text.length());
        while (matcher.find()) {
            for (int x = 1; x <= matcher.groupCount(); x++) {
                String match = matcher.group(x).trim().replaceAll("&#39;", "'").replaceAll("&#34;", "\"");
                try {
                    CardEntry card = CardSearch.INSTANCE.findCardExact(match);
                    matcher.appendReplacement(sb, generateCardLink(card));
                } catch (IllegalArgumentException e) {
                    // do nothing
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String parseTextForDisciplines(String text) {
        Matcher matcher = MARKUP_PATTERN.matcher(text);

        StringBuffer sb = new StringBuffer(text.length());
        while (matcher.find()) {
            for (int x = 1; x <= matcher.groupCount(); x++) {
                String match = matcher.group(x).trim();
                if (disciplineSet.contains(match.toLowerCase())) {
                    matcher.appendReplacement(sb, generateDisciplineLink(match));
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String generateCardLink(CardEntry card) {
        return "<a class='card-name' title='" + card.getCardId() + "'>" + card.getName() + "</a>";
    }

    public static String generateDisciplineLink(String discipline) {
        return "<span class='discipline " + discipline + "'></span>";
    }

}
