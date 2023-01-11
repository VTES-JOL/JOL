package net.deckserver.dwr.creators;

import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.JolAdmin;
import net.deckserver.dwr.model.PlayerModel;

class GameCreator implements ViewCreator {

    public String getFunction() {
        return "loadGame";
    }

    public Object createData(PlayerModel player) {
        String name = player.getCurrentGame();
        GameModel game = JolAdmin.getInstance().getGameModel(name);
        GameView gview = game.getView(player.getPlayer());
        player.changeGame(name);
        return gview.create();
    }

}
