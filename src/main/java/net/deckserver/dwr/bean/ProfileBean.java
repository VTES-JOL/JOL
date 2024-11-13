package net.deckserver.dwr.bean;

import lombok.Getter;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

@Getter
public class ProfileBean {

    private final String email;
    private final String discordID;
    private final String veknID;
    private final boolean imageTooltipPreference;

    public ProfileBean(PlayerModel model) {
        String player = model.getPlayerName();
        JolAdmin jolAdmin = JolAdmin.INSTANCE;
        this.email = jolAdmin.getEmail(player);
        this.discordID = jolAdmin.getDiscordID(player);
        this.veknID = jolAdmin.getVeknID(player);
        this.imageTooltipPreference = jolAdmin.getImageTooltipPrefence(player);
    }

}
