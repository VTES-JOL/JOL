package net.deckserver.storage.json.system;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TournamentSpecialRules {
    private String condition;
    private List<String> rules = new ArrayList<>();
}
