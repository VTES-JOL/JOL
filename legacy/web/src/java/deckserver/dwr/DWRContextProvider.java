package deckserver.dwr;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRContextProvider implements ContextProvider {
    public DWRContextProvider() {
    }

    public ServletContext getServletContext() {
        return WebContextFactory.get().getServletContext();
    }

    public HttpServletRequest getHttpServletRequest() {
        return WebContextFactory.get().getHttpServletRequest();
    }
}
