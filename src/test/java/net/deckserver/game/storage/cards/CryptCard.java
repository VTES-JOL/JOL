package net.deckserver.game.storage.cards;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class CryptCard extends BaseCard {
    private String id;
    private String amaranthId;
    private String name;
    private String type;
    private String text;
    private String displayName;
    private Set<String> names;
    private Set<String> partials;
    private Set<String> sets;

    private boolean unique = true;
    private boolean infernal = false;
    private Integer capacity;
    private String clan;
    private String group;
    private String sect;
    private String path;
    private boolean advanced;
    private String title;
    private String votes;
    private List<String> disciplines;
    private Map<String, Integer> discipline;

    private boolean banned;
}
