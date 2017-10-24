package net.deckserver.game.ui.state;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.CardContainer;

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
            return JolAdmin.getInstance().getAllCards().getCardById(card).getName();
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

}
