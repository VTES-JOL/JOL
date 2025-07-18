package net.deckserver.game.ui.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.Location;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DsLocation extends DsCardContainer implements Location {

    private String lname;
    private String owner;

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

    public void setName(String name) {
        this.lname = name;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    public void shuffle(int num) {
        List<Card> sCards = Arrays.asList(getCards());
        if (num == 0) num = sCards.size();
        if (num > sCards.size()) num = sCards.size();
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
