package deckserver.game.state;

import deckserver.dwr.Utils;

import java.util.Collections;

class LocBox extends CardBox implements Location {

    private final String lname;

    LocBox(String name, DsGame game) {
        super(game);
        this.lname = name;
    }

    public void initCards(String[] cardIds) {
        cards.clear();
        for (String cardId : cardIds) {
            Card c = new DsCard(getGame().getNewId() + "", cardId);
            addCard(c, false);
            getGame().addCard(c);
        }
    }

    public void shuffle(int num) {
        SCard[] sCards = getCards();
        Utils.shuffle(sCards, num);
        cards.clear();
        Collections.addAll(cards, (Card[]) sCards);
    }

    public String getName() {
        return lname;
    }

    public SCard getCard(int index) {
        return cards.get(index);
    }

    public SCard getFirstCard() {
        return cards.getFirst();
    }

}
