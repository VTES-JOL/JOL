package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import nbclient.vtesmodel.JolAdminFactory;


public class _jol3 extends com.orionserver.http.OrionHttpJspPage {


  // ** Begin Declarations


  // ** End Declarations

  public void _jspService(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {

    response.setContentType( "text/html;charset=UTF-8");
    /* set up the intrinsic variables using the pageContext goober:
    ** session = HttpSession
    ** application = ServletContext
    ** out = JspWriter
    ** page = this
    ** config = ServletConfig
    ** all session/app beans declared in globals.jsa
    */
    PageContext pageContext = JspFactory.getDefaultFactory().getPageContext( this, request, response, null, true, JspWriter.DEFAULT_BUFFER, true);
    // Note: this is not emitted if the session directive == false
    HttpSession session = pageContext.getSession();
    int __jsp_tag_starteval;
    ServletContext application = pageContext.getServletContext();
    JspWriter out = pageContext.getOut();
    _jol3 page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      {
        String __url=OracleJspRuntime.toStr("/WEB-INF/jsps/topframe/topbar.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[3]);
      {
        String __url=OracleJspRuntime.toStr("/WEB-INF/jsps/topframe/main.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[4]);
      {
        String __url=OracleJspRuntime.toStr("/WEB-INF/jsps/dwr/game.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[5]);
      {
        String __url=OracleJspRuntime.toStr("/WEB-INF/jsps/topframe/deck.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[6]);
      {
        String __url=OracleJspRuntime.toStr("/WEB-INF/jsps/topframe/bugs.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[7]);
      {
        String __url=OracleJspRuntime.toStr("/WEB-INF/jsps/topframe/admin.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[8]);
       String player = (String) request.getSession(true).getAttribute("meth");
         if(JolAdminFactory.INSTANCE.isSuperUser(player)) {
         
      out.write(__oracle_jsp_text[9]);
      {
        String __url=OracleJspRuntime.toStr("/WEB-INF/jsps/topframe/super.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[10]);
       } 
      out.write(__oracle_jsp_text[11]);

    }
    catch (java.lang.Throwable e) {
      if (!(e instanceof javax.servlet.jsp.SkipPageException)){
        try {
          if (out != null) out.clear();
        }
        catch (java.lang.Exception clearException) {
        }
        pageContext.handlePageException(e);
      }
    }
    finally {
      OracleJspRuntime.extraHandlePCFinally(pageContext, true);
      JspFactory.getDefaultFactory().releasePageContext(pageContext);
    }

  }
  
  private static final char __oracle_jsp_text[][]=new char[12][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n".toCharArray();
    __oracle_jsp_text[1] = 
    "\n".toCharArray();
    __oracle_jsp_text[2] = 
    "\n<html>\n <head>\n  <title>JOL3</title>\n  <meta name=\"decription\" content=\"JOL3 Version 1.1\"></meta>\n  <meta name=\"keywords\" content=\"JOL, VTES\"></meta>\n  <meta name=\"robots\" content=\"noindex, nofollow\"></meta>\n  <meta name=\"rating\" content=\"general\"></meta>\n  <meta name=\"generator\" content=\"vi\"></meta>\n  <script type='text/javascript' src='/jol3/dwr/interface/DS.js'></script>\n  <script type='text/javascript' src='/jol3/dwr/engine.js'></script>\n  <script type='text/javascript' src='/jol3/dwr/util.js'></script>\n  <script type='text/javascript' src='/jol3/ds.js'></script>\n  <link rel=\"stylesheet\" type=\"text/css\" href=\"/jol3/styles.css\"/>\n </head>\n <body onload=\"init();\"><div id=\"dsdebug\"></div><div id=\"loadmsg\">Loading...</div><div id=\"loaded\"\n                                                                                       style=\"display :none;\">\n <div id=\"disabledZone\" style=\"position: absolute; z-index: 1000; left: 0px; top: 0px; width: 100%; height: 100%; visibility: hidden;\">\n<div id=\"messageZone\" style=\"padding: 4px; background: red none repeat scroll 0%; position: absolute; top: 0px; right: 0px; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; font-family: Arial,Helvetica,sans-serif;\">Loading</div>\n</div>                                                                                      \n   ".toCharArray();
    __oracle_jsp_text[3] = 
    "\n    \n   <div id=\"content\">\n    <input type=\"hidden\" name=\"contentselect\" id=\"contentselect\" value=\"main\"/>\n     \n    <div id=\"main\">\n     ".toCharArray();
    __oracle_jsp_text[4] = 
    "\n    </div>\n     \n    <div id=\"game\" style=\"display :none;\">\n     ".toCharArray();
    __oracle_jsp_text[5] = 
    "\n    </div>\n     \n    <div id=\"deck\" style=\"display :none;\">\n     ".toCharArray();
    __oracle_jsp_text[6] = 
    "\n    </div>\n    \n    <div id=\"bugss\" style=\"display :none;\">\n     ".toCharArray();
    __oracle_jsp_text[7] = 
    "\n    </div>\n    \n    <div id=\"admin\" style=\"display :none;\">\n      ".toCharArray();
    __oracle_jsp_text[8] = 
    "\n    </div>\n".toCharArray();
    __oracle_jsp_text[9] = 
    "   \n    <div id=\"suser\" style=\"display :none;\">\n      ".toCharArray();
    __oracle_jsp_text[10] = 
    "\n    </div>\n".toCharArray();
    __oracle_jsp_text[11] = 
    "\n    <div id=\"help\" style=\"display :none;\">\n     <iframe width=\"100%\" height=\"100%\" src=\"/doc/commands.html\">\n     </iframe>\n    </div>\n   </div>\n  </div></body>\n</html>\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
