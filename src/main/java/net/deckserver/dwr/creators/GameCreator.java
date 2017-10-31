package net.deckserver.dwr.creators;

import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.bean.GameBean;
import net.deckserver.dwr.model.GameModel;
import net.deckserver.dwr.model.GameView;
import net.deckserver.dwr.model.PlayerModel;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

class GameCreator implements ViewCreator {

    private static final Logger logger = getLogger(GameCreator.class);

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
