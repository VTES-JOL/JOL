package deckserver.dwr;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class ContextDebug {

	public static void debug(HttpServletRequest request) {
		try {
			ServletContext ctx = request.getSession().getServletContext();
			System.err.println(request.getContextPath());
			System.err.println(request.getPathInfo());
			System.err.println(ctx.getServletContextName());
			URL resource = ctx.getResource("/WEB-INF/jsps/state/card.jsp");
			ByteArrayInputStream in = (ByteArrayInputStream) resource.getContent();
			byte[] bytes = new byte[1000];
			System.err.println(in.read(bytes));
			System.err.println(new String(bytes));
			System.err.println(resource.getContent());
			System.err.println(ctx.getResource("/WEB-INF/jsps/state/card.jsp"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
