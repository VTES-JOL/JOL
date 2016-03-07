package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;


public class _bugs extends com.orionserver.http.OrionHttpJspPage {


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
    _bugs page = this;
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
    "<p>Browse deckserver bug reports and create new ones</p>\n\n<div id=\"newbugentry\" style=\"display :none;\">\nSummary: <input type=\"text\" name=\"bugsummaryinput\" size=50 maxlength=60 /> <br />\nDescription: <textarea id=\"bugdescripinput\" rows=25 cols=60></textarea> <br />\n</div>\n<button onclick=\"bugentry();\">Create bug report</button>\n<table id=\"bugtable\" width=100%>\n</table>\nDescription: <br />\n<div id=\"bugdescrip\"></div> <br />\nComments:\n<table id=\"commenttable\" width=100%>\n</table>\n\n<!--\n<p>\nKnown bugs:\n<ol>\n<li> Exception thrown if cards drawn on empty deck.\n<li> Sessions authenticate according to originating browser\n</ol>\n\n<p>\nHere is the list of small tweaks I'm working on:\n<ol>\n<li> More compact game history (1 item for transfer, for example)\n<li> burn a card should put all the contained cards (blood dolls, etc) in the ashheap too\n<li> More descriptive text on all the pages\n<li> Server-level logging.\n</ol>\n\n<p> \nAnd some bigger tweaks that are necessary to clear alpha:\n<ol>\n<li> more active controls on the game page - do away with the command text area.\n</ol>\n\n<p> \nAnd some even bigger features that are really necessary before 1.0\n<ol>\n<li> Undo\n</ol>\n\n<p> \nAnd some nice-to-haves\n<ol>\n<li> Pool calculations\n<li> vp/votes for each player shown in the player's region\n</ol>\n-->".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
