package net.deckserver.servlet;

import net.deckserver.Utils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/main.jsp")
public class ParamFilter implements Filter {

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
