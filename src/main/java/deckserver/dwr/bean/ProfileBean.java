package deckserver.dwr.bean;

import deckserver.client.JolAdmin;
import deckserver.dwr.PlayerModel;

public class ProfileBean {

    private String email;
    private boolean receivePing;
    private boolean receiveSummary;

    public ProfileBean(AdminBean abean, PlayerModel model) {
        String player = model.getPlayer();
        this.email = JolAdmin.getInstance().getEmail(player);
        this.receivePing = JolAdmin.getInstance().receivesPing(player);
        this.receiveSummary = JolAdmin.getInstance().receivesTurnSummaries(player);
    }

    public String getEmail() {
        return email;
    }

    public boolean isReceivePing() {
        return receivePing;
    }

    public boolean isReceiveSummary() {
        return receiveSummary;
    }
}
