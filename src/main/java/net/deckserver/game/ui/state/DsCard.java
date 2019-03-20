package net.deckserver.game.ui.state;

import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.CardContainer;
import net.deckserver.game.storage.cards.CardSearch;

class DsCard extends DsCardContainer implements Card {

    private String id;
    private String card;
    private DsCardContainer parent;

    DsCard(String id, String card) {
        super(null);
        this.id = id;
        this.card = card;
    }

    DsGame getGame() {
        return parent.getGame();
    }

    public String getName() {
        try {
            return CardSearch.INSTANCE.getCardById(card).getName();
        } catch (Throwable t) {
            return "ERROR CARD";
        }
    }

    public CardContainer getParent() {
        return parent;
    }

    void setParent(DsCardContainer parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public String getCardId() {
        return card;
    }

    @Override
    public String toString() {
        return "DsCard{" +
                "id='" + id + '\'' +
                ", card='" + card + '\'' +
                '}';
    }
}
