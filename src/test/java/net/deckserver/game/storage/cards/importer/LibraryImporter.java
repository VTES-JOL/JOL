package net.deckserver.game.storage.cards.importer;

import lombok.extern.slf4j.Slf4j;
import net.deckserver.game.storage.cards.LibraryCard;
import net.deckserver.game.storage.cards.LibraryCardMode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class LibraryImporter extends AbstractImporter<LibraryCard> {

    private static final int FIELD_ID = 0;
    private static final int FIELD_NAME = 1;
    private static final int FIELD_ALIASES = 2;
    private static final int FIELD_TYPE = 3;
    private static final int FIELD_REQUIREMENTS_CLAN = 4;
    private static final int FIELD_REQUIREMENTS_PATH = 5;
    private static final int FIELD_REQUIREMENTS_DISCIPLINE = 6;
    private static final int FIELD_POOL_COST = 7;
    private static final int FIELD_BLOOD_COST = 8;
    private static final int FIELD_CONVICTION_COST = 9;
    private static final int FIELD_BURN_OPTION = 10;
    private static final int FIELD_TEXT = 11;
    private static final int FIELD_SET = 13;
    private static final int FIELD_BANNED = 14;

    private static final Function<String, Boolean> BURN_OPTION = (text) -> text.equals("Y") || text.equalsIgnoreCase("Yes");

    private static final Pattern PUT_INTO_PLAY_PATTERN = Pattern.compile(".*[Pp]ut this card in(?:to)? play.*");
    private static final Pattern PUT_INTO_UNCONTROLLED_PATTERN = Pattern.compile(".*[Pp]ut this card in(?:to)? play in your uncontrolled region.*");
    private static final Pattern PUT_ON_ACTING_PATTERN = Pattern.compile(".*[Pp]ut this card on the acting.*");
    private static final Pattern PUT_ON_CONTROLLED_PATTERN = Pattern.compile(".*[Pp]ut this card on a minion you control.*");
    private static final Pattern PUT_ON_SELF_PATTERN = Pattern.compile(".*[Pp]ut this card on this.*");
    private static final Pattern PUT_ON_SOMETHING_PATTERN = Pattern.compile(".*[Pp]ut this card on.*");
    private static final Pattern AS_ABOVE_PATTERN = Pattern.compile("As (\\[(?<disc>.*)])? ?above.*");
    private static final Pattern REMOVE_FROM_GAME_PATTERN = Pattern.compile(".*[Rr]emove this card from the game.*");
    private static final Pattern TROPHY_PATTERN = Pattern.compile("Master. Trophy.");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(Unique|Master:) location");
    private static final Pattern EQUIPEMENT_LOCATION_PATTERN = Pattern.compile("does not count as equipment");
    private static final Pattern ONE_PER_ROUND_PATTERN = Pattern.compile("Only one (.*?) (?:may|can) each round.");
    private static final Pattern ONE_PER_TURN_PATTERN = Pattern.compile("Only one (.*?) (?:may|can) be played each turn.");
    private static final Pattern ONE_PER_GAME_PATTERN = Pattern.compile("Only one (.*?) can be played(?: or called)? (?:in|during) a game.");
    private static final Pattern MODE_OR_PATTERN = Pattern.compile("^\\[[A-Za-z]+\\]\\s+or\\s+\\[[A-Za-z]+\\](?=\\s|$)");
    private static final Pattern MODE_PATTERN = Pattern.compile("^\\[[A-Za-z]+\\](\\[[A-Za-z]+\\])*(?=\\s|$)");
    private static final Pattern DISCIPLINE_EXTRACTOR_PATTERN = Pattern.compile("\\[([A-Za-z]+)\\]");

    private static final List<String> DISCIPLINES = Arrays.asList("ani", "obe", "cel", "dom", "dem", "for", "san", "thn", "vic", "pro", "chi", "val", "mel", "nec", "obf", "obl", "pot", "qui", "pre", "ser", "tha", "aus", "vis", "abo", "myt", "dai", "spi", "tem", "obt", "str", "mal", "flight");

    private final boolean playTestMode;

    public LibraryImporter(Path basePath, String filePrefix) {
        super(basePath, filePrefix);
        this.playTestMode = false;
    }

    public LibraryImporter(Path basePath, String filePrefix, boolean playTestMode) {
        super(basePath, filePrefix);
        this.playTestMode = playTestMode;
    }

    @Override
    public LibraryCard map(String[] lineData) {
        String originalName = lineData[FIELD_NAME].trim();
        List<String> aliases = Arrays.stream(lineData[FIELD_ALIASES].split(";")).map(String::trim).collect(Collectors.toList());
        Names names = Utils.generateNames(originalName, aliases, false, null);

        LibraryCard card = new LibraryCard();
        card.setId(lineData[FIELD_ID]);
        card.setName(originalName);
        card.setDisplayName(names.getDisplayName());
        card.setName(names.getUniqueName());
        card.setNames(names.getNames());
        card.setPlayTest(playTestMode);
        Utils.split(lineData[FIELD_TYPE], "/").ifPresent(card::setType);

        // Calculate requirements
        Utils.split(lineData[FIELD_REQUIREMENTS_CLAN], "/").ifPresent(card::setClans);
        Utils.split(lineData[FIELD_REQUIREMENTS_DISCIPLINE], "/").ifPresent(card::setDisciplines);
        Utils.getClean(lineData[FIELD_REQUIREMENTS_PATH]).ifPresent(card::setPath);

        // Calculate cost
        Utils.getClean(lineData[FIELD_POOL_COST]).ifPresent(card::setPool);
        Utils.getClean(lineData[FIELD_BLOOD_COST]).ifPresent(card::setBlood);
        Utils.getClean(lineData[FIELD_CONVICTION_COST]).ifPresent(card::setConviction);

        Utils.getClean(lineData[FIELD_BURN_OPTION]).map(BURN_OPTION).ifPresent(card::setBurnOption);
        Utils.getClean(lineData[FIELD_TEXT]).ifPresent(card::setText);
        Utils.getClean(lineData[FIELD_BANNED]).map(banned -> !banned.isEmpty()).ifPresent(card::setBanned);

        Utils.getClean(lineData[FIELD_SET]).map(Utils::getSets).ifPresent(card::setSets);

        List<String> lines = new ArrayList<>(Arrays.asList(card.getText().split("\n")));
        List<String> preambleLines = new ArrayList<>(1);

        switch (card.getType().get(0).toLowerCase()) {
            case "conviction":
                return parsePowerOrConviction(card, LibraryCardMode.Target.SOMETHING);
            case "power":
                return parsePowerOrConviction(card, LibraryCardMode.Target.SELF);
        }

        //Some cards like Make the Misere have two lines of preamble
        while (!(lines.size() == 1 || lines.get(0).startsWith("[")))
            preambleLines.add(lines.remove(0));

        if (!preambleLines.isEmpty()) {
            String preamble = String.join("\n", preambleLines);
            card.setPreamble(preamble);

            // Preamble checks
            String p = preamble.toLowerCase();
            if (p.contains("unique")) card.setUnique(true);
            if (p.contains("do not replace")) card.setDoNotReplace(true);
            if (p.contains("more than one discipline can be used when playing this card")
                    || p.contains("more than one discipline can be used to play this card"))
                card.setMultiMode(true);
        }
        setModes(card, lines);
        return card;
    }

    /**
     * Powers and convictions behave differently than other minion cards in
     * that their various modes can only be used after they have been played on
     * a minion. The act of playing them on a minion is like playing a card
     * with a single mode, so we treat them as having a single mode here. A
     * potential future feature would be: clicking one of these cards that
     * is already on a minion displays the card modal with separate modes that
     * could then be used for individual effect.
     */
    LibraryCard parsePowerOrConviction(LibraryCard card, LibraryCardMode.Target target) {
        LibraryCardMode mode = new LibraryCardMode();
        mode.setText(card.getText());
        mode.setTarget(target);
        List<LibraryCardMode> modes = new ArrayList<>(1);
        modes.add(mode);
        card.setModes(modes);
        return card;
    }

    void setModes(LibraryCard card, List<String> lines) {
        List<LibraryCardMode> modes = new ArrayList<>(lines.size());
        for (String line : lines) {
            LibraryCardMode mode = new LibraryCardMode();
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
            mode.setText(modeText.trim());

            if (PUT_INTO_UNCONTROLLED_PATTERN.matcher(mode.getText()).matches())
                mode.setTarget(LibraryCardMode.Target.INACTIVE_REGION);
            else if (card.getType().stream().anyMatch("equipment"::equalsIgnoreCase)
                    || card.getType().stream().anyMatch("retainer"::equalsIgnoreCase)
                    || ((card.getType().stream().anyMatch("action"::equalsIgnoreCase)
                    || card.getType().stream().anyMatch("action modifier"::equalsIgnoreCase))
                    && PUT_ON_ACTING_PATTERN.matcher(mode.getText()).matches())
                    || PUT_ON_SELF_PATTERN.matcher(mode.getText()).matches())
                mode.setTarget(LibraryCardMode.Target.SELF);
            else if (REMOVE_FROM_GAME_PATTERN.matcher(mode.getText()).matches())
                mode.setTarget(LibraryCardMode.Target.REMOVE_FROM_GAME);
            else if (PUT_ON_CONTROLLED_PATTERN.matcher(mode.getText()).matches())
                mode.setTarget(LibraryCardMode.Target.MINION_YOU_CONTROL);
            else if (PUT_ON_SOMETHING_PATTERN.matcher(mode.getText()).matches())
                mode.setTarget(LibraryCardMode.Target.SOMETHING);
            else if ((card.getType().stream().anyMatch("master"::equalsIgnoreCase)
                    && card.getPreamble() != null
                    && (card.getPreamble().toLowerCase().contains("location") || card.getPreamble().toLowerCase().contains("trophy")))
                    || card.getType().stream().anyMatch("ally"::equalsIgnoreCase)
                    || card.getType().stream().anyMatch("event"::equalsIgnoreCase)
                    || PUT_INTO_PLAY_PATTERN.matcher(mode.getText()).matches())
                mode.setTarget(LibraryCardMode.Target.READY_REGION);
            else {
                Matcher matcher = AS_ABOVE_PATTERN.matcher(modeText);
                if (matcher.matches()) {
                    String disciplineString = matcher.group("disc");
                    LibraryCardMode reference;
                    if (disciplineString == null) {
                        reference = modes.get(modes.size() - 1);
                    } else {
                        List<String> discReference = Arrays.asList(disciplineString.split("[\\[\\]]+"));
                        reference = modes.stream().filter(m -> m.getDisciplines().equals(discReference)).findFirst().orElse(null);
                        if (reference == null) {
                            System.out.printf(
                                    "WARNING! %s: Could not find match for '%s' among modes [%s]%n",
                                    card.getName(), disciplines, modes);
                        }
                    }
                    if (reference != null) {
                        mode.setTarget(reference.getTarget());
                    }
                }
            }
            if (orMode) {
                for (String discipline : disciplines) {
                    LibraryCardMode discMode = new LibraryCardMode(mode);
                    discMode.setDisciplines(List.of(discipline));
                    modes.add(discMode);
                }
            } else {
                mode.setDisciplines(disciplines);
                modes.add(mode);
            }
        }
        card.setModes(modes);
    }
}
