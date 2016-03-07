/*
 * AdminFactory.java
 *
 * Created on April 14, 2004, 10:05 PM
 */

package deckserver.util;

import javax.servlet.ServletContext;

import nbclient.vtesmodel.JolAdminFactory;
import webclient.state.JolAdmin;
import deckserver.rich.AdminBean;

/**
 *
 * @author  Joe User
 */
public class AdminFactory {
    
    public static synchronized JolAdminFactory get(ServletContext context) {
        if(JolAdminFactory.INSTANCE == null) try {
            System.out.println("Initing deckserver with " + context.getInitParameter("directory"));
            JolAdminFactory.INSTANCE = new JolAdmin(context.getInitParameter("directory"));
            context.setAttribute("dsadmin", new AdminBean());
            System.out.println("Initialization complete");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return JolAdminFactory.INSTANCE;
    }
    
    public static synchronized AdminBean getBean(ServletContext context) {
    	return (AdminBean) context.getAttribute("dsadmin");
    }
    
    public static synchronized void init(ServletContext context) {
        get(context);
    }
    
    public static synchronized JolAdminFactory getFactory() {
        return JolAdminFactory.INSTANCE;
    }
    
    public static synchronized AdminBean getAdmin() {
        return AdminBean.INSTANCE;
    }
}
