package net.deckserver.dwr.creators;

import net.deckserver.Utils;
import net.deckserver.dwr.bean.AdminBean;
import net.deckserver.dwr.model.PlayerModel;
import org.directwebremoting.WebContextFactory;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateFactory {

    private static final Map<String, ViewCreator> viewMap = new HashMap<>();

    static {
        viewMap.put("nav", new NavCreator());
        viewMap.put("game", new GameCreator());
        viewMap.put("main", new MainCreator());
        viewMap.put("deck", new DeckCreator());
        viewMap.put("admin", new AdminCreator());
        viewMap.put("profile", new ProfileCreator());
        viewMap.put("active", new AllGamesCreator());
        viewMap.put("super", new SuperCreator());
    }

    private static ViewCreator getView(String type) {
        return viewMap.get(type);
    }

    public static Map<String, Object> getUpdate() {
        AdminBean abean = AdminBean.INSTANCE;
        PlayerModel player = Utils.getPlayerModel(WebContextFactory.get().getHttpServletRequest(), abean);
        abean.recordAccess(player);
        List<String> views = Arrays.asList(player.getView(), "nav", "status");
        return views.stream()
                .map(UpdateFactory::getView)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ViewCreator::getFunction, v -> v.createData(abean, player)));
    }

}
