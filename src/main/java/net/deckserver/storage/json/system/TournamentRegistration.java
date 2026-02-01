package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "player")
public class TournamentRegistration {
    private String player;
    private String vekn;
    private String deck;

    public TournamentRegistration(String name, String vekn) {
        this.player = name;
        this.vekn = vekn;
    }
}
