package net.deckserver.game.storage.cards.importer;

import net.deckserver.game.storage.cards.CardSet;

import java.nio.file.Path;

public class SetImporter extends AbstractImporter<CardSet> {

    private static final int FIELD_ID = 0;
    private static final int FIELD_CODE = 1;
    private static final int FIELD_NAME = 3;

    public SetImporter(Path dataPath) {
        super(dataPath);
    }

    @Override
    public CardSet map(String[] lineData) {
        String id =  lineData[FIELD_ID].trim();
        String code = lineData[FIELD_CODE].trim();
        String name = lineData[FIELD_NAME].trim();
        return new CardSet(id, code, name);
    }
}
