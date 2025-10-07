package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

@Getter
public class ProfileBean {

    private final String email;
    private final String discordID;
    private final String veknID;
    private final boolean imageTooltipPreference;

    public ProfileBean(PlayerModel model) {
        String player = model.getPlayerName();
        this.email = JolAdmin.getEmail(player);
        this.discordID = JolAdmin.getDiscordID(player);
        this.veknID = JolAdmin.getVeknID(player);
        this.imageTooltipPreference = JolAdmin.getImageTooltipPreference(player);
    }

}
