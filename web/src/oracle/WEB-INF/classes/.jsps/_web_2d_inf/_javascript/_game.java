package _web_2d_inf._javascript;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;


public class _game extends com.orionserver.http.OrionHttpJspPage {


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
    _game page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
       WebParams params = (WebParams) request.getSession().getAttribute("wparams");
         String prefix = params.getPrefix();
         
         
      out.write(__oracle_jsp_text[1]);
       out.write(prefix); 
      out.write(__oracle_jsp_text[2]);
       out.write(prefix); 
      out.write(__oracle_jsp_text[3]);

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
  
  private static final char __oracle_jsp_text[][]=new char[4][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n".toCharArray();
    __oracle_jsp_text[1] = 
    "\n  <SCRIPT LANGUAGE=\"JavaScript\">\n   <!-- Hide from older browsers\n   var aWindow = '';\n   function openWin(card) // Open card text in separate window (always on top)\n   {\n    var URL = '".toCharArray();
    __oracle_jsp_text[2] = 
    "card?' + card;\n    openWinImpl(URL,\"card\");\n   }\n   function openWinImpl(URL,win)\n   {\n    if (!aWindow.closed && aWindow.location)\n    {\n     aWindow.location.href = URL;\n    }\n    else\n    {\n     aWindow=window.open(URL,win,\"toolbar=yes,width=750,height=250,scrollbars=yes,menubar=no\");\n     if (!aWindow.opener) aWindow.opener = self;\n    }\n    if (window.focus) {aWindow.focus()}\n   }\n  // var tWindow = '';\n   function openTurnWin() // Open old turn in separate window (always on top)\n   {\n    var turn = document.getElementById(\"oldturns\").value;\n    var URL = '".toCharArray();
    __oracle_jsp_text[3] = 
    "turn?' + turn;\n    openWinImpl(URL,\"turn\");\n   }\n   function openHelpWin() // Open command help window\n   {\n    var URL = '/doc/commands.html';\n    openWinImpl(URL,\"help\");\n   }\n   function details(thistag) // Toggle region details on/off\n   {\n    var region = document.getElementById(\"region\" + thistag);\n    if (region.style.display=='none') // Details not displayed\n    {\n     region.style.display = ''; // Show details\n     document.getElementById(thistag).innerHTML=\"-\";\n    } \n    else // Details already displayed\n    {\n     region.style.display = 'none'; // Hide details\n     document.getElementById(thistag).innerHTML=\"+\";\n    }\n   }\n   function collapse() // Hide all region details\n   {\n    for(i=1; i<6; i++) // For each player (1-5)\n    {\n\tvar region = document.getElementById(\"regionr\" + i);\n\t//document.getElementById(\"r\"+i).innerHTML=\"+\";\n\t//region.style.display = 'none'; // Hide ready region details\n        var region = document.getElementById(\"regiont\" + i);\n\tdocument.getElementById(\"t\"+i).innerHTML=\"+\";\n\tregion.style.display = 'none'; // Hide topor region details\n\tregion = document.getElementById(\"regioni\" + i);\n\tdocument.getElementById(\"i\"+i).innerHTML=\"+\";\n\tregion.style.display = 'none'; // Hide inactive region details\n\tregion = document.getElementById(\"regiona\" + i);\n\tdocument.getElementById(\"a\"+i).innerHTML=\"+\";\n\tregion.style.display = 'none'; // Hide ashheap region details\n    }\n   }\n   // Stop hiding from older browsers -->\n  </SCRIPT>".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
