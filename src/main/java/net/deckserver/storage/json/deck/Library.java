package net.deckserver.storage.json.deck;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Library {
    private int count;
    private List<LibraryCard> cards = new ArrayList<>();
}
