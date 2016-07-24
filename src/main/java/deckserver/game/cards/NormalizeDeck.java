package deckserver.game.cards;

public interface NormalizeDeck extends Deck {

    String getDeckString();

    CardSet findCardName(String text);

    String[] getErrorLines();

    int getCryptSize();

    int getLibSize();

    String getGroups();

}