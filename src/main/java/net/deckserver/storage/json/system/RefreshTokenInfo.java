package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshTokenInfo {
    private String id;
    private String playerName;
    private String secretHash;
    private String deviceLabel;
    private long createdAt;
    private long lastUsedAt;
    private long expiresAt;
    private boolean remember;
}
