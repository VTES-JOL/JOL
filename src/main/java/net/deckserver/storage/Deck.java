package net.deckserver.storage;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Deck {

    private int cryptCount;
    private int libraryCount;
    private Map<String, Integer> contents = new HashMap<>();
    private List<String> errors = new ArrayList<>();
    private List<String> comments = new ArrayList<>();
}
