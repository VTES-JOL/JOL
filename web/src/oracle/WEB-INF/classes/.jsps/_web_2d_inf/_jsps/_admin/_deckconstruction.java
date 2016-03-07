package _web_2d_inf._jsps._admin;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import cards.model.*;
import cards.local.NormalizeDeck;
import java.util.Iterator;
import deckserver.servlet.DeckServlet;
import java.io.PrintWriter;


public class _deckconstruction extends com.orionserver.http.OrionHttpJspPage {


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
    _deckconstruction page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
      out.write(__oracle_jsp_text[4]);
      out.write(__oracle_jsp_text[5]);
      {
        String __url=OracleJspRuntime.toStr("../../javascript/game.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[6]);
       
         DeckParams p = (DeckParams) request.getAttribute("dparams");
         WebParams params = (WebParams) request.getSession().getAttribute("wparams");
      
      out.write(__oracle_jsp_text[7]);
       if(p.getType().equals("All")) { 
      out.write(__oracle_jsp_text[8]);
      }
      out.write(__oracle_jsp_text[9]);
       for(int i = 0; i < CardEntry.types.length; i++) { 
          String type = CardEntry.types[i]; 
      out.write(__oracle_jsp_text[10]);
       out.write(type); 
      out.write(__oracle_jsp_text[11]);
       if(p.getType().equals(type)) { 
      out.write(__oracle_jsp_text[12]);
      }
      out.write(__oracle_jsp_text[13]);
       out.write(type); 
      out.write(__oracle_jsp_text[14]);
        } 
      out.write(__oracle_jsp_text[15]);
       out.write(p.getQuery()); 
      out.write(__oracle_jsp_text[16]);
       CardEntry[] cards = p.getCards();
         for(int i = 0; i < cards.length; i++) { 
      out.write(__oracle_jsp_text[17]);
       out.write(cards[i].getCardId()); 
      out.write(__oracle_jsp_text[18]);
         request.setAttribute("cparams", new CardParams(cards[i])); 
      out.write(__oracle_jsp_text[19]);
      {
        String __url=OracleJspRuntime.toStr("../state/card.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[20]);
         } 
      out.write(__oracle_jsp_text[21]);
       out.write(Math.max(10,Math.min(p.getDeckObj().getCards().length,50)) + "");
      out.write(__oracle_jsp_text[22]);
        
          out.write(p.getDeckObj().getDeckString());
              
      out.write(__oracle_jsp_text[23]);
      {
        String __url=OracleJspRuntime.toStr("./showdeck.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[24]);
       // DeckServlet.printDeckHtml(params,p.getDeckObj(),new PrintWriter(out)); 
      
      out.write(__oracle_jsp_text[25]);
       out.write(p.getName()); 
      out.write(__oracle_jsp_text[26]);
       String[] errors = p.getErrors();
         for(int i = 0; i < errors.length; i++) 
             out.write(errors[i] + "<br/>");
         
      out.write(__oracle_jsp_text[27]);

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
  
  private static final char __oracle_jsp_text[][]=new char[28][];
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
    "\n ".toCharArray();
    __oracle_jsp_text[6] = 
    "\n".toCharArray();
    __oracle_jsp_text[7] = 
    "\n<form method=post>\n  <input type=\"hidden\" name=\"editinit\" value=\"true\"/>\n  Type:\n  <select name=type>\n    <option value=\"All\" ".toCharArray();
    __oracle_jsp_text[8] = 
    " SELECTED ".toCharArray();
    __oracle_jsp_text[9] = 
    " >\n       All\n    </option>\n".toCharArray();
    __oracle_jsp_text[10] = 
    "\n    <option value=\"".toCharArray();
    __oracle_jsp_text[11] = 
    "\" ".toCharArray();
    __oracle_jsp_text[12] = 
    " SELECTED ".toCharArray();
    __oracle_jsp_text[13] = 
    " >\n       ".toCharArray();
    __oracle_jsp_text[14] = 
    "\n    </option>\n".toCharArray();
    __oracle_jsp_text[15] = 
    "\n  </select>\n  Search on\n  <input name=query value=\"".toCharArray();
    __oracle_jsp_text[16] = 
    "\"/>\n  <input type=submit name=construct value=Search />\n  <hr/>\n".toCharArray();
    __oracle_jsp_text[17] = 
    "\n  <input type=checkbox name=newcard value=\"".toCharArray();
    __oracle_jsp_text[18] = 
    "\">\n".toCharArray();
    __oracle_jsp_text[19] = 
    "\n     ".toCharArray();
    __oracle_jsp_text[20] = 
    "\n  </input>\n".toCharArray();
    __oracle_jsp_text[21] = 
    "\n      \n  <table>\n    <td rowspan=10>\n    <textarea cols=50 rows=".toCharArray();
    __oracle_jsp_text[22] = 
    " name=deck>".toCharArray();
    __oracle_jsp_text[23] = 
    "</textarea>\n    </td><td> </td>\n    <td rowspan=10\">\n       ".toCharArray();
    __oracle_jsp_text[24] = 
    "\n".toCharArray();
    __oracle_jsp_text[25] = 
    "\n    </td>\n    <tr>\n      <td><input type=submit name=construct value=\"Adjust deck\" /></td>\n    </tr>\n    <tr>\n      <td>Deck name:<input name=deckname value=\"".toCharArray();
    __oracle_jsp_text[26] = 
    "\" /></td>\n    </tr>\n    <tr>\n      <td><input type=submit name=submit value=\"Submit deck\"/></td>\n    </tr>\n  </table>\n</form>\n\nLines in the deck submission not matched to cards:<br/>\n<b>\n".toCharArray();
    __oracle_jsp_text[27] = 
    "\n</b>\n<p>\n\nUse this page to enter your decks.  Use the search button on the top to search through all\navailable cards.  Check the boxes next to the cards you want to include in your deck.  Hit the\n\"Adjust deck\" button - this adds all the checked cards to the deck.  You can add multiples of\na given card by changing the numeral in the deck listing.  When you're finished, hit the \"Submit\ndeck\" button.  Then give the deck a name, and you're done.\n<p>\nAlternately, you can simply cut/paste a deck into the text area.  A line is formatted like:\n<pre>\n1x Side Strike\n</pre>\nbut unique prefixes (like in this case \"side s\") can be used, caps are optional, and the 1x is optional.\nLines that aren't recognized by the software are ignored, so you can even paste decks with grouping\nor other text included into the text box.\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
