package net.deckserver.game;

import java.util.List;

public interface Card extends CardContainer, Notation {

    // Card Full Name
    String getName();

    // Instance ID
    String getId();

    // Card Detail ID
    String getCardId();

    // Parent Region / Card
    CardContainer getParent();

    // Attached Cards
    List<Card> getChildren();

}
