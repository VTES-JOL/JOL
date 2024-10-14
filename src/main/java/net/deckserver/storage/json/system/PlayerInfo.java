package net.deckserver.storage.json.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerInfo {
    private String name;
    private String id;
    private String email;
    private String hash;
    private String discordId;
    private String veknId;
    private Set<PlayerRole> roles;

    public PlayerInfo(String name, String id, String email, String hash) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.hash = hash;
    }
}
