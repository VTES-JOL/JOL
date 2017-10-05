package net.deckserver.storage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Deck {

    private int cryptCount;
    private int libraryCount;
    private List<DeckItem> contents = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
}
