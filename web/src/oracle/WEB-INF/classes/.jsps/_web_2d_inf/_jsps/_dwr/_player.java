package _web_2d_inf._jsps._dwr;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import deckserver.dwr.Utils;
import nbclient.vtesmodel.JolGame;


public class _player extends com.orionserver.http.OrionHttpJspPage {


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
    _player page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
      out.write(__oracle_jsp_text[4]);
       String curPlayer = Utils.getPlayer(request);
         JolGame game = (JolGame) request.getAttribute("game");
         String player = (String) request.getAttribute("pparam");
         boolean active = player.equals(curPlayer);
         boolean edge = player.equals(game.getEdge());
         String[] players = game.getPlayers();
         int index = -1;
         for(int i = 0; i < players.length; i++)
             if(players[i].equals(player)) index = i + 1;
         
      out.write(__oracle_jsp_text[5]);
       out.write(player + (edge ? "<font color=yellow>(EDGE)</font>" : "")); 
      out.write(__oracle_jsp_text[6]);
       out.write(String.valueOf(game.getPool(player))); 
      out.write(__oracle_jsp_text[7]);
       out.write(String.valueOf(game.getState().getPlayerLocation(player,JolGame.LIBRARY).getCards().length)); 
      out.write(__oracle_jsp_text[8]);
       out.write(String.valueOf(game.getState().getPlayerLocation(player,JolGame.CRYPT).getCards().length)); 
      out.write(__oracle_jsp_text[9]);
       out.write(String.valueOf(game.getState().getPlayerLocation(player,JolGame.HAND).getCards().length)); 
      out.write(__oracle_jsp_text[10]);
       request.setAttribute("rparams",new RegionParams(game,player,index,"lime","READY",JolGame.READY_REGION,false)); 
      out.write(__oracle_jsp_text[11]);
      {
        String __url=OracleJspRuntime.toStr("region.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[12]);
       request.setAttribute("rparams",new RegionParams(game,player,index,"aqua","TORPOR",JolGame.TORPOR,false)); 
      out.write(__oracle_jsp_text[13]);
      {
        String __url=OracleJspRuntime.toStr("region.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[14]);
       request.setAttribute("rparams",new RegionParams(game,player,index,"blue","INACTIVE",JolGame.INACTIVE_REGION,!active)); 
      out.write(__oracle_jsp_text[15]);
      {
        String __url=OracleJspRuntime.toStr("region.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[16]);
       request.setAttribute("rparams",new RegionParams(game,player,index,"silver","ASHHEAP",JolGame.ASHHEAP,false)); 
      out.write(__oracle_jsp_text[17]);
      {
        String __url=OracleJspRuntime.toStr("region.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[18]);

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
  
  private static final char __oracle_jsp_text[][]=new char[19][];
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
    "\n".toCharArray();
    __oracle_jsp_text[5] = 
    "\n<b>".toCharArray();
    __oracle_jsp_text[6] = 
    "</b>\n(Pool: ".toCharArray();
    __oracle_jsp_text[7] = 
    ")\n<BR>\n<table><tr><td align=left>\n<FONT COLOR=olive>Library:".toCharArray();
    __oracle_jsp_text[8] = 
    "<BR>\n</td><td align=center><FONT COLOR=teal>Crypt:".toCharArray();
    __oracle_jsp_text[9] = 
    "<BR>\n</td><td align=right><FONT COLOR=fuschia>Hand:".toCharArray();
    __oracle_jsp_text[10] = 
    "<BR>\n</td></tr></table>\n<HR>\n".toCharArray();
    __oracle_jsp_text[11] = 
    "\n".toCharArray();
    __oracle_jsp_text[12] = 
    "\n<hr/>\n".toCharArray();
    __oracle_jsp_text[13] = 
    "\n".toCharArray();
    __oracle_jsp_text[14] = 
    "\n<hr/>\n".toCharArray();
    __oracle_jsp_text[15] = 
    "\n".toCharArray();
    __oracle_jsp_text[16] = 
    "\n<hr/>\n".toCharArray();
    __oracle_jsp_text[17] = 
    "\n".toCharArray();
    __oracle_jsp_text[18] = 
    "\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
