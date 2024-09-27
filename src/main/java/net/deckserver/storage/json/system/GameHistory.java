package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class GameHistory {
    private String name;
    private String started;
    private String ended;
    private Map<String, PlayerResult> results = new HashMap<>();

}
