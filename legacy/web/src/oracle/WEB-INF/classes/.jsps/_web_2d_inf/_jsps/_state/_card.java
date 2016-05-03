package _web_2d_inf._jsps._state;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import nbclient.model.Card;
import cards.model.CardEntry;
import nbclient.vtesmodel.JolGame;


public class _card extends com.orionserver.http.OrionHttpJspPage {


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
    _card page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
       JolGame game = (JolGame) request.getAttribute("game");
         CardParams p = (CardParams) request.getAttribute("cparams");
         Card c = p.getCard();
         if (p.isHidden()) { 
      out.write(__oracle_jsp_text[4]);
       } else { 
      out.write(__oracle_jsp_text[5]);
       out.write(p.getId()); 
      out.write(__oracle_jsp_text[6]);
       out.write(p.getName()); 
      out.write(__oracle_jsp_text[7]);
       }
        if(game != null) {
         int counters = game.getCounters(c.getId());
         int capac = game.getCapacity(c.getId());
         if(counters > 0 || capac > 0) {
             String lab = (capac > 0) ? "Blood" : "Counters";
             out.write(", " + lab + ": " + counters);
             if(capac > 0) out.write("/" + capac);
         }
         if(game.isTapped(c.getId())) {
             out.write(", TAPPED");
         }
         String text = game.getText(c.getId());
         if(text != null && text.length() > 0) {
             out.write("," + text);
             }
         if(p.doNesting()) { 
             Card[] cards = (Card[])c.getCards();
             if(cards != null && cards.length > 0) 
      out.write(__oracle_jsp_text[8]);
       for(int i = 0; i < cards.length; i++) { 
                     request.setAttribute("cparams", new CardParams(cards[i])); 
      out.write(__oracle_jsp_text[9]);
      {
        String __url=OracleJspRuntime.toStr("card.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[10]);
               } 
      out.write(__oracle_jsp_text[11]);
        }
        } else { // print extra stuff during card viewing
            CardEntry card = p.getEntry();
            if(card.isCrypt()) {
                String[] text = card.getFullText();
                String group = "G" + card.getGroup();
                out.write("(" + group + ")");
                }
        } 

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
    "\nXXXXXX\n".toCharArray();
    __oracle_jsp_text[5] = 
    "\n<A HREF=\"javascript:openWin('".toCharArray();
    __oracle_jsp_text[6] = 
    "');\">\n ".toCharArray();
    __oracle_jsp_text[7] = 
    "\n</A>\n".toCharArray();
    __oracle_jsp_text[8] = 
    "\n       <ol>\n       ".toCharArray();
    __oracle_jsp_text[9] = 
    "\n               ".toCharArray();
    __oracle_jsp_text[10] = 
    "\n".toCharArray();
    __oracle_jsp_text[11] = 
    "\n       </ol>\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
