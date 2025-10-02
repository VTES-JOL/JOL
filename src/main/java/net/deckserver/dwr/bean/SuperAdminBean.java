package net.deckserver.dwr.bean;

import net.deckserver.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

public class SuperAdminBean {

    public SuperAdminBean(PlayerModel model) {
        JolAdmin admin = JolAdmin.INSTANCE;
    }
}
