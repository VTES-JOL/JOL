package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;
import net.deckserver.services.PlayerService;
import net.deckserver.storage.json.system.PlayerInfo;

@Getter
public class ProfileBean {

    private final String email;
    private final String discordID;
    private final String veknID;
    private final String country;
    private final String edgeColor;
    private final boolean imageTooltipPreference;

    public ProfileBean(PlayerModel model) {
        String player = model.getPlayerName();
        PlayerInfo playerInfo = PlayerService.get(player);
        this.email = playerInfo.getEmail();
        this.discordID = playerInfo.getDiscordId();
        this.veknID = playerInfo.getVeknId();
        this.imageTooltipPreference = JolAdmin.getImageTooltipPreference(player);
        this.edgeColor = JolAdmin.getEdgeColor(player);
        this.country = playerInfo.getCountryCode();
    }

}
