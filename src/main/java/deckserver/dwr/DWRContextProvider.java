package deckserver.dwr;

import uk.ltd.getahead.dwr.WebContextFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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
