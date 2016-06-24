package deckserver.dwr;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public interface ContextProvider {

    ServletContext getServletContext();

    HttpServletRequest getHttpServletRequest();
}
