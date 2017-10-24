package net.deckserver.game.ui.state;

import net.deckserver.Utils;
import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;

import java.util.Collections;

class DsLocation extends DsCardContainer implements Location {

    private final String lname;

    DsLocation(String name, DsGame game) {
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
        Card[] sCards = getCards();
        Utils.shuffle(sCards, num);
        cards.clear();
        Collections.addAll(cards, (Card[]) sCards);
    }

    public String getName() {
        return lname;
    }

    public Card getCard(int index) {
        return cards.get(index);
    }

    public Card getFirstCard() {
        return cards.getFirst();
    }

}
