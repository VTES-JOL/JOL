package net.deckserver.game;

public interface Region extends CardContainer, Notation {

    String getName();

    // Shuffle all the cards in region
    void shuffle();

    // Shuffle only the first X cards
    void shuffle(int limit);

    Card getCard(int index);

    Card getCard();
}
