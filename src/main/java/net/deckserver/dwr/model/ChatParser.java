package net.deckserver.dwr.model;

import net.deckserver.game.storage.cards.CardSearch;
import net.deckserver.storage.json.cards.CardSummary;
import org.owasp.html.Sanitizers;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatParser {

    private static final Pattern MARKUP_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final Pattern STYLE_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final Pattern D_PATTERN = Pattern.compile("\\(D\\)");
    private static final Pattern COST_PATTERN = Pattern.compile("Cost: (\\d|X) (pool|blood)");
    private static final List<String> disciplineSet = Arrays.asList("ani", "obe", "cel", "dom", "dem", "for", "san", "thn", "vic", "pro", "chi", "val", "mel", "nec", "obf", "pot", "qui", "pre", "ser", "tha", "aus", "vis", "abo", "myt", "dai", "spi", "tem", "obt", "str", "mal", "obl", "flight", "inn", "jud", "viz", "ven", "def", "mar", "red");
    public static String sanitizeText(String text) {
        return Sanitizers.LINKS.sanitize(text);
    }

    public static String parseText(String text) {
        String parsedForCards = parseTextForCards(text);
        String parsedForDisciplines = parseTextForDisciplines(parsedForCards);
        String parsedForDActions = parseTextForDAction(parsedForDisciplines);
        return parseTextForStyle(parsedForDActions);
    }

    public static String parseSymbols(String text) {
        text = parseTextForDisciplines(text);
        return parseTextForDAction(text);
    }

    public static boolean isDiscipline(String discipline) {
        return disciplineSet.contains(discipline.toLowerCase());
    }

    private static String parseTextForCards(String text) {
        Matcher matcher = MARKUP_PATTERN.matcher(text);

        StringBuilder sb = new StringBuilder(text.length());
        while (matcher.find()) {
            for (int x = 1; x <= matcher.groupCount(); x++) {
                String match = matcher.group(x).trim().replaceAll("&#39;", "'").replaceAll("&#34;", "\"");
                try {
                     CardSearch.INSTANCE.findCardExact(match).ifPresent(card -> matcher.appendReplacement(sb, generateCardLink(card)));
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

        StringBuilder sb = new StringBuilder(text.length());
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

    private static String parseTextForDAction(String text) {
        Matcher matcher = D_PATTERN.matcher(text);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, generateDAction());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String parseTextForStyle(String text) {
        Matcher matcher = STYLE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        while (matcher.find()) {
            for (int x = 1; x <= matcher.groupCount(); x++) {
                String match = matcher.group(x).trim();
                matcher.appendReplacement(sb, generateStyle(match));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String parseTextForCost(String text) {
        Matcher matcher = COST_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        while (matcher.find()) {
            int cost = Integer.parseInt(matcher.group(1));
            String type = matcher.group(2);
            matcher.appendReplacement(sb, generateCost(cost, type));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String generateCardLink(CardSummary card) {
        return "<a class='card-name' data-card-id='" + card.getId() + "'>" + card.getDisplayName() + (card.isAdvanced() ? " <i class='icon adv'/>" : "") + "</a>";
    }

    public static String generateDisciplineLink(String discipline) {
        return "<span class='icon " + discipline + "'></span>";
    }

    public static String generateStyle(String text) {
        return "<span class='game-name'>" + text + "</span>";
    }

    public static String generateDAction() {
        return "<span class='icon D'></span>";
    }

    public static String generateCost(int cost, String type) {
        return "Cost: <span class='icon cost " + type + cost + "'></span>";
    }

}
