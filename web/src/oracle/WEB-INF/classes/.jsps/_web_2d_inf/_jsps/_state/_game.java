package _web_2d_inf._jsps._state;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import nbclient.vtesmodel.JolGame;


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
       WebParams params = (WebParams) session.getAttribute("wparams");
         JolGame game = (JolGame) request.getAttribute("game");
         boolean showPlayer = params != null && params.isInGame();
         boolean isPlayer = params != null && params.isPlayer();
         
      out.write(__oracle_jsp_text[4]);
       out.write(game.getName()); 
      out.write(__oracle_jsp_text[5]);
       out.write(game.getName()); 
      out.write(__oracle_jsp_text[6]);
      {
        String __url=OracleJspRuntime.toStr("styles.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[7]);
      {
        String __url=OracleJspRuntime.toStr("../../javascript/game.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[8]);
       if(showPlayer) { 
      out.write(__oracle_jsp_text[9]);
         if(isPlayer) { 
      out.write(__oracle_jsp_text[10]);
        String player = params.getPlayer();
                 request.setAttribute("hparams",new HandParams(game,player,"fuschia","Cards in hand",JolGame.HAND)); 
      out.write(__oracle_jsp_text[11]);
      {
        String __url=OracleJspRuntime.toStr("hand.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[12]);
       } 
      out.write(__oracle_jsp_text[13]);
      {
        String __url=OracleJspRuntime.toStr("command.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[14]);
       } 
      out.write(__oracle_jsp_text[15]);
      {
        String __url=OracleJspRuntime.toStr("messages.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[16]);
      {
        String __url=OracleJspRuntime.toStr("state.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[17]);

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
  
  private static final char __oracle_jsp_text[][]=new char[18][];
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
    "\n  <TITLE>".toCharArray();
    __oracle_jsp_text[5] = 
    "</TITLE>\n  <META NAME=\"decription\" CONTENT=\"JOL3 Version 0.2\">\n  <META NAME=\"keywords\" CONTENT=\"".toCharArray();
    __oracle_jsp_text[6] = 
    ", JOL\">\n  <META NAME=\"robots\" CONTENT=\"noindex, nofollow\">\n  <META NAME=\"rating\" CONTENT=\"general\">\n  <META NAME=\"generator\" CONTENT=\"vi\">\n".toCharArray();
    __oracle_jsp_text[7] = 
    "\n </HEAD>\n ".toCharArray();
    __oracle_jsp_text[8] = 
    "\n <BODY BGCOLOR=\"black\" \n       TEXT=\"red\"\n       LINK=\"yellow\"\n       VLINK=\"yellow\"\n       ALINK=\"yellow\"\n       onLoad=\"collapse();\">\n  <TABLE border=2>\n".toCharArray();
    __oracle_jsp_text[9] = 
    "\n   <TR>\n".toCharArray();
    __oracle_jsp_text[10] = 
    "\n    <TD valign=top WIDTH=\"30%\">\n       ".toCharArray();
    __oracle_jsp_text[11] = 
    "\n       ".toCharArray();
    __oracle_jsp_text[12] = 
    "\n    </td>\n    ".toCharArray();
    __oracle_jsp_text[13] = 
    "\n    <td valign=top>\n       ".toCharArray();
    __oracle_jsp_text[14] = 
    "\n    </td>\n   </TR>\n".toCharArray();
    __oracle_jsp_text[15] = 
    "\n   <tr>\n    <TD colspan='2'>\n      ".toCharArray();
    __oracle_jsp_text[16] = 
    "\n    </TD>\n   </TR>\n   <TR>\n    <TD COLSPAN='2'>\n      ".toCharArray();
    __oracle_jsp_text[17] = 
    "\n    </TD>\n   </TR>\n  </TABLE>\n </BODY>\n\n    \n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
