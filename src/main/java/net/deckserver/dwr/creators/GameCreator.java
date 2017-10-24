package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.PlayerModel;

class GameCreator implements ViewCreator {

    public String getFunction() {
        return "loadGame";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        String name = model.getCurrentGame();
        GameModel game = abean.getGameModel(name);
        GameView gview = game.getView(model.getPlayer());
        model.changeGame(name);
        return gview.create();
    }

}
