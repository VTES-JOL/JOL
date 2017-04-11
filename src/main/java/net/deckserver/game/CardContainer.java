package net.deckserver.game;

import java.util.List;

public interface CardContainer {

    void addCard(Card card, int position);

    void addCard(Card card);

    void removeCard(Card card);

    void setCards(List<Card> cards);

    List<Card> getCards();
}
