package net.deckserver.game.cards.importer;

import net.deckserver.game.cards.PlayMode;
import net.deckserver.game.cards.PlayTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Derives a library card's play modes from its type list and raw card text —
 * ported verbatim (regexes and all) from the old test-scope
 * {@code LibraryImporter#setModes/parsePowerOrConviction/setMode} plus its
 * preamble handling, now run at boot by
 * {@link net.deckserver.game.cards.CardRegistry}.
 *
 * <p>The bracket notation in the text ({@code [dom]}, {@code [DOM]}) marks a
 * discipline-gated option; each becomes one {@link PlayMode}. The leading
 * restriction line(s) become the {@code preamble} and set the
 * {@code unique} / {@code doNotReplace} / {@code multiMode} flags.
 */
public final class PlayModeParser {

    private static final Logger logger = LoggerFactory.getLogger(PlayModeParser.class);

    private static final Pattern PUT_INTO_PLAY_PATTERN = Pattern.compile(".*[Pp]ut this card in(?:to)? play.*");
    private static final Pattern PUT_INTO_UNCONTROLLED_PATTERN = Pattern.compile(".*[Pp]ut this card in(?:to)? play in your uncontrolled region.*");
    private static final Pattern PUT_ON_CONTROLLED_PATTERN = Pattern.compile(".*[Pp]ut this card on a minion you control.*");
    private static final Pattern PUT_ON_SELF_PATTERN = Pattern.compile(".*[Pp]ut this card(?: (?:with|and) .*?)? on (this|the acting).*");
    private static final Pattern PUT_ON_SOMETHING_PATTERN = Pattern.compile(".*[Pp]ut this card on.*");
    private static final Pattern AS_ABOVE_PATTERN = Pattern.compile("As (\\[(?<disc>.*)])? ?above.*");
    private static final Pattern REMOVE_FROM_GAME_PATTERN = Pattern.compile(".*[Rr]emove this card from the game.*");
    private static final Pattern MODE_OR_PATTERN = Pattern.compile("^\\[[A-Za-z]+\\]\\s+or\\s+\\[[A-Za-z]+\\](?=\\s|$)");
    private static final Pattern MODE_PATTERN = Pattern.compile("^\\[[A-Za-z]+\\](\\[[A-Za-z]+\\])*(?=\\s|$)");
    private static final Pattern DISCIPLINE_EXTRACTOR_PATTERN = Pattern.compile("\\[([A-Za-z]+)\\]");

    private static final Set<String> SELF_TYPES = Set.of("Equipment", "Retainer");

    private PlayModeParser() {
    }

    public record Result(String preamble, List<PlayMode> modes, boolean multiMode, boolean doNotReplace, boolean unique) {
    }

    public static Result parse(List<String> types, String cardText) {
        String text = cardText == null ? "" : cardText;
        List<String> lines = new ArrayList<>(Arrays.asList(text.split("\n")));

        String firstType = types.isEmpty() ? "" : types.getFirst().toLowerCase();
        if (firstType.equals("conviction")) {
            return new Result(null, List.of(new PlayMode(List.of(), text, PlayTarget.SOMETHING)), false, false, false);
        }
        if (firstType.equals("power")) {
            return new Result(null, List.of(new PlayMode(List.of(), text, PlayTarget.SELF)), false, false, false);
        }

        // Some cards (e.g. Make the Misère) have two lines of preamble.
        List<String> preambleLines = new ArrayList<>(1);
        while (!(lines.size() == 1 || lines.getFirst().startsWith("["))) {
            preambleLines.add(lines.removeFirst());
        }

        String preamble = null;
        boolean unique = false;
        boolean doNotReplace = false;
        boolean multiMode = false;
        if (!preambleLines.isEmpty()) {
            preamble = String.join("\n", preambleLines);
            String p = preamble.toLowerCase();
            if (p.contains("unique")) unique = true;
            if (p.contains("do not replace")) doNotReplace = true;
            if (p.contains("more than one discipline can be used when playing this card")
                    || p.contains("more than one discipline can be used to play this card")) {
                multiMode = true;
            }
        }

        List<PlayMode> modes = buildModes(types, preamble, lines);
        return new Result(preamble, modes, multiMode, doNotReplace, unique);
    }

    private static List<PlayMode> buildModes(List<String> types, String preamble, List<String> lines) {
        // Mutable staging mirror of the old LibraryCardMode, so the "[as above]"
        // back-reference can read a prior mode's resolved target.
        record Staged(List<String> disciplines, String text, PlayTarget[] target) {
        }
        List<Staged> staged = new ArrayList<>(lines.size());

        for (String line : lines) {
            boolean orMode = false;
            String prefix = null;
            String modeText = line;
            List<String> disciplines = new ArrayList<>();

            if (line.startsWith("[")) {
                Matcher orMatcher = MODE_OR_PATTERN.matcher(line);
                Matcher discMatcher = MODE_PATTERN.matcher(line);
                if (orMatcher.find()) {
                    orMode = true;
                    prefix = orMatcher.group();
                    modeText = line.substring(orMatcher.end()).trim();
                } else if (discMatcher.find()) {
                    prefix = discMatcher.group();
                    modeText = line.substring(discMatcher.end()).trim();
                }
                if (prefix != null) {
                    Matcher m = DISCIPLINE_EXTRACTOR_PATTERN.matcher(prefix);
                    while (m.find()) {
                        disciplines.add(m.group(1));
                    }
                }
            }
            modeText = modeText.trim();

            PlayTarget[] target = new PlayTarget[1];
            if (PUT_INTO_UNCONTROLLED_PATTERN.matcher(modeText).matches()) {
                target[0] = PlayTarget.INACTIVE_REGION;
            } else if (PUT_ON_SELF_PATTERN.matcher(modeText).matches() || types.stream().anyMatch(SELF_TYPES::contains)) {
                target[0] = PlayTarget.SELF;
            } else if (REMOVE_FROM_GAME_PATTERN.matcher(modeText).matches()) {
                target[0] = PlayTarget.REMOVE_FROM_GAME;
            } else if (PUT_ON_CONTROLLED_PATTERN.matcher(modeText).matches()) {
                target[0] = PlayTarget.MINION_YOU_CONTROL;
            } else if (PUT_ON_SOMETHING_PATTERN.matcher(modeText).matches()) {
                target[0] = PlayTarget.SOMETHING;
            } else if ((types.stream().anyMatch("master"::equalsIgnoreCase)
                    && preamble != null
                    && (preamble.toLowerCase().contains("location") || preamble.toLowerCase().contains("trophy")))
                    || types.stream().anyMatch("ally"::equalsIgnoreCase)
                    || types.stream().anyMatch("event"::equalsIgnoreCase)
                    || PUT_INTO_PLAY_PATTERN.matcher(modeText).matches()) {
                target[0] = PlayTarget.READY_REGION;
            } else {
                Matcher matcher = AS_ABOVE_PATTERN.matcher(modeText);
                if (matcher.matches()) {
                    String disciplineString = matcher.group("disc");
                    Staged reference;
                    if (disciplineString == null) {
                        reference = staged.isEmpty() ? null : staged.getLast();
                    } else {
                        List<String> discReference = Arrays.asList(disciplineString.split("[\\[\\]]+"));
                        reference = staged.stream().filter(s -> s.disciplines().equals(discReference)).findFirst().orElse(null);
                        if (reference == null) {
                            logger.warn("could not resolve '[as above]' reference for '{}' among {}", modeText, disciplines);
                        }
                    }
                    if (reference != null) {
                        target[0] = reference.target()[0];
                    }
                }
            }

            if (orMode) {
                for (String discipline : disciplines) {
                    staged.add(new Staged(List.of(discipline), modeText, new PlayTarget[]{target[0]}));
                }
            } else {
                staged.add(new Staged(List.copyOf(disciplines), modeText, target));
            }
        }

        List<PlayMode> modes = new ArrayList<>(staged.size());
        for (Staged s : staged) {
            modes.add(new PlayMode(s.disciplines(), s.text(), s.target()[0]));
        }
        return modes;
    }
}
