package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.LobbyPageBean;
import net.deckserver.dwr.model.PlayerModel;

class LobbyCreator implements ViewCreator {

    public String getFunction() {
        return "callbackLobby";
    }

    public Object createData(PlayerModel model) {
        return new LobbyPageBean(model.getPlayerName());
    }

}
