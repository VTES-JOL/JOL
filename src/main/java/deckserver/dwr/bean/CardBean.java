package deckserver.dwr.bean;

import deckserver.game.cards.CardEntry;

public class CardBean {

    private final String id;
    private final String name;
    private final String[] text;

    public CardBean(CardEntry card) {
        id = card.getCardId();
        name = card.getName();
        text = card.getFullText();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getText() {
        return text;
    }
}
