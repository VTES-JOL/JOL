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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author administrator
 */
public class CardSearch {

    private static final Logger logger = getLogger(CardSearch.class);

    private static final Pattern MARKUP_PATTERN = Pattern.compile("\\[(.*?)\\]");

    private Map<String, String> nameKeys = new HashMap<>();
    private Map<String, CardEntry> cardTable = new HashMap<>();
    private CardEntry[] cardArr;

    public CardSearch(Path cardPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CollectionType cardSummaryCollectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, CardSummary.class);
        List<CardSummary> cardList = objectMapper.readValue(cardPath.toFile(), cardSummaryCollectionType);
        cardList.forEach(card -> {
            for (String name : card.getNames()) {
                nameKeys.put(name.toLowerCase(), card.getJolId());
            }
            CardEntry cardEntry = new CardEntry(card);
            cardTable.put(card.getJolId(), cardEntry);
        });
        cardArr = cardTable.values().toArray(new CardEntry[0]);
        logger.info("Read {} keys, {} cards", nameKeys.size(), cardTable.size());
    }

    public CardEntry[] getAllCards() {
        return cardArr;
    }

    public CardEntry getCardById(String id) {
        CardEntry entry = cardTable.get(id);
        logger.trace("Lookup {} - found {}", id, entry);
        return entry;
    }

    public CardEntry[] searchByType(CardEntry[] set, String type) {
        return searchByField(set, "Cardtype:", type);
    }

    public CardEntry[] searchByText(CardEntry[] set, String text) {
        text = text.toLowerCase();
        Vector<CardEntry> v = new Vector<>();
        for (CardEntry anArr : set) {
            String[] cardText = anArr.getFullText();
            for (String aCardText : cardText) {
                if (aCardText.toLowerCase().indexOf(text) > 0) {
                    v.add(anArr);
                    break;
                }
            }
        }
        set = new CardEntry[v.size()];
        v.toArray(set);
        return set;
    }

    public CardEntry[] searchByField(CardEntry[] set, String field, String value) {
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
        set = new CardEntry[v.size()];
        v.toArray(set);
        return set;
    }

    public String parseText(String text) {
        Matcher matcher = MARKUP_PATTERN.matcher(text);

        StringBuffer sb = new StringBuffer(text.length());
        while (matcher.find()) {
            for (int x = 1; x <= matcher.groupCount(); x++) {
                String match = matcher.group(x);
                try {
                    CardEntry card = findCard(match);
                    matcher.appendReplacement(sb, generateCardLink(card));
                } catch (IllegalArgumentException e) {
                    // do nothing
                }
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String generateCardLink(CardEntry card) {
        return "<a class='card-name' title='" + card.getCardId() + "'>" + card.getName() + "</a>";
    }

    public String getId(String nm) {
        return nameKeys.get(nm.toLowerCase());
    }

    public Set<String> getNames() {
        return nameKeys.keySet();
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
