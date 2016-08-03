package net.deckserver.jol.game.cards.search;

import net.deckserver.jol.game.cards.CardEntry;
import net.deckserver.jol.game.cards.CardType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Created by shannon on 26/07/2016.
 */
public interface CardRepository {

    Collection<CardEntry> findAll();

    CardEntry findById(String id);

    Collection<CardEntry> findByName(String name);

    Collection<CardEntry> findByType(String query, EnumSet<CardType> typeFilter);

    void refresh();
}
