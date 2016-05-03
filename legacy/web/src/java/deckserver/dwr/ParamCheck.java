package deckserver.dwr;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

public class ParamCheck implements Filter {
	
	private ServletContext ctx = null;

	public void init(FilterConfig arg0) throws ServletException {
		ctx = arg0.getServletContext();
		// no initialization necessary
	}

	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException {
		Utils.checkParams((HttpServletRequest) arg0,ctx);
		
		arg2.doFilter(arg0,arg1);
	}

	public void destroy() {
		// no destroy necessary
	}
}
