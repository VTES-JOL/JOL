package net.deckserver.game.storage.cards.importer;

import lombok.extern.slf4j.Slf4j;
import net.deckserver.game.storage.cards.CryptCard;

import java.io.InputStream;
import java.util.*;

@Slf4j
public class CryptImporter extends AbstractImporter<CryptCard> {

    private static final int FIELD_NAME = 0;
    private static final int FIELD_TYPE = 1;
    private static final int FIELD_CLAN = 2;
    private static final int FIELD_ADVANCED = 3;
    private static final int FIELD_GROUP = 4;
    private static final int FIELD_CAPACITY = 5;
    private static final int FIELD_DISCIPLINES = 6;
    private static final int FIELD_TEXT = 7;
    private static final int FIELD_TITLE = 9;
    private static final int FIELD_BANNED = 10;
    private static final int FIELD_DISCIPLINE_ABOMBWE = 12;
    private static final int FIELD_DISCIPLINE_ANIMALISM = 13;
    private static final int FIELD_DISCIPLINE_AUSPEX = 14;
    private static final int FIELD_DISCIPLINE_CELERITY = 15;
    private static final int FIELD_DISCIPLINE_CHIMERSTRY = 16;
    private static final int FIELD_DISCIPLINE_DAIMOINON = 17;
    private static final int FIELD_DISCIPLINE_DEMENTATION = 18;
    private static final int FIELD_DISCIPLINE_DOMINATE = 19;
    private static final int FIELD_DISCIPLINE_FORTITUDE = 20;
    private static final int FIELD_DISCIPLINE_MELPOMINEE = 21;
    private static final int FIELD_DISCIPLINE_MYTHERCERIA = 22;
    private static final int FIELD_DISCIPLINE_NECROMANCY = 23;
    private static final int FIELD_DISCIPLINE_OBEAH = 24;
    private static final int FIELD_DISCIPLINE_OBFUSCATE = 25;
    private static final int FIELD_DISCIPLINE_OBTENEBRATION = 26;
    private static final int FIELD_DISCIPLINE_POTENCE = 27;
    private static final int FIELD_DISCIPLINE_PRESENCE = 28;
    private static final int FIELD_DISCIPLINE_PROTEAN = 29;
    private static final int FIELD_DISCIPLINE_QUIETUS = 30;
    private static final int FIELD_DISCIPLINE_SANGUINUS = 31;
    private static final int FIELD_DISCIPLINE_SERPENTIS = 32;
    private static final int FIELD_DISCIPLINE_SPIRITUS = 33;
    private static final int FIELD_DISCIPLINE_TEMPORIS = 34;
    private static final int FIELD_DISCIPLINE_THANATOSIS = 35;
    private static final int FIELD_DISCIPLINE_THAUMATURGY = 36;
    private static final int FIELD_DISCIPLINE_VALEREN = 37;
    private static final int FIELD_DISCIPLINE_VICISSITUDE = 38;
    private static final int FIELD_DISCIPLINE_VISCERATIKA = 39;
    private static final int FIELD_DISCIPLINE_DEFENSE = 40;
    private static final int FIELD_DISCIPLINE_INNOCENCE = 41;
    private static final int FIELD_DISCIPLINE_JUSTICE = 42;
    private static final int FIELD_DISCIPLINE_MARTYRDOM = 43;
    private static final int FIELD_DISCIPLINE_REDEMPTION = 44;
    private static final int FIELD_DISCIPLINE_VENGEANCE = 45;
    private static final int FIELD_DISCIPLINE_VISION = 46;

    private List<CardAlias> cardAliases = new ArrayList<>();

    public CryptImporter(InputStream dataStream) {
        super(dataStream);
    }

    public CryptImporter(InputStream dataStream, InputStream aliasStream) {
        super(dataStream, aliasStream);
    }

    @Override
    public CryptCard map(String[] lineData) {
        String originalName = lineData[FIELD_NAME].trim();
        List<String> aliases = getAliases().stream().filter(cardAlias -> cardAlias.getCard().equals(originalName)).findFirst().map(CardAlias::getAliases).orElse(new ArrayList<>());
        boolean advanced = Utils.getClean(lineData[FIELD_ADVANCED]).map(String::toLowerCase).map("advanced"::equals).orElse(false);
        String displayName = Utils.generateDisplayName(originalName, advanced);
        Set<String> names = Utils.otherNames(originalName, advanced, aliases);

        CryptCard card = new CryptCard();
        card.setName(originalName);
        card.setDisplayName(displayName);
        card.setNames(names);
        Utils.getClean(lineData[FIELD_TYPE]).ifPresent(card::setType);
        Utils.getClean(lineData[FIELD_CLAN]).ifPresent(card::setClan);

        if (advanced) card.setAdvanced(advanced);
        Utils.getClean(lineData[FIELD_GROUP]).ifPresent(card::setGroup);
        Utils.getClean(lineData[FIELD_CAPACITY]).map(Integer::valueOf).ifPresent(card::setCapacity);

        Utils.getClean(lineData[FIELD_DISCIPLINES]).ifPresent(card::setDisciplineList);
        Optional.ofNullable(card.getDisciplineList()).ifPresent(disciplineList -> {
            Map<String, Integer> disciplineMap = new HashMap<>();
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_ABOMBWE], "abombwe");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_ANIMALISM], "animalism");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_AUSPEX], "auspex");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_CELERITY], "celerity");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_CHIMERSTRY], "chimerstry");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_DAIMOINON], "daimoinon");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_DEMENTATION], "dementation");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_DOMINATE], "dominate");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_FORTITUDE], "fortitude");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_MELPOMINEE], "melpominee");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_MYTHERCERIA], "mytherceria");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_NECROMANCY], "necromancy");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_OBEAH], "obeah");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_OBFUSCATE], "obfuscate");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_OBTENEBRATION], "obtenebration");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_POTENCE], "potence");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_PRESENCE], "presence");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_PROTEAN], "protean");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_QUIETUS], "quietus");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_SANGUINUS], "sanguinus");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_SERPENTIS], "serpentis");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_SPIRITUS], "spiritus");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_TEMPORIS], "temporis");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_THANATOSIS], "thanatosis");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_THAUMATURGY], "thaumaturgy");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_VALEREN], "valeren");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_VICISSITUDE], "vicissitude");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_VISCERATIKA], "visceratika");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_DEFENSE], "defense");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_INNOCENCE], "innocence");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_JUSTICE], "justice");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_MARTYRDOM], "martyrdom");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_REDEMPTION], "redemption");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_VENGEANCE], "vengeance");
            populateDiscipline(disciplineMap, lineData[FIELD_DISCIPLINE_VISION], "vision");
            card.setDisciplines(disciplineMap);
        });

        Utils.getClean(lineData[FIELD_TEXT]).ifPresent(card::setText);
        Utils.getClean(lineData[FIELD_TITLE]).ifPresent(card::setTitle);
        Utils.getClean(lineData[FIELD_BANNED]).map(banned -> !banned.isEmpty()).ifPresent(card::setBanned);

        card.setSect(determineSect(card.getClan(), card.getText()));

        return card;
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
}
