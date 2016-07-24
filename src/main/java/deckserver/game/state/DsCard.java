package deckserver.game.state;

import deckserver.client.JolAdminFactory;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

class DsCard extends CardBox implements Card {

    private String id;
    private String card;
    private CardBox parent;

    private static final Logger logger = getLogger(DsCard.class);

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
            return JolAdminFactory.INSTANCE.getAllCards().getCardById(card).getName();
        } catch (Throwable t) {
            logger.error("Error finding card " + this.card);
            return "ERROR CARD";
        }
    }

    public SCardContainer getParent() {
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
