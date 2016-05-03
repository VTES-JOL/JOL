package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;


public class _deck extends com.orionserver.http.OrionHttpJspPage {


  // ** Begin Declarations


  // ** End Declarations

  public void _jspService(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {

    response.setContentType( "text/html");
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
    _deck page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);

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
  
  private static final char __oracle_jsp_text[][]=new char[1][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n<table border=1 width=100%>\n<tr>\n<td width=25% align=top>\nYour decks:\n<div class=\"gamediv\">\n<table class=\"gametable\" id=\"decks\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\" width=\"100%\">\n</table>\n</div>\nOpen games:\n<table id=\"opengames\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\" width=\"100%\">\n</table>\nRegister for game:\n<select id=\"reggames\">\n</select>\n<select id=\"regdecks\">\n</select>\n<button onclick=\"doregister();\">Register</button>\n</td>\n<td width=40%>\n<table width=100%><tr><td align=left>Name: <input readonly=readonly id=deckname type=text size=20 maxlength=30/></td>\n <td align=right><div id=\"noedit\"> <button onclick=\"doedit();\">Edit</button><button onclick=\"donewdeck();\">New</button></div>\n  <div id=\"deckedit\" style=\"display :none;\"> <button onclick=\"dosave();\">Save</button></div></td></tr></table>\n<textarea rows=25 cols=60 id=\"decktext\" readonly=readonly ></textarea>\n<table width=\"100%\"><tr><td align=left>Search for cards:</td><td align=right>\nShuffle:<input type=\"checkbox\" name=\"shuffle\" value=\"yes\" /><button id=\"adjust\" onclick=\"doadjust();\">Parse deck</button>\n</td></tr></table>\n<form action=\"javascript:dosearch();\">\n  Type: <select id=\"cardtype\"><option value=\"All\">All</option></select><br />\n  Query: <input type=text id=cardquery /><br />\n  <button onclick=\"dosearch();\">Search cards</button><br />\n</form>\n<div class=\"cardsdiv\">\n  <table class=\"gametable\" id=\"showcards\" cellspacing=0 cellpadding=0 border=0></table>\n</div></td>\n<td align=top>\nDeck errors: <br />\n<div id=\"deckerrors\" class=\"errdiv\">\n</div>\n<hr />\n<div id=\"deckcontentdiv\"><table><tr><td id=\"deckcontents\"></td></tr></table></div>\n<hr />\nCard Texts:\n  <select id=\"deckcards\" onchange=\"selectCardDeck()\"></select>\n  <input type=hidden id=\"cardSelect\" value=\"history\"/>\n  <div class=\"history\" id=\"cardtext\"></div>\n</td></tr></table>".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
