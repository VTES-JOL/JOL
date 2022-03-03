package net.deckserver.game.json.deck;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardSummary {

    private String id;
    private String amaranthId;
    private String type;
    private String text;
    private String htmlText;
    private String originalText; //Original: as in the CSV
    private String displayName;
    private Set<String> names = new HashSet<>();
    private boolean crypt;
    private boolean unique;
    private boolean burnOption;
    private String group;
    private String sect;
    private List<String> clans;

    //Library only
    private String preamble;
    private List<LibraryCardMode> modes;
    private boolean doNotReplace;
    private boolean multiMode;
    private String cost;

    //Crypt only
    private Integer capacity;
    private List<String> disciplines;
}
