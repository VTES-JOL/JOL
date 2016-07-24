package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SuperCreator implements ViewCreator {

    public String getFunction() {
        return "loadsuper";
    }

    public Object createData(AdminBean abean, PlayerModel model) {
        if (!model.isSuper()) return null;
        List<GameModel> games = abean.getActiveGames();
        Collection<String> res = games.stream().filter(GameModel::isActive).map(GameModel::getName).collect(Collectors.toCollection(ArrayList::new));
        return res.toArray();
    }
}
