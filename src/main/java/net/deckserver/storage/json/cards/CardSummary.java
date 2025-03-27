package net.deckserver.storage.json.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.deckserver.game.storage.cards.CardType;

import java.util.*;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardSummary {

    private String id;
    private String type;
    private String htmlText;
    private String originalText;
    private String displayName;
    private String name;
    private Set<String> names = new HashSet<>();
    private boolean crypt;
    private boolean unique;
    private boolean burnOption;
    private String group;
    private String sect;
    private List<String> clans = new ArrayList<>();
    private boolean banned;

    //Library only
    private String preamble;
    private List<LibraryCardMode> modes;
    private boolean doNotReplace;
    private boolean multiMode;
    private String cost;

    //Crypt only
    private Integer capacity;
    private List<String> disciplines = new ArrayList<>();
    private String title;
    private String votes;
    private boolean advanced;

    @JsonIgnore
    public boolean hasLife() {
        return CardType.lifeTypes().contains(getCardType());
    }

    @JsonIgnore
    public boolean hasBlood() {
        return CardType.VAMPIRE.equals(getCardType());
    }

    public boolean isMinion() {
        return hasBlood() || hasLife();
    }

    @JsonIgnore
    public String getTypeClass() {
        return Arrays.stream(type.toLowerCase()
                        .trim().split("/"))
                .map(s -> s.replaceAll(" ", "_"))
                .sorted()
                .collect(Collectors.joining(" "));
    }

    @JsonIgnore
    public List<String> getClanClass() {
        return clans.stream()
                .map(s -> s.replaceAll(" ", "_"))
                .map(String::toLowerCase)
                .sorted()
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public CardType getCardType() {
        return CardType.of(type);
    }
}
