package _web_2d_inf._jsps._state;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import nbclient.vtesmodel.JolGame;
import nbclient.vtesmodel.JolAdminFactory;
import nbclient.model.GameAction;


public class _messages extends com.orionserver.http.OrionHttpJspPage {


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
    _messages page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
      out.write(__oracle_jsp_text[4]);
      out.write(__oracle_jsp_text[5]);
       WebParams params = (WebParams) session.getAttribute("wparams");
         JolGame game = (JolGame) request.getAttribute("game");
         
      out.write(__oracle_jsp_text[6]);
       out.write(game.getName()); 
      out.write(__oracle_jsp_text[7]);
       out.write(game.getCurrentTurn() + " " + game.getPhase()); 
      out.write(__oracle_jsp_text[8]);
       String[] turns = game.getTurns();
         for(int i = turns.length - 1; i >= 0; i--) { 
            String link = JolAdminFactory.INSTANCE.getGameId(game.getName()) + "-" + turns[i]; 
      out.write(__oracle_jsp_text[9]);
       out.write(link);
      out.write(__oracle_jsp_text[10]);
          out.write(turns[i]); 
      out.write(__oracle_jsp_text[11]);
          } 
      out.write(__oracle_jsp_text[12]);
       GameAction[] actions = game.getActions(game.getCurrentTurn());
         for(int i = 0; i < actions.length; i++) { 
      out.write(__oracle_jsp_text[13]);
       if(i == actions.length - 1) out.print("SELECTED"); 
      out.write(__oracle_jsp_text[14]);
       if(actions[i].isCommand()) out.print("<b>");
         out.print(actions[i].getText()); 
         if(actions[i].isCommand()) out.print("</b>"); 
      out.write(__oracle_jsp_text[15]);
       } 
      out.write(__oracle_jsp_text[16]);

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
  
  private static final char __oracle_jsp_text[][]=new char[17][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\r\n".toCharArray();
    __oracle_jsp_text[1] = 
    "\r\n".toCharArray();
    __oracle_jsp_text[2] = 
    "\r\n".toCharArray();
    __oracle_jsp_text[3] = 
    "\r\n".toCharArray();
    __oracle_jsp_text[4] = 
    "\r\n".toCharArray();
    __oracle_jsp_text[5] = 
    "\r\n".toCharArray();
    __oracle_jsp_text[6] = 
    "\r\n<table><tr><td align=left>\r\n<font color=yellow>".toCharArray();
    __oracle_jsp_text[7] = 
    "</font><font color=white>Current Turn: ".toCharArray();
    __oracle_jsp_text[8] = 
    ".\r\n</td><td align=right>\r\nOld turns:\r\n<select id=\"oldturns\" name=\"oldturns\">\r\n".toCharArray();
    __oracle_jsp_text[9] = 
    "\r\n<option value=\"".toCharArray();
    __oracle_jsp_text[10] = 
    "\">".toCharArray();
    __oracle_jsp_text[11] = 
    "</option>\r\n".toCharArray();
    __oracle_jsp_text[12] = 
    "\r\n</select>\r\n<A HREF=\"javascript:openTurnWin();\">View old turn</A><BR>\r\n</td></tr><tr><td colspan=2>\r\n<SELECT NAME=\"commands\" SIZE=10 MULTIPLE>\r\n<option>---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------</option>\r\n".toCharArray();
    __oracle_jsp_text[13] = 
    "\r\n<OPTION ".toCharArray();
    __oracle_jsp_text[14] = 
    ">\r\n".toCharArray();
    __oracle_jsp_text[15] = 
    "\r\n</OPTION>\r\n".toCharArray();
    __oracle_jsp_text[16] = 
    "\r\n</SELECT>\r\n</td></tr></table>".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
