package net.deckserver.game.storage.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class CryptCard {
    private String id;
    private String jolId;
    private String amaranthId;
    private String name;
    private String type;
    private String text;
    private String displayName;
    private Set<String> names;
    private Set<String> partials;

    private boolean unique = true;
    private Integer capacity;
    private String clan;
    private String group;
    private String sect;
    private Boolean advanced;
    private String title;
    private List<String> disciplines;
    private Map<String, Integer> discipline;

    private Boolean banned;
}
