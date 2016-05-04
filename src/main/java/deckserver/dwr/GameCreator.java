package deckserver.dwr;

import deckserver.rich.AdminBean;
import deckserver.rich.GameModel;
import deckserver.rich.GameView;
import deckserver.rich.PlayerModel;

public class GameCreator implements ViewCreator {

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
