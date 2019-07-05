package net.deckserver.game.storage.cards.importer;

import lombok.extern.slf4j.Slf4j;
import net.deckserver.game.storage.cards.LibraryCard;
import net.deckserver.game.storage.cards.LibraryCardMode;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class LibraryImporter extends AbstractImporter<LibraryCard> {

    private static final int FIELD_ID = 0;
    private static final int FIELD_NAME = 1;
    private static final int FIELD_ALIASES = 2;
    private static final int FIELD_TYPE = 3;
    private static final int FIELD_REQUIREMENTS_CLAN = 4;
    private static final int FIELD_REQUIREMENTS_DISCIPLINE = 5;
    private static final int FIELD_POOL_COST = 6;
    private static final int FIELD_BLOOD_COST = 7;
    private static final int FIELD_CONVICTION_COST = 8;
    private static final int FIELD_BURN_OPTION = 9;
    private static final int FIELD_TEXT = 10;
    private static final int FIELD_REQUIREMENTS_OTHER = 13;
    private static final int FIELD_BANNED = 14;

    private Function<String, Boolean> burnOption = (text) -> text.equals("Y") || text.equalsIgnoreCase("Yes");

    public LibraryImporter(Path dataPath) {
        super(dataPath);
    }

    @Override
    public LibraryCard map(String[] lineData) {
        String originalName = lineData[FIELD_NAME].trim();
        List<String> aliases = Arrays.stream(lineData[FIELD_ALIASES].split(";")).map(String::trim).collect(Collectors.toList());
        Set<String> names = Utils.otherNames(originalName, false, aliases);
        String displayName = Utils.generateDisplayName(originalName, false);

        LibraryCard card = new LibraryCard();
        card.setId(lineData[FIELD_ID]);
        card.setName(originalName);
        card.setDisplayName(displayName);
        card.setNames(names);
        Utils.split(lineData[FIELD_TYPE], "/").ifPresent(card::setType);

        // Calculate requirements
        Utils.split(lineData[FIELD_REQUIREMENTS_CLAN], "/").ifPresent(card::setClans);
        Utils.split(lineData[FIELD_REQUIREMENTS_DISCIPLINE], "/").ifPresent(card::setDisciplines);
        Utils.split(lineData[FIELD_REQUIREMENTS_OTHER], ",").ifPresent(card::setRequirements);

        // Calculate cost
        Utils.getClean(lineData[FIELD_POOL_COST]).ifPresent(card::setPool);
        Utils.getClean(lineData[FIELD_BLOOD_COST]).ifPresent(card::setBlood);
        Utils.getClean(lineData[FIELD_CONVICTION_COST]).ifPresent(card::setConviction);

        Utils.getClean(lineData[FIELD_BURN_OPTION]).map(burnOption).ifPresent(card::setBurnOption);
        Utils.getClean(lineData[FIELD_TEXT]).ifPresent(card::setText);
        Utils.getClean(lineData[FIELD_BANNED]).map(banned -> !banned.isEmpty()).ifPresent(card::setBanned);

        List<String> lines = new ArrayList(Arrays.asList(card.getText().split("\n")));
        List<String> preambleLines = new ArrayList(1);

        //Some cards like Make the Misere have two lines of preamble
        while (!(lines.size() == 1 || lines.get(0).startsWith("[")))
            preambleLines.add(lines.remove(0));

        if (preambleLines.size() > 0) {
            String preamble = String.join("\n", preambleLines);
            card.setPreamble(preamble);

            // Preamble checks
            String p = preamble.toLowerCase();
            if (p.contains("unique")) card.setUnique(true);
            if (p.contains("do not replace")) card.setDoNotReplace(true);
            if (p.contains("more than one discipline can be used when playing this card")) card.setMultiMode(true);
        }
        setModes(card, lines);
        return card;
    }
    void setModes(LibraryCard card, List<String> lines) {
        List<LibraryCardMode> modes = new ArrayList(lines.size());
        for (String line: lines) {
            LibraryCardMode mode = new LibraryCardMode();
            if (line.startsWith("[")) {
                String[] disciplinesAndText = line.split(" ", 2);
                List<String> disciplines = Arrays
                    .stream(disciplinesAndText[0].split("[\\[\\]]+"))
                    .filter(d -> !d.equals(""))
                    .collect(Collectors.toList());
                mode.setDisciplines(disciplines);
                mode.setText(disciplinesAndText[1]);
            }
            else mode.setText(line);
            modes.add(mode);
        }
        card.setModes(modes);
    }
}
