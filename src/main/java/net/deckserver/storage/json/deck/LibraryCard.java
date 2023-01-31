package net.deckserver.storage.json.deck;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LibraryCard {
    private String type;
    private int count;
    private List<CardCount> cards = new ArrayList<>();
}
