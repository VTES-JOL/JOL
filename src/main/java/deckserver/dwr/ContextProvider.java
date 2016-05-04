package deckserver.dwr;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public interface ContextProvider {
    
    public ServletContext getServletContext();
    
    public HttpServletRequest getHttpServletRequest();
}
