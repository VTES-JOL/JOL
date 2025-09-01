/*
 * CardSearch.java
 *
 * Created on September 24, 2003, 8:51 PM
 */

package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import net.deckserver.storage.json.cards.CardSummary;
import net.deckserver.storage.json.cards.SecuredCardLoader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        InputStream loader;
        try {
            loader = getCloudfrontStream();
            logger.info("Using cloudfront stream");
        } catch (Exception e) {
            try {
                loader = getLocalStream();
                logger.info("Using local stream");
            } catch (Exception e1) {
                logger.error("Unable to read cards - using empty list");
                return;
            }
        }
        try {
            List<CardSummary> cardList = objectMapper.readValue(loader, cardSummaryCollectionType);
            init(cardList);
        } catch (IOException e) {
            logger.error("Unable to read cards", e);
        }
    }

    public void refresh(List<CardSummary> cardList) {
        logger.info("Using static list");
        INSTANCE.init(cardList);
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

    public CardSummary get(String id) {
        return cards.get(id);
    }

    private void init(List<CardSummary> cardList) {
        boolean playtestEnabled = cardList.stream().anyMatch(CardSummary::isPlayTest);
        if (playtestEnabled) {
            logger.info("Playtest cards enabled");
        }
        cardList.forEach(card -> {
            for (String name : card.getNames()) {
                nameKeys.put(name.toLowerCase(), card.getId());
            }
            cards.put(card.getId(), card);
        });
        logger.info("Read {} keys, {} cards", nameKeys.size(), cards.size());
    }

    private InputStream getCloudfrontStream() throws Exception {
        SecuredCardLoader loader = new SecuredCardLoader("/secured/cards.json");
        return loader.generateSignedUrl().openStream();
    }

    private InputStream getLocalStream() throws Exception {
        return new FileInputStream(Paths.get("target/static/secured/cards.json").toFile());
    }
}
