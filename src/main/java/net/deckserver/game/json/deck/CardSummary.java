package net.deckserver.game.json.deck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardSummary {

    private String id;
    private String jolId;
    private String amaranthId;
    private String type;
    private String text;
    private String htmlText;
    private String displayName;
    private Set<String> names = new HashSet<>();
    private boolean crypt;
    private boolean unique;
    private String group;
}
