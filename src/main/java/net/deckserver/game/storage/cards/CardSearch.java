/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author administrator
 */
public class CardSearch {

    private static final Logger logger = getLogger(CardSearch.class);

    private Map<String, String> nameKeys;
    private Map<String, CardEntry> cardTable = new HashMap<>();
    private CardEntry[] cardArr;

    public CardSearch(List<String> keys, List<String> text) {
        this.nameKeys = keys.stream().map(s -> s.split("=")).collect(Collectors.toMap(s -> s[1].toLowerCase(), s -> s[0]));
        List<String> currentCardText = new ArrayList<>();
        for (String textLine : text) {
            if (textLine.trim().isEmpty()) {
                CardEntry cardEntry = new CardEntry(nameKeys, currentCardText);
                cardTable.put(cardEntry.getCardId(), cardEntry);
                currentCardText = new ArrayList<>();
            } else {
                currentCardText.add(textLine);
            }
        }
        cardArr = cardTable.values().toArray(new CardEntry[0]);
        logger.info("Read {} keys, {} cards", nameKeys.size(), cardTable.size());
    }

    public CardEntry[] getAllCards() {
        return cardArr;
    }

    public CardEntry getCardById(String id) {
        return cardTable.get(id);
    }

    public CardEntry[] searchByName(CardEntry[] set, String name) {
        return searchByField(set, "Name:", name);
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

    public String getId(String nm) {
        return nameKeys.get(nm.toLowerCase());
    }

    public Set<String> getNames() {
        return nameKeys.keySet();
    }

    public CardEntry findCard(String text) throws IllegalArgumentException {
        final String lowerText = text.toLowerCase();
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

    public void export(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Map<String, CardAlias> exportMap = new HashMap<>();
        cardTable.forEach((k, v) -> {
            CardAlias alias = new CardAlias();
            alias.setKey(k);
            alias.setText(Arrays.asList(v.getFullText()));
            exportMap.put(k, alias);
        });

        nameKeys.forEach((k, v) -> {
            exportMap.get(v).getNames().add(k);
        });

        objectMapper.writeValue(file, exportMap);
        System.out.println(exportMap.size());
    }
}
