/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.deckserver.storage.json.cards.CardSummary;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author administrator
 */
public class CardSearch {

    private static final Logger logger = getLogger(CardSearch.class);
    public static CardSearch INSTANCE = new CardSearch();
    private final Map<String, String> nameKeys = new HashMap<>();
    private final Map<String, CardSummary> cards = new HashMap<>();

    private CardSearch() {
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType cardSummaryCollectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, CardSummary.class);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream resourceStream = loader.getResourceAsStream("cards.json")) {
            List<CardSummary> cardList = objectMapper.readValue(resourceStream, cardSummaryCollectionType);
            cardList.forEach(card -> {
                for (String name : card.getNames()) {
                    nameKeys.put(name.toLowerCase(), card.getId());
                }
                cards.put(card.getId(), card);
            });
            logger.info("Read {} keys, {} cards", nameKeys.size(), cards.size());
        } catch (IOException e) {
            logger.error("Unable to read cards", e);
        }
    }

    public Optional<CardSummary> findCardExact(String text) {
        final String lowerText = StringUtils.stripAccents(text).toLowerCase();
        String key = nameKeys.get(lowerText);
        if (key != null) {
            CardSummary entry = cards.get(key);
            return Optional.of(entry);
        } else {
            return Optional.empty();
        }
    }

    public Optional<CardSummary> findCard(String text) {
        final String lowerText = StringUtils.stripAccents(text).toLowerCase();
        Optional<CardSummary> entry = Optional.empty();
        if (nameKeys.containsKey(lowerText)) {
            entry = Optional.of(cards.get(nameKeys.get(lowerText)));
        } else {
            for (String name : nameKeys.keySet()) {
                if (name.startsWith(lowerText)) {
                    entry = Optional.of(cards.get(nameKeys.get(name)));
                }
            }
        }
        return entry;
    }

    public Set<CardSummary> autoComplete(String text) {
        return nameKeys.keySet().stream().filter(name -> name.toLowerCase().contains(text.toLowerCase()))
                .map(nameKeys::get)
                .map(cards::get)
                .collect(Collectors.toSet());
    }

    public Collection<CardSummary> allCards() {
        return cards.values();
    }

    public CardSummary get(String id) {
        return cards.get(id);
    }
}
