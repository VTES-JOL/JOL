package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.DeckPageBean;
import net.deckserver.dwr.model.PlayerModel;

class DeckCreator implements ViewCreator {

    public String getFunction() {
        return "callbackShowDecks";
    }

    public Object createData(PlayerModel model) {
        return new DeckPageBean(model);
    }

}
