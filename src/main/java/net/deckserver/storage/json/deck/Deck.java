package net.deckserver.storage.json.deck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Deck {

    private String id;
    private String name;
    private Crypt crypt = new Crypt();
    private Library library = new Library();
    private String comments;
    private String player;
}
