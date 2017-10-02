package net.deckserver.storage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Deck {

    private List<DeckItem> contents = new ArrayList<>();
}
