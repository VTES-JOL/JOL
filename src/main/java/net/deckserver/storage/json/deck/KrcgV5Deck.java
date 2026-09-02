package net.deckserver.storage.json.deck;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * The KRCG v5 deck document (see <a href="https://v4.api.krcg.org/docs">KRCG
 * API v4</a>) — the canonical on-disk / interchange shape for a deck.
 *
 * <p>Unlike the historical {@link Deck} JSON this is a <em>flat</em> card list
 * ({@code cards[]} with a {@code kind} discriminator) rather than a
 * crypt/library-by-type tree. {@link KrcgV5Mapper} converts between the two;
 * {@link Deck} stays the in-memory model every service, validator and REST bean
 * works with.
 *
 * <p>TWDA-only provenance fields ({@code event}, {@code score}, {@code raven})
 * are intentionally not modelled — {@code @JsonIgnoreProperties} lets us read a
 * TWDA export without them, and our own decks never carry them.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KrcgV5Deck {

    private String id;
    private String name;
    private List<KrcgV5Card> cards = new ArrayList<>();
    private String comment;
    private String author;
    private String player;
}
