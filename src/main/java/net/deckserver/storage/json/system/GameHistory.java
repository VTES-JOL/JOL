package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GameHistory {
    private String name;
    private String started;
    private String ended;
    private List<PlayerResult> results = new ArrayList<>();

}
