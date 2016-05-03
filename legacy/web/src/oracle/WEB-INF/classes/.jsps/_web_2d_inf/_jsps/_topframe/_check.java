package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import deckserver.rich.*;
import nbclient.vtesmodel.*;
import java.util.Date;


public class _check extends com.orionserver.http.OrionHttpJspPage {


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
    _check page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
      out.write(__oracle_jsp_text[4]);
      out.write(__oracle_jsp_text[5]);
       WebParams p = (WebParams) session.getAttribute("wparams");
         JolAdminFactory admin = AdminFactory.get(application);
         AdminBean abean = AdminFactory.getBean(application);
         String player = p.getPlayer();
         String game = (String) request.getParameter("game");
         boolean interactive = (player != null) && admin.doInteractive(player);
         GameModel model = abean.getGameModel(game);
         GameView view = model.getView(player);
         boolean refresh = view.isChanged();
         view.clearAccess();
         Date stamp = new Date(model.getTimestamp());
        // boolean refresh = !admin.haveAccessed(game).contains(player);
        // Date stamp = admin.getGameTimeStamp(game);
         int interval = RefreshInterval.calc(stamp);
         String baseurl = request.getRequestURL().toString();
         String gameurl = baseurl.substring(0,baseurl.indexOf("/jol3") + 5) + "/" + game;
         
      out.write(__oracle_jsp_text[6]);
       out.write(interactive + ""); 
      out.write(__oracle_jsp_text[7]);
       out.write(refresh + ""); 
      out.write(__oracle_jsp_text[8]);
       out.write(gameurl); 
      out.write(__oracle_jsp_text[9]);
       out.write(interval + ""); 
      out.write(__oracle_jsp_text[10]);

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
  
  private static final char __oracle_jsp_text[][]=new char[11][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n".toCharArray();
    __oracle_jsp_text[1] = 
    "\n".toCharArray();
    __oracle_jsp_text[2] = 
    "\n".toCharArray();
    __oracle_jsp_text[3] = 
    "\n".toCharArray();
    __oracle_jsp_text[4] = 
    "\n".toCharArray();
    __oracle_jsp_text[5] = 
    "\n".toCharArray();
    __oracle_jsp_text[6] = 
    "\n<html>\n<head>\n<script type=\"text/javascript\">\n\nfunction rls()\n{\n  // location.url = 'http://espn.go.com';\n  // if( !document.getElementById('dontpoll').checked) {\n //   location = 'http://espn.go.com';\n     location.reload();\n  //   }\n  // else {\n  // setTimeout(\"rls()\",5000);\n  // }\n}\n\nfunction start()\n{\nif( ".toCharArray();
    __oracle_jsp_text[7] = 
    " ) {\n\n if ( ".toCharArray();
    __oracle_jsp_text[8] = 
    " )  {\n  with (parent) {\n   location = \"".toCharArray();
    __oracle_jsp_text[9] = 
    "\";\n   }\n  }\n else {\n  setTimeout(\"rls()\", ".toCharArray();
    __oracle_jsp_text[10] = 
    " ); \n  }\n }\n}\n</script>\n</head>\n<body>\n<script type=text/javascript>\n    start();\n</script>\n\n</body>\n\n</html>\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
