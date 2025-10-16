package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.deckserver.game.enums.PlayerRole;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class PlayerInfo {
    private String name;
    private String id;
    private String email;
    private String hash;
    private String discordId;
    private String veknId;
    private String countryCode;
    private Set<PlayerRole> roles = new HashSet<>();
    private boolean showImages = true;

    public PlayerInfo(String name, String id, String email, String hash) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.hash = hash;
    }
}
