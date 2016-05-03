package _web_2d_inf._jsps._state;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import nbclient.vtesmodel.JolGame;


public class _command extends com.orionserver.http.OrionHttpJspPage {


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
    _command page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
       WebParams params = (WebParams) session.getAttribute("wparams");
         JolGame game = (JolGame) request.getAttribute("game");
         String player = params.getPlayer();
         String[] players = game.getPlayers();
         boolean active = player.equals(game.getActivePlayer());
         
      out.write(__oracle_jsp_text[4]);
       out.write(params.getPrefix() + params.getGame()); 
      out.write(__oracle_jsp_text[5]);
       if(active) { 
      out.write(__oracle_jsp_text[6]);
           boolean show = false;
             String phase = game.getPhase();
             for(int i = 0; i < game.TURN_PHASES.length; i++) {
                 if(phase.equals(game.TURN_PHASES[i])) show = true;
                 if(show) { 
      out.write(__oracle_jsp_text[7]);
       out.write(game.TURN_PHASES[i]); 
      out.write(__oracle_jsp_text[8]);
       out.write(game.TURN_PHASES[i]);
      out.write(__oracle_jsp_text[9]);
                   }
                 } 
      out.write(__oracle_jsp_text[10]);
       } 
      out.write(__oracle_jsp_text[11]);
       for(int i = 0; i < players.length; i++) { 
      out.write(__oracle_jsp_text[12]);
       out.write(players[i]); 
      out.write(__oracle_jsp_text[13]);
       out.write(players[i] + "(" + game.getPingTag(players[i]) + ")");
      out.write(__oracle_jsp_text[14]);
        } 
      out.write(__oracle_jsp_text[15]);
       if(active) { 
      out.write(__oracle_jsp_text[16]);
       } 
      out.write(__oracle_jsp_text[17]);
       String res = params.getStatusMsg();
         if(res != null)  { 
      out.write(__oracle_jsp_text[18]);
          out.write("Status: " + res); 
         } 
      out.write(__oracle_jsp_text[19]);
      out.write(__oracle_jsp_text[20]);
       out.write(game.getGlobalText()); 
      out.write(__oracle_jsp_text[21]);
       out.write(game.getPlayerText(player)); 
      out.write(__oracle_jsp_text[22]);

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
  
  private static final char __oracle_jsp_text[][]=new char[23][];
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
    "\n<form target=\"_top\" action=\"".toCharArray();
    __oracle_jsp_text[5] = 
    "\" method=post>\n<table><tr><td rowspan=2>\n<table><tr><td>\n".toCharArray();
    __oracle_jsp_text[6] = 
    "\nPhase: <select name=phase>\n".toCharArray();
    __oracle_jsp_text[7] = 
    "\n    <option value=\"".toCharArray();
    __oracle_jsp_text[8] = 
    "\">".toCharArray();
    __oracle_jsp_text[9] = 
    "</option>\n".toCharArray();
    __oracle_jsp_text[10] = 
    "\n</select>\n</td></tr><tr><td>\n".toCharArray();
    __oracle_jsp_text[11] = 
    "\nJOL COMMAND: (<A HREF=\"javascript:openHelpWin()\">docs</a>)\n<input name=command size=25 maxlength=100>\n</td></tr><tr><td>\nCHAT MESSAGE: <input name=message size=25 maxlength=120>\n</td></tr><tr><td>\nPING:\n<select name=ping>\n    <option value=\"\" SELECTED></option>\n".toCharArray();
    __oracle_jsp_text[12] = 
    "\n    <option value=\"".toCharArray();
    __oracle_jsp_text[13] = 
    "\">".toCharArray();
    __oracle_jsp_text[14] = 
    "</option>\n".toCharArray();
    __oracle_jsp_text[15] = 
    "\n</select>\n</td></tr><tr><td>\n".toCharArray();
    __oracle_jsp_text[16] = 
    "\nEnd Turn? <select name=\"newturn\">\n<option value=\"no\" SELECTED>No</option>\n<option value=\"yes\">Yes</option>\n</select>\n</td></tr><tr><td>\n".toCharArray();
    __oracle_jsp_text[17] = 
    "\n<input type=\"submit\" value=\"Submit\"/>\n".toCharArray();
    __oracle_jsp_text[18] = 
    "\n</td></tr><tr><td>\n".toCharArray();
    __oracle_jsp_text[19] = 
    "\n".toCharArray();
    __oracle_jsp_text[20] = 
    "\n</td></tr></table></td>\n<td valign=top>\nGlobal notes and pending actions:<br>\n<textarea rows=\"4\" cols=\"50\" name=\"global\">".toCharArray();
    __oracle_jsp_text[21] = 
    "</textarea>\n</td></tr>\n<tr><td valign=top>\nPrivate notepad:<br>\n<textarea rows=\"4\" cols=\"50\" name=\"notes\">".toCharArray();
    __oracle_jsp_text[22] = 
    "</textarea>\n</td></tr>\n</table>\n</form>".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
