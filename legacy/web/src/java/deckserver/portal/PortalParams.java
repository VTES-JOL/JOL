/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.portal;

import javax.servlet.http.*;
import deckserver.util.WebParams;
import java.util.*;
import nbclient.vtesmodel.JolAdminFactory;

/**
 *
 * @author  Joe User
 */
public class PortalParams extends WebParams {
    
    private static final Collection<String> pages = new HashSet<String>();
    private static final Collection<String> adminpages = new HashSet<String>();
    private static final Collection<String> sitepages = new HashSet<String>();
    
    {
        pages.addAll(Arrays.asList(new String[] {
            "main",
            "help",
            "deck",
            "user",
            "show"
        }));
        adminpages.add("admin");
        sitepages.add("site");
    }
    
    public PortalParams(HttpServletRequest request) {
        super(request);
    }
    
    public String getPage() {
        String page = getRequest().getParameter("page");
        if(page != null) {
            if(pages.contains(page)) return page;
            JolAdminFactory admin = JolAdminFactory.INSTANCE;
            if(adminpages.contains(page) && admin.isAdmin(getPlayer())) {
                return page;
            }
            if(sitepages.contains(page) && admin.isSuperUser(getPlayer())) {
                return page;
            }
        }
        if(getGame() != null) return "game";
        return "main";
    }
    
}
