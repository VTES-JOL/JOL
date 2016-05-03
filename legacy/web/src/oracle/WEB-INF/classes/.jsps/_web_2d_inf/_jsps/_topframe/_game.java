package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.WebParams;
import nbclient.vtesmodel.*;


public class _game extends com.orionserver.http.OrionHttpJspPage {


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
    _game page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
       WebParams p = (WebParams) session.getAttribute("wparams");
         String prefix = p.getPrefix();
         String name = p.getGame();
         String player = p.getPlayer();
         JolGame game = JolAdminFactory.INSTANCE.getGame(name);
         int counter = game.getGameCounter();
         if(player == null) player = "";
      
      out.write(__oracle_jsp_text[4]);
       out.write(name); 
      out.write(__oracle_jsp_text[5]);
       out.write(name); 
      out.write(__oracle_jsp_text[6]);
       out.write(prefix); 
      out.write(__oracle_jsp_text[7]);
       out.write(name);
      out.write(__oracle_jsp_text[8]);
       out.write(player);
      out.write(__oracle_jsp_text[9]);
       out.write(prefix); 
      out.write(__oracle_jsp_text[10]);
       out.write(name); 
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
    "\n".toCharArray();
    __oracle_jsp_text[3] = 
    "\n".toCharArray();
    __oracle_jsp_text[4] = 
    "\n<html>\n  <head>\n    <title> JOL - ".toCharArray();
    __oracle_jsp_text[5] = 
    "\n    </title>\n  </head>\n  <frameset border=\"0\" cols=\"100%,*\">\n    <frame name=\"stateframe".toCharArray();
    __oracle_jsp_text[6] = 
    "\" src=\"".toCharArray();
    __oracle_jsp_text[7] = 
    "state.jsp?game=".toCharArray();
    __oracle_jsp_text[8] = 
    "&player=".toCharArray();
    __oracle_jsp_text[9] = 
    "\"/>\n    <frame name=\"pollframe\" src=\"".toCharArray();
    __oracle_jsp_text[10] = 
    "poll.jsp?game=".toCharArray();
    __oracle_jsp_text[11] = 
    "\"/>\n  </frameset>\n  \n</html>\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
