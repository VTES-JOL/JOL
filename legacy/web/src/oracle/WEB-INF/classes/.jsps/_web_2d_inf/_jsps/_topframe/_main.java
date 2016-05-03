package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;


public class _main extends com.orionserver.http.OrionHttpJspPage {


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
    _main page = this;
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
    "\n<table width=100%>\n<tr>\n<td width=25% align=top>\n<div id=\"player\" style=\"display: none;\">\nYour games:\n<div id=\"owngamediv\" class=\"gamediv\">\n<table class=\"gametable\" id=\"owngames\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\" width=\"100%\">\n</table>\n</div>\n</div>\n<div id=\"register\">\nRegister for deckserver.net to create decks and join games!\n<form method=post>\nName:<input type=\"text\" size=15 name=\"newplayer\"/>\n<br />\nPassword:<input type=\"password\" size=15\" name=\"newpassword\"/>\n<br />\nEmail:<input type=\"text\" size=30 name=\"newemail\"/>\n<br />\n<input type=submit name=\"register\" value=\"Register\"/>\n</form>\n</div>\n</td>\n<td width=50%>\n<p>\nWelcome to JOL-3, the latest version of Jyhad-OnLine, where you can play Vampire-The Eternal Struggle(VTES) card games online over\nthe web.\nTo play games on this server, register, login, create some decks, and email register@deckserver.net to\nget into a game.\n</p>\n<div id=\"globalchat\" style=\"display: none;\">\nNow logged on: <span id=\"whoson\"></span><br />\nAdmins currently on: <span id=\"adson\"></span>\n<br />Chat to organize new games: \n<div class=history id=\"gchatwin\">\n<table width=100% class=\"chattable\" id=\"gchattable\" cellspacing=0 cellpadding=0 border=0>\n</table>\n</div>\n<form action=\"javascript: globchat();\">\n<span id=\"chatstamp\"></span> Chat: <input type=\"text\" style=\"width:100%\" maxlength=100 id=\"gchat\"/>\n</form>\n</div>\nCurrently active games:\n<div class=\"gamediv\">\n<table id=\"activegames\" border=\"1\" cellspacing=\"1\" cellpadding=\"1\" width=\"100%\">\n</table>\n</div>\n</td>\n<td width=20% align=top>\n<div id=\"news\">\n</div>\n<script type=\"text/javascript\"><!--\ngoogle_ad_client = \"pub-9814664900369745\";\ngoogle_alternate_color = \"000000\";\ngoogle_ad_width = 180;\ngoogle_ad_height = 150;\ngoogle_ad_format = \"180x150_as\";\ngoogle_ad_type = \"text\";\ngoogle_ad_channel = \"\";\ngoogle_color_border = \"000000\";\ngoogle_color_bg = \"000000\";\ngoogle_color_link = \"FFFF66\";\ngoogle_color_text = \"CC0000\";\ngoogle_color_url = \"FFFF66\";\n//-->\n</script>\n<script type=\"text/javascript\"\n  src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n</script>\n<script type=\"text/javascript\"><!--\ngoogle_ad_client = \"pub-9814664900369745\";\ngoogle_alternate_color = \"000000\";\ngoogle_ad_width = 200;\ngoogle_ad_height = 90;\ngoogle_ad_format = \"200x90_0ads_al\";\ngoogle_ad_channel = \"\";\ngoogle_color_border = \"000000\";\ngoogle_color_bg = \"000000\";\ngoogle_color_link = \"FFFF66\";\ngoogle_color_text = \"CC0000\";\ngoogle_color_url = \"FFFF66\";\n//-->\n</script>\n<script type=\"text/javascript\"\n  src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">\n</script>\n</td></tr></table>".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
