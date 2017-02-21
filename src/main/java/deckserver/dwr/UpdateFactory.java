package deckserver.dwr;

import deckserver.dwr.bean.AdminBean;
import org.directwebremoting.WebContextFactory;

import java.util.HashMap;
import java.util.Map;

class UpdateFactory {

    private static final Map<String, ViewCreator> viewMap = new HashMap<>();

    static {
        viewMap.put("nav", new NavCreator());
        viewMap.put("game", new GameCreator());
        viewMap.put("main", new MainCreator());
        viewMap.put("deck", new DeckCreator());
        viewMap.put("admin", new AdminCreator());
        viewMap.put("status", new StatusCreator());
    }

    private static ViewCreator getView(String type) {
        return viewMap.get(type);
    }

    static Map<String, Object> getUpdate() {
        AdminBean abean = AdminBean.INSTANCE;
        PlayerModel model = Utils.getPlayerModel(WebContextFactory.get().getHttpServletRequest(), abean);
        model.recordAccess();
        String[] views = new String[]{model.getView(), "nav", "status"};
        Map<String, Object> ret = new HashMap<>();
        for (String view1 : views) {
            ViewCreator view = getView(view1);
            if (view != null)
                ret.put(view.getFunction(), view.createData(abean, model));
        }
        return ret;
    }

}
