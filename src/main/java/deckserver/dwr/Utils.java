package deckserver.dwr;

import deckserver.rich.AdminBean;
import deckserver.rich.PlayerModel;
import deckserver.util.AdminFactory;
import deckserver.util.Logger;
import nbclient.vtesmodel.JolAdminFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;

public class Utils {
	
	private static Logger log = Logger.getLogger(Utils.class);
	static final DateFormat format = new SimpleDateFormat("HH:mm M/d ");
	
	{
		Logger.activateLog("Utils");
	}

	public static String getGameName(HttpServletRequest request) {
		if (true) { // workaround until beta period is over.
			return (String) request.getAttribute("gamename");
		}
		String gamename = request.getServletPath();
		if (gamename.length() > 0)
			gamename = gamename.substring(1);
		if (gamename.equals("")
				|| !JolAdminFactory.INSTANCE.existsGame(gamename))
			gamename = null;
		return gamename;
	}

	public static String getPlayer(HttpServletRequest request) {
             return (String) request.getSession().getAttribute("meth");
	}
        
        public static void setPlayer(HttpServletRequest request, String player) {
            request.getSession().setAttribute("meth", player);
        }
	
	public static PlayerModel getPlayerModel(HttpServletRequest request, AdminBean abean) {
		String player = getPlayer(request);
		PlayerModel model;
		if(player == null) {
			model = (PlayerModel) request.getSession().getAttribute("guest");
			if(model == null) {
				model = abean.getPlayerModel(null);
				request.getSession().setAttribute("guest",model);
			}
		} else {
			model = abean.getPlayerModel(player);
		}
		return model;
	}

	public static void checkParams(HttpServletRequest request,ServletContext ctx) {
		AdminBean abean = AdminFactory.getBean(ctx);
		String player = (String) request.getSession().getAttribute("meth");
		String login = request.getParameter("login");
		log.log("Get request " + request.getRequestURI() + " with " + player + " and " + login);
		if (login != null) {
			if (login.equals("Log in")) {
				player = request.getParameter("dsuserin");
				String password = request.getParameter("dspassin");
				if (player != null
						&& JolAdminFactory.INSTANCE.authenticate(player,
								password)) {
                                        setPlayer(request,player);
					System.err.println("Logged in with " + player + " " + password);
				} else {
					System.err.println("Log in failed: " + player);
					player = null;
				}
			} else if (login.equals("Log out")) {
				System.err.println("Log out: " + player);
				request.getSession().removeAttribute("meth");
				abean.remove(player);
				player = null;
			}
		} else if("Register".equals(request.getParameter("register"))) {
			player = request.getParameter("newplayer");
			String email = request.getParameter("newemail");
			String password = request.getParameter("newpassword");
			if(JolAdminFactory.INSTANCE.registerPlayer(player, password, email)) {
                                setPlayer(request,player);
				System.err.println("registered " + player);
			} else {
				System.err.println("registration failed for " + player);
				player = null;
			}
		}
		PlayerModel model = getPlayerModel(request, abean);
		if(player == null) {
			request.getSession().setAttribute("guest", model);
		}
		String gamename = request.getPathInfo();
		if (gamename != null && gamename.length() > 0)
			gamename = gamename.substring(1);
		if (gamename != null && gamename.length() > 0
				&& JolAdminFactory.INSTANCE.existsGame(gamename)) {
			System.err.println("Setting game to be " + gamename);
			
			model.enterGame(abean,gamename);
		}
	}

	static public final String getDate() {
	    return Utils.format.format(new Date());
	}

	public static String sanitizeName(String name) {
		StringCharacterIterator it = new StringCharacterIterator(name);
		StringBuffer res = new StringBuffer();
		for(char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
			if(Character.isJavaIdentifierPart(c))
				res.append(c);
			else if (Character.isWhitespace(c)) 
				res.append(' ');
		}
		return res.toString();
	}

}
