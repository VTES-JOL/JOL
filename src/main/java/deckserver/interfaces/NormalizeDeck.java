package deckserver.interfaces;

public interface NormalizeDeck extends Deck {

    String getDeckString();

    String getFilteredDeck();

    CardSet findCardName(String text);

    String[] getErrorLines();

    int getCryptSize();

    int getLibSize();

    String getGroups();

}