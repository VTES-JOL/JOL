package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SuperCreator implements ViewCreator {

    public String getFunction() {
        return "loadsuper";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        if (!model.isSuper()) return null;
        List<GameModel> games = abean.getActiveGames();
        Collection<String> res = new ArrayList<String>();
        for (GameModel g : games) {
            if (g.isActive()) res.add(g.getName());
        }
        return res.toArray();
    }
}
