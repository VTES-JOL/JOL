package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

public class ProfileBean {

    private final String email;
    private final String discordID;

    public ProfileBean(PlayerModel model) {
        String player = model.getPlayerName();
        JolAdmin jolAdmin = JolAdmin.getInstance();
        this.email = jolAdmin.getEmail(player);
        this.discordID = jolAdmin.getDiscordID(player);
    }

    public String getEmail() {
        return email;
    }

    public String getDiscordID() {
        return discordID;
    }

}
