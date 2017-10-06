package net.deckserver.storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@EqualsAndHashCode(exclude = {"name", "comment"})
public class DeckItem {
    private String key;
    private String name;
    private Integer count;
    private boolean crypt;
    private String comment;

    public static DeckItem of(String key, String name, Integer count, boolean crypt) {
        DeckItem item = new DeckItem();
        item.key = key;
        item.name = name;
        item.count = count;
        item.crypt = crypt;
        return item;
    }

    public static DeckItem of(String comment) {
        DeckItem item = new DeckItem();
        item.comment = comment;
        return item;
    }
}
