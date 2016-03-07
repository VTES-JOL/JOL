package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import nbclient.vtesmodel.JolGame;


public class _state extends com.orionserver.http.OrionHttpJspPage {


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
    _state page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
       WebParams params = (WebParams) request.getSession().getAttribute("wparams");
         if(params == null) { params = new WebParams(request);
         String game = request.getParameter("game");
         params.setGame(game);
         String player = request.getParameter("player"); // PENDING from session
         if(player == null) player = "";
         params.setPlayer(player);
         }
         String game = params.getGame();
         JolGame jgame = AdminFactory.get(application).getGame(game);
         request.setAttribute("game",jgame);
         request.setAttribute("params",params);
         
      out.write(__oracle_jsp_text[4]);
      if (true) {
        String __url=OracleJspRuntime.toStr("../state/game.jsp");
        // Forward 
        pageContext.forward( __url);
        return;
      }

      out.write(__oracle_jsp_text[5]);

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
  
  private static final char __oracle_jsp_text[][]=new char[6][];
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
    "\n<html>\n<head>\n  ".toCharArray();
    __oracle_jsp_text[5] = 
    "\n</html>\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
