package net.deckserver.game;

import lombok.Data;

import java.util.Set;

@Data
public class SummaryCard {

    private String id;
    private String key;
    private String type;
    private String text;
    private String displayName;
    private Set<String> names;
}