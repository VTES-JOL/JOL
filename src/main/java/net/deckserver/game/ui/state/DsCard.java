package net.deckserver.game.ui.state;

import lombok.Getter;
import lombok.Setter;
import net.deckserver.game.interfaces.state.Card;
import net.deckserver.game.interfaces.state.CardContainer;
import net.deckserver.game.storage.cards.CardSearch;

public class DsCard extends DsCardContainer implements Card {

    private String id;
    private String card;
    @Getter @Setter
    private String owner;
    private DsCardContainer parent;

    DsCard(String id, String card, String owner) {
        super(null);
        this.id = id;
        this.card = card;
        this.owner = owner;
    }

    DsGame getGame() {
        return parent.getGame();
    }

    public String getName() {
        try {
            return CardSearch.INSTANCE.get(card).getName();
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
