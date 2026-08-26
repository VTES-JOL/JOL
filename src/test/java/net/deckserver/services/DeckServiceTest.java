package net.deckserver.services;

import net.deckserver.game.enums.DeckFormat;
import net.deckserver.storage.json.deck.Deck;
import net.deckserver.storage.json.deck.ExtendedDeck;
import net.deckserver.storage.json.system.DeckInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class DeckServiceTest {

    @Test
    void readsFixtureDeckFromJpa() {
        DeckInfo info = DeckService.get("Player1", "Weenie Animalism");
        assertThat(info, notNullValue());
        assertThat(info.getDeckId(), equalTo("01GR3EV0YN7R2GP2E0NQBTK8JB"));
    }

    @Test
    void listsPlayerDeckNames() {
        Set<String> names = DeckService.getPlayerDeckNames("Player1");
        assertThat(names, hasItem("Weenie Animalism"));
    }

    @Test
    void addsSavesAndRemovesADeck() {
        String playerName = "Player1";
        String deckName = "DeckServiceTestDeck";

        Deck deck = new Deck();
        deck.setName(deckName);
        deck.setAuthor(playerName);
        ExtendedDeck extendedDeck = new ExtendedDeck(deck, null, new java.util.ArrayList<>());

        DeckInfo info = new DeckInfo("deck-service-test-id", deckName, DeckFormat.MODERN, Set.of());
        DeckService.addDeck(playerName, deckName, info);
        DeckService.saveDeck(info.getDeckId(), extendedDeck);

        assertThat(DeckService.get(playerName, deckName).getDeckId(), equalTo(info.getDeckId()));
        assertThat(DeckService.getDeck(info.getDeckId()).getDeck().getName(), equalTo(deckName));

        DeckService.remove(playerName, deckName);
        assertThat(DeckService.get(playerName, deckName), nullValue());
    }

    @Test
    void serializesAndDeserializesDeckContentForGameRegistration() {
        Deck deck = new Deck();
        deck.setName("RoundTrip");
        ExtendedDeck extendedDeck = new ExtendedDeck(deck, null, new java.util.ArrayList<>());

        String json = DeckService.serializeDeck(extendedDeck);
        assertThat(json, notNullValue());

        ExtendedDeck roundTripped = DeckService.deserializeDeck(json);
        assertThat(roundTripped.getDeck().getName(), equalTo("RoundTrip"));

        // null content (e.g. legacy pre-migration registration) degrades gracefully
        assertThat(DeckService.deserializeDeck(null), notNullValue());
    }
}
