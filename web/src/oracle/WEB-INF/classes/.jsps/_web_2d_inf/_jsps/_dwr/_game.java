package _web_2d_inf._jsps._dwr;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;


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
  
  private static final char __oracle_jsp_text[][]=new char[2][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n".toCharArray();
    __oracle_jsp_text[1] = 
    "\n<TABLE width=100% border=2>\n   <TR>\n    <TD valign=top WIDTH=\"30%\" id=\"hand\"/>\n    <td id=\"dsForm\"><form onsubmit=\"javascript:return dosubmit();\">\n     <table>\n      <tr id=\"phasecommand\"><td>Phase:</td><td><select id=\"phase\" name=\"phase\"/></td></tr>\n      <tr><td>Command:</td>\n         <td><input name=\"command\" type=\"text\" id=\"command\" size=25 maxlength=100/></td>\n      </tr>\n      <tr><td>Chat:</td>\n      <td><input name=\"chat\" type=\"text\" id=\"chat\" size=25 maxlength=100/>\n      </td></tr>\n      <tr><td>Ping: </td><td><select id=\"ping\" name=\"ping\"/></td></tr>\n      <tr id=\"endcommand\"><td>End turn?</td><td><select id=\"endturn\" name=\"endturn\"><option value=\"No\">No</option><option value=\"Yes\">Yes</option></select></td></tr>\n      <tr><td><input name=\"Submit\" type=\"submit\" value=\"Submit\"/></td></tr>\n      <tr><td colspan=\"2\"><span id=\"status\"/></td></tr>\n     </table>\n    </form></td>\n   <td colspan=2 valign=top>Global notes and pending actions: <br />\n     <textarea rows=\"4\" cols=\"50\" name=\"global\" id=\"global\"></textarea><br />\n     <div id=\"playerPad\">\n      Private notepad:<br>\n     <textarea rows=\"4\" cols=\"50\" name=\"notes\" id=\"notes\"></textarea>\n     </div>\n   </td>\n  </tr>\n<tr>\n<td align=left><font color=yellow><table><tr><td align=left><div id=\"gamename\"></div></td><td align=right><div id=\"gamestamp\"></div></tr></table></font></td>\n<td align=center><font color=white>Current Turn: <span id=\"turnlabel\"/>.</td>\n<td align=left><a name=\"cd\"><select id=\"cards\" name=\"cards\" onchange=\"selectCard()\"></a>\n    <option value=\"NOCARD\">Show history</option>\n  </select></td>\n<td align=right>Turn selector:<select id=\"turns\" name=\"turns\" onchange=\"getHistory()\" /></td>\n</tr>\n   <tr>\n    <TD colspan=\"2\" width=\"60%\"><div class=\"history\" id=\"curturn\"><table class=\"chattable\" cellspacing=0 cellpadding=0 border=0 id=\"curturntable\"></table> </div></TD>\n    <td colspan=\"2\"><div class=\"history\" id=\"extra\"><input type=hidden id=\"extraSelect\" value=\"history\"/><div id=\"history\"/></div></td>\n   </TR>\n   <TR>\n    <TD colspan=\"4\">\n      <span id=\"state\"/>\n    </TD>\n   </TR>\n  </TABLE>".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
