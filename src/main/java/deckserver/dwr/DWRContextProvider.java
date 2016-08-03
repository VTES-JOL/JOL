package deckserver.dwr;

import org.directwebremoting.WebContextFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

class DWRContextProvider implements ContextProvider {

    public ServletContext getServletContext() {
        return WebContextFactory.get().getServletContext();
    }

    public HttpServletRequest getHttpServletRequest() {
        return WebContextFactory.get().getHttpServletRequest();
    }
}
