package net.deckserver.dwr.bean;

import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

public class ProfileBean {

    private String email;
    private String discordID;
    private boolean pingDiscord;

    public ProfileBean(AdminBean abean, PlayerModel model) {
        String player = model.getPlayer();
        JolAdmin jolAdmin = JolAdmin.getInstance();
        this.email = jolAdmin.getEmail(player);
        this.discordID = jolAdmin.getDiscordID(player);
        this.pingDiscord = jolAdmin.receivesDiscordPing(player);
    }

    public String getEmail() {
        return email;
    }

    public String getDiscordID() {
        return discordID;
    }

    //This name is not natural, but makes for a natural sound in JavaScript
    public boolean isPingDiscord() {
        return pingDiscord;
    }
}
