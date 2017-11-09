package net.deckserver.game.storage.cards.importer;

import lombok.extern.slf4j.Slf4j;
import net.deckserver.game.storage.cards.LibraryCard;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class LibraryImporter extends AbstractImporter<LibraryCard> {

    private static final int FIELD_NAME = 0;
    private static final int FIELD_TYPE = 1;
    private static final int FIELD_REQUIREMENTS_CLAN = 2;
    private static final int FIELD_REQUIREMENTS_DISCIPLINE = 3;
    private static final int FIELD_POOL_COST = 4;
    private static final int FIELD_BLOOD_COST = 5;
    private static final int FIELD_CONVICTION_COST = 6;
    private static final int FIELD_BURN_OPTION = 7;
    private static final int FIELD_TEXT = 8;
    private static final int FIELD_REQUIREMENTS_OTHER = 11;
    private static final int FIELD_BANNED = 12;

    public LibraryImporter(InputStream dataStream) {
        super(dataStream);
    }

    public LibraryImporter(InputStream dataStream, InputStream aliasStream) {
        super(dataStream, aliasStream);
    }

    @Override
    public LibraryCard map(String[] lineData) {
        String originalName = lineData[FIELD_NAME].trim();
        List<String> aliases = getAliases().stream().filter(cardAlias -> cardAlias.getCard().equals(originalName)).findFirst().map(CardAlias::getAliases).orElse(new ArrayList<>());
        Set<String> names = Utils.otherNames(originalName, false, aliases);
        String displayName = Utils.generateDisplayName(originalName, false);

        LibraryCard card = new LibraryCard();
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

        Utils.getClean(lineData[FIELD_BURN_OPTION]).map(burnOption -> burnOption.equals("Y")).ifPresent(card::setBurnOption);
        Utils.getClean(lineData[FIELD_TEXT]).ifPresent(card::setText);
        Utils.getClean(lineData[FIELD_BANNED]).map(banned -> !banned.isEmpty()).ifPresent(card::setBanned);

        return card;
    }
}
