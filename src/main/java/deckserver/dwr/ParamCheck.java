package deckserver.dwr;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ParamCheck implements Filter {

    private ServletContext ctx = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        ctx = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Utils.checkParams((HttpServletRequest) request, ctx);
        chain.doFilter(request, response);
    }

    public void destroy() {
    }
}
