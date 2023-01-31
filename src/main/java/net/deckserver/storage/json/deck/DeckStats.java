package net.deckserver.storage.json.deck;

import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
public class DeckStats {

    private int cryptSize;
    private int librarySize;
    private Set<String> groups;
    private boolean valid = false;
    private boolean bannedCards;
    private String summary;

    public DeckStats(int cryptSize, int librarySize, Set<String> groups, boolean valid, boolean hasBannedCards) {
        this.cryptSize = cryptSize;
        this.librarySize = librarySize;
        this.groups = groups;
        this.valid = valid;
        this.bannedCards = hasBannedCards;
        this.summary = String.format("Crypt: %d Library: %d Groups: %s", cryptSize, librarySize, String.join("/", groups));
    }

    public int getCryptSize() {
        return cryptSize;
    }

    public int getLibrarySize() {
        return librarySize;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isBannedCards() {
        return bannedCards;
    }

    public String getSummary() {
        return summary;
    }
}
