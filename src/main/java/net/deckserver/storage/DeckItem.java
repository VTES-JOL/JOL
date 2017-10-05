package net.deckserver.storage;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class DeckItem {
    private String key;
    private String name;
    private Integer count;
    private String type;
    private String comment;

    public static DeckItem of(String key, String name, Integer count, String type) {
        DeckItem item = new DeckItem();
        item.key = key;
        item.name = name;
        item.count = count;
        item.type = type;
        return item;
    }

    public static DeckItem of(String comment) {
        DeckItem item = new DeckItem();
        item.comment = comment;
        return item;
    }
}
