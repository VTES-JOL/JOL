package net.deckserver.services;

import com.vdurmont.emoji.EmojiParser;
import net.deckserver.game.cards.CardRef;
import net.deckserver.game.cards.CardRegistry;
import org.owasp.html.Sanitizers;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chat / log text rendering.
 *
 * <p>{@link #parseGlobalChat}/{@link #parseGameChat} no longer emit HTML. They
 * rewrite the markup a player types into plain-text <em>tokens</em> the React
 * client resolves to components at render time:
 * <ul>
 *   <li>{@code [Card Name]}      &rarr; {@code [card:<id>:<name>]} (+ {@code :adv} for an advanced crypt card)</li>
 *   <li>{@code [pot]} / {@code [POT]} &rarr; {@code [disc:pot]} / {@code [disc:POT]} (case = inferior/superior)</li>
 *   <li>{@code (D)}               &rarr; {@code [d]}</li>
 *   <li>{@code {text}}            &rarr; {@code [style:text]}</li>
 * </ul>
 * Emoji shortcodes are still expanded to Unicode here. The output is plain text
 * (HTML entities from {@link #sanitizeText} are decoded back), safe to render as
 * React text nodes — see {@code src/main/webui/src/utils/parseMessageTokens.ts}.
 *
 * <p>{@link #parseSymbols} is unchanged and still emits HTML {@code <span>}
 * icons — its only caller is {@code CardDatabaseBuilder}, which bakes the static
 * card-tooltip HTML assets, not chat.
 */
public class ParserService {

    private static final Pattern MARKUP_PATTERN = Pattern.compile("\\[(.*?)\\]");
    private static final Pattern STYLE_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final Pattern D_PATTERN = Pattern.compile("\\(D\\)");
    // Numeric character reference — decimal (&#43;) or hex (&#x1f972;). The legacy
    // sanitiser encoded far more than the obvious &#39;/&#34;/&#64; (e.g. + as
    // &#43;, = as &#61;, and emoji as &#x…;); all of them must come back now that
    // the message is rendered as plain text, not HTML the browser would decode.
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(x?)([0-9a-fA-F]+);");
    private static final Set<String> disciplineSet = Set.of("ani", "obe", "cel", "dom", "dem", "for", "san", "thn", "vic", "pro", "chi", "val", "mel", "nec", "obf", "pot", "qui", "pre", "ser", "tha", "aus", "vis", "abo", "myt", "dai", "spi", "tem", "obt", "str", "mal", "obl", "flight", "inn", "jud", "viz", "ven", "def", "mar", "red");

    public static String sanitizeText(String text) {
        return Sanitizers.LINKS.sanitize(text);
    }

    public static String parseGlobalChat(String text) {
        return parseChat(text, false);
    }

    public static String parseGameChat(String text) {
        return parseChat(text, true);
    }

    private static String parseChat(String text, boolean includePlaytest) {
        String s = tokeniseCards(text, includePlaytest);
        s = tokeniseDisciplines(s);
        s = tokeniseDAction(s);
        s = parseTextForEmoji(s);
        s = tokeniseStyle(s);
        return decodeBasicEntities(s);
    }

    public static String parseSymbols(String text) {
        text = parseTextForDisciplines(text);
        return parseTextForDAction(text);
    }

    public static String parseTextForEmoji(String text) {
        return EmojiParser.parseToUnicode(text);
    }

    public static boolean isDiscipline(String discipline) {
        return disciplineSet.contains(discipline.toLowerCase());
    }

    // ── Token builders ──────────────────────────────────────────────────────
    // Used directly by callers (e.g. JolGame) that assemble a message from
    // pieces and never run it back through parseGameChat.

    public static String cardToken(String cardId, String name, boolean advanced) {
        return "[card:" + cardId + ":" + name + (advanced ? ":adv" : "") + "]";
    }

    public static String disciplineToken(String code) {
        return "[disc:" + code + "]";
    }

    // ── Chat tokenisers ────────────────────────────────────────────────────

    private static String tokeniseCards(String text, boolean includePlaytest) {
        Matcher matcher = MARKUP_PATTERN.matcher(text);

        StringBuilder sb = new StringBuilder(text.length());
        while (matcher.find()) {
            String match = matcher.group(1).trim().replaceAll("&#39;", "'").replaceAll("&#34;", "\"");
            try {
                CardRegistry.resolveNormalized(match, includePlaytest)
                        .map(CardRef::of)
                        .ifPresent(card -> matcher.appendReplacement(sb, Matcher.quoteReplacement(card.token())));
            } catch (IllegalArgumentException e) {
                // unresolved bracket — leave it verbatim
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String tokeniseDisciplines(String text) {
        Matcher matcher = MARKUP_PATTERN.matcher(text);

        StringBuilder sb = new StringBuilder(text.length());
        while (matcher.find()) {
            String match = matcher.group(1).trim();
            if (disciplineSet.contains(match.toLowerCase())) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(disciplineToken(match)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String tokeniseDAction(String text) {
        return D_PATTERN.matcher(text).replaceAll("[d]");
    }

    private static String tokeniseStyle(String text) {
        Matcher matcher = STYLE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        while (matcher.find()) {
            String match = matcher.group(1).trim();
            matcher.appendReplacement(sb, Matcher.quoteReplacement("[style:" + match + "]"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Undo the HTML entity encoding {@link #sanitizeText} applies — the message
     * is rendered as plain text now, so every numeric character reference
     * ({@code &#64;}, {@code &#39;}, {@code &#43;}, {@code &#x1f972;}, …) must
     * come back as a real character. {@code &amp;} is unescaped last so a
     * literally-typed {@code &#64;} (encoded to {@code &amp;#64;}) survives as
     * {@code &#64;} rather than being turned into an {@code @}.
     */
    private static String decodeBasicEntities(String text) {
        Matcher m = NUMERIC_ENTITY.matcher(text);
        StringBuilder sb = new StringBuilder(text.length());
        while (m.find()) {
            int codePoint;
            try {
                codePoint = Integer.parseInt(m.group(2), m.group(1).isEmpty() ? 10 : 16);
            } catch (NumberFormatException e) {
                continue; // e.g. "&#abc;" with no x — not a valid reference, leave verbatim
            }
            if (Character.isValidCodePoint(codePoint)) {
                m.appendReplacement(sb, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
            }
        }
        m.appendTail(sb);
        return sb.toString().replace("&amp;", "&");
    }

    // ── HTML symbol rendering (CardDatabaseBuilder only) ────────────────────

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

    public static String generateDisciplineLink(String discipline) {
        return "<span class='icon " + discipline + "'></span>";
    }

    public static String generateDAction() {
        return "<span class='icon D'></span>";
    }

}
