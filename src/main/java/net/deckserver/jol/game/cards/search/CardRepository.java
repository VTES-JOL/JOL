package net.deckserver.jol.game.cards.search;

import net.deckserver.jol.game.cards.CardEntry;
import net.deckserver.jol.game.cards.CardType;

import java.util.Collection;
import java.util.Set;

/**
 * Created by shannon on 26/07/2016.
 */
public interface CardRepository {

    String getId(String name);
    Collection<CardEntry> findAll();
    CardEntry findById(String id);
    Set<CardEntry> findByType(String query, CardType type);
    void refresh();
}
