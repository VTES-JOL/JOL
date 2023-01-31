package net.deckserver.game.ui.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class DsLocation extends DsCardContainer implements Location {

    private final String lname;

    DsLocation(String name, DsGame game) {
        super(game);
        this.lname = name;
    }

    public void initCards(List<String> cardIds, String owner) {
        cards.clear();
        for (String cardId : cardIds) {
            Card c = new DsCard(getGame().getNewId() + "", cardId, owner);
            addCard(c, false);
            getGame().addCard(c);
        }
    }

    public void shuffle(int num) {
        List<Card> sCards = Arrays.asList(getCards());
        List<Card> subList = sCards.subList(0, num);
        for (int x = 0; x < 20; x++) {
            Collections.shuffle(subList, new SecureRandom());
        }
        cards.clear();
        cards.addAll(sCards);
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
