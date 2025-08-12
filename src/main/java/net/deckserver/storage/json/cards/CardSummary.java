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
    private String displayName;
    private String name;
    private Set<String> names = new HashSet<>();
    private boolean crypt;
    private boolean unique;
    private String group;
    private String sect;
    private String path;
    private List<String> clans = new ArrayList<>();
    private boolean banned;
    private Set<String> sets = new HashSet<>();
    private boolean playTest;

    //Crypt only
    private Integer capacity;
    private List<String> disciplines = new ArrayList<>();
    private String title;
    private String votes;
    private boolean advanced;
    private boolean infernal;

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

    public String getSingleClanClass() {
        return clans.stream().findFirst()
                .map(s -> s.replaceAll(" ", "_"))
                .map(String::toLowerCase)
                .orElse(null);
    }

    @JsonIgnore
    public CardType getCardType() {
        return CardType.of(type);
    }
}
