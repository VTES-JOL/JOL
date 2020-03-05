/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.deckserver.game.json.deck.CardSummary;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author administrator
 */
public class CardSearch {

    private static final Logger logger = getLogger(CardSearch.class);

    private Map<String, String> nameKeys = new HashMap<>();
    private Map<String, CardEntry> cardTable = new HashMap<>();

    public static CardSearch INSTANCE = new CardSearch(Paths.get(System.getenv("JOL_DATA")));

    private CardSearch(Path cardPath) {
        cardPath = cardPath.resolve("cards").resolve("cards.json");
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType cardSummaryCollectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, CardSummary.class);
        try {
            List<CardSummary> cardList = objectMapper.readValue(cardPath.toFile(), cardSummaryCollectionType);
            cardList.forEach(card -> {
                for (String name : card.getNames()) {
                    nameKeys.put(name.toLowerCase(), card.getJolId());
                }
                CardEntry cardEntry = new CardEntry(card);
                cardTable.put(card.getJolId(), cardEntry);
            });
            logger.info("Read {} keys, {} cards", nameKeys.size(), cardTable.size());
        } catch (IOException e) {
            logger.error("Unable to read cards", e);
        }
    }

    public Collection<CardEntry> getAllCards() {
        return cardTable.values();
    }

    public CardEntry getCardById(String id) {
        CardEntry entry = cardTable.get(id);
        logger.trace("Lookup {} - found {}", id, entry);
        return entry;
    }

    public Collection<CardEntry> searchByType(Collection<CardEntry> set, String type) {
        return searchByField(set, "Cardtype:", type);
    }

    public Collection<CardEntry> searchByText(Collection<CardEntry> set, String text) {
        text = text.toLowerCase();
        Set<CardEntry> v = new HashSet<>();
        for (CardEntry anArr : set) {
            String[] cardText = anArr.getFullText();
            for (String aCardText : cardText) {
                if (aCardText.toLowerCase().indexOf(text) > 0) {
                    v.add(anArr);
                    break;
                }
            }
        }
        return v;
    }

    public Collection<CardEntry> searchByField(Collection<CardEntry> set, String field, String value) {
        List<CardEntry> v = new ArrayList<>();
        value = value.toLowerCase();
        for (CardEntry anArr : set) {
            String[] text = anArr.getFullText();
            for (String aText : text)
                if (aText.startsWith(field)) {
                    if (aText.toLowerCase().indexOf(value, 6) > 0)
                        v.add(anArr);
                    break;
                }
        }
        return v;
    }

    public String getId(String nm) {
        return nameKeys.get(nm.toLowerCase());
    }

    public Set<String> getNames() {
        return nameKeys.keySet();
    }

    public CardEntry findCardExact(String text) throws IllegalArgumentException {
        final String lowerText = StringUtils.stripAccents(text).toLowerCase();
        String key = nameKeys.get(lowerText);
        if (key != null) {
            return cardTable.get(key);
        } else {
            throw new IllegalArgumentException("Card not found");
        }

    }

    public CardEntry findCard(String text) throws IllegalArgumentException {
        final String lowerText = StringUtils.stripAccents(text).toLowerCase();
        if (nameKeys.containsKey(lowerText)) {
            return cardTable.get(nameKeys.get(lowerText));
        } else {
            for (String name : nameKeys.keySet()) {
                if (name.startsWith(lowerText)) {
                    return cardTable.get(nameKeys.get(name));
                }
            }
        }
        throw new IllegalArgumentException("Can't find " + text);
    }
}
