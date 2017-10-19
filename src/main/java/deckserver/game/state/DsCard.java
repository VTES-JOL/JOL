package deckserver.game.state;

import deckserver.client.JolAdmin;

class DsCard extends CardBox implements Card {

    private String id;
    private String card;
    private CardBox parent;

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

    void setParent(CardBox parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public String getCardId() {
        return card;
    }

}
