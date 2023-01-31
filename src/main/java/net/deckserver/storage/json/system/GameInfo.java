package net.deckserver.storage.json.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameInfo {
    private String name;
    private String id;
    private String owner;
    private Visibility visibility;
    private GameStatus status;
}
