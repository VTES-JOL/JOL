package deckserver.dwr;

import java.util.*;

import javax.servlet.ServletContext;

import deckserver.rich.*;
import deckserver.util.AdminFactory;

public class UpdateFactory {
	
	private static final Map<String,ViewCreator> viewMap = new HashMap<String,ViewCreator>();
	
	static {
		viewMap.put("nav", new NavCreator());
		viewMap.put("game", new GameCreator());
		viewMap.put("main", new MainCreator());
		viewMap.put("deck", new DeckCreator());
		viewMap.put("admin", new AdminCreator());
		viewMap.put("bugs", new BugsCreator());
		viewMap.put("suser", new SuperCreator());
	}
	
	public static ViewCreator getView(String type) {
		return (ViewCreator) viewMap.get(type);
	}

	public static Map<String,Object> getUpdate(ContextProvider provider) {
		ServletContext ctx = provider.getServletContext();
		AdminBean abean = AdminFactory.getBean(ctx);
		PlayerModel model = Utils.getPlayerModel(provider.getHttpServletRequest(),abean);
		model.recordAccess();
		String[] views = new String[] {model.getView(),"nav"};
		Map<String,Object> ret = new HashMap<String,Object>();
		for(int i = 0; i < views.length; i++) {
			ViewCreator view = getView(views[i]);
			if(view != null)
				ret.put(view.getFunction(),view.createData(abean,model));
		}
		return ret;
	}
			
}
