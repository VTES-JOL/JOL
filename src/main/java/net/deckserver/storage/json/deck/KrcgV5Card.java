package net.deckserver.storage.json.deck;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * One entry of a {@link KrcgV5Deck}'s flat {@code cards} array — the KRCG v5
 * card shape (see <a href="https://v4.api.krcg.org/docs">KRCG API v4</a>).
 *
 * <p>Only a storage/interchange DTO: {@link KrcgV5Mapper} maps it to and from
 * the canonical {@link CardCount} the rest of the app uses. {@code printed_name}
 * / {@code types} / {@code suffix} / {@code unicity_suffix} are all derived from
 * {@link net.deckserver.game.cards.CardRegistry} on write and ignored on read
 * (the numeric {@code id} is the source of truth).
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KrcgV5Card {

    private Integer id;

    @JsonProperty("printed_name")
    private String printedName;

    /** Name disambiguator — {@code "ADV"} for an advanced crypt card, else {@code ""}. */
    @JsonProperty("unicity_suffix")
    private String unicitySuffix = "";

    /** Decklist display marker — {@code "G3"} / {@code "G3 ADV"} for crypt, {@code ""} for library. */
    private String suffix = "";

    private int count;

    /** {@code "Crypt"} or {@code "Library"}. */
    private String kind;

    private List<String> types = new ArrayList<>();

    /** Per-card note; maps to {@link CardCount#getComments()}. */
    private String comment;
}
