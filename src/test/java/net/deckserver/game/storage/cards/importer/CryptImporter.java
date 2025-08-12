package net.deckserver.game.storage.cards.importer;

import lombok.extern.slf4j.Slf4j;
import net.deckserver.game.storage.cards.CryptCard;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class CryptImporter extends AbstractImporter<CryptCard> {

    private static final int FIELD_ID = 0;
    private static final int FIELD_NAME = 1;
    private static final int FIELD_ALIASES = 2;
    private static final int FIELD_TYPE = 3;
    private static final int FIELD_CLAN = 4;
    private static final int FIELD_PATH = 5;
    private static final int FIELD_ADVANCED = 6;
    private static final int FIELD_GROUP = 7;
    private static final int FIELD_CAPACITY = 8;
    private static final int FIELD_DISCIPLINES = 9;
    private static final int FIELD_TEXT = 10;
    private static final int FIELD_SET = 11;
    private static final int FIELD_TITLE = 12;
    private static final int FIELD_BANNED = 13;
    Predicate<? super String> UNIQUE_FILTER = (text) -> text.contains("are not unique") || text.contains("non-unique");
    Predicate<String> INFERNAL_FILTER = (text) -> text.contains("Infernal.");

    private final boolean playTestMode;

    public CryptImporter(Path basePath, String filePrefix) {
        super(basePath, filePrefix);
        this.playTestMode = false;
    }

    public CryptImporter(Path basePath, String filePrefix, boolean playTestMode) {
        super(basePath, filePrefix);
        this.playTestMode = playTestMode;
    }

    @Override
    public CryptCard map(String[] lineDataOriginal) {
        String[] lineData = enhance(lineDataOriginal);
        String originalName = lineData[FIELD_NAME].trim();
        List<String> aliases = Arrays.stream(lineData[FIELD_ALIASES].split(";")).map(String::trim).collect(Collectors.toList());
        boolean advanced = Utils.getClean(lineData[FIELD_ADVANCED]).map(String::toLowerCase).map("advanced"::equals).orElse(false);
        Integer group = Utils.getClean(lineData[FIELD_GROUP]).filter(s -> !s.equals("ANY")).map(Integer::valueOf).orElse(null);
        Names names = Utils.generateNames(originalName, aliases, advanced, group);

        CryptCard card = new CryptCard();
        card.setId(lineData[FIELD_ID]);
        card.setName(originalName);
        card.setDisplayName(names.getDisplayName());
        card.setName(names.getUniqueName());
        card.setNames(names.getNames());
        card.setPlayTest(playTestMode);
        Utils.getClean(lineData[FIELD_TYPE]).ifPresent(card::setType);
        Utils.getClean(lineData[FIELD_CLAN]).ifPresent(card::setClan);

        if (advanced) card.setAdvanced(advanced);
        Utils.getClean(lineData[FIELD_GROUP]).ifPresent(card::setGroup);
        Utils.getClean(lineData[FIELD_CAPACITY]).map(Integer::valueOf).ifPresent(card::setCapacity);

        Utils.getClean(lineData[FIELD_DISCIPLINES])
                .map(disciplineLine -> Arrays.asList(disciplineLine.split("\\s")))
                .ifPresent(card::setDisciplines);

        Utils.getClean(lineData[FIELD_TEXT]).ifPresent(card::setText);
        Utils.getClean(lineData[FIELD_TEXT]).map(String::toLowerCase).filter(UNIQUE_FILTER).ifPresent(s -> card.setUnique(false));
        Utils.getClean(lineData[FIELD_TEXT]).filter(INFERNAL_FILTER).ifPresent(s -> card.setInfernal(true));
        Utils.getClean(lineData[FIELD_TITLE]).ifPresent(card::setTitle);

        Utils.getClean(lineData[FIELD_BANNED]).map(banned -> !banned.isEmpty()).ifPresent(card::setBanned);
        Utils.getClean(lineData[FIELD_SET]).map(Utils::getSets).ifPresent(card::setSets);

        card.setSect(determineSect(card.getClan(), card.getText()));
        Optional.ofNullable(card.getTitle()).map(this::determineVotes).ifPresent(card::setVotes);

        return card;
    }

    private String[] enhance(String[] lineData) {
        lineData = Arrays.copyOf(lineData, 49);
        return lineData;
    }

    private void populateDiscipline(Map<String, Integer> disciplines, String disciplineCell, String disciplineName) {
        Utils.getClean(disciplineCell)
                .map(Integer::valueOf)
                .filter(x -> x != 0)
                .ifPresent(x -> disciplines.put(disciplineName, x));
    }

    private String determineSect(String clan, String text) {
        String sect;
        switch (clan) {
            case "Brujah":
            case "Malkavian":
            case "Nosferatu":
            case "Toreador":
            case "Tremere":
            case "Ventrue":
            case "Caitiff":
                sect = "Camarilla";
                break;
            case "Brujah antitribu":
            case "Malkavian antitribu":
            case "Nosferatu antitribu":
            case "Toreador antitribu":
            case "Tremere antitribu":
            case "Ventrue antitribu":
            case "Gangrel antitribu":
            case "Salubri antitribu":
            case "Lasombra":
            case "Tzimisce":
            case "Pander":
            case "Ahrimane":
            case "Blood Brother":
            case "Harbinger of Skulls":
            case "Kiasyd":
                sect = "Sabbat";
                break;
            case "Akunanse":
            case "Guruhi":
            case "Ishtarri":
            case "Osebo":
                sect = "Laibon";
                break;
            default:
                sect = "Independent";
        }
        String textSect = null;
        if (text.matches("^Sabbat.*?[:.]")) {
            textSect = "Sabbat";
        } else if (text.matches("^(Independent anarch|Anarch).*?[:.]")) {
            textSect = "Anarch";
        } else if (text.matches("^Independent.*?[:.]")) {
            textSect = "Independent";
        } else if (text.matches("^Camarilla.*?[:.]")) {
            textSect = "Camarilla";
        }
        if (textSect != null && !sect.equals(textSect)) {
            log.debug("Overwriting {} with {} because of [{}]", sect, textSect, text);
            sect = textSect;
        }
        return sect;
    }

    private String determineVotes(String title) {
        String votes = "";
        switch (title) {
            case "1 vote":
            case "primogen":
            case "bishop":
                votes = "1";
                break;
            case "2 votes":
            case "archbishop":
            case "prince":
            case "magaji":
            case "baron":
                votes = "2";
                break;
            case "justicar":
            case "cardinal":
                votes = "3";
                break;
            case "inner circle":
            case "regent":
                votes = "4";
                break;
            case "priscus":
                votes = "P";
                break;
        }
        return votes;
    }
}
