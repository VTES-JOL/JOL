package _web_2d_inf._jsps._admin;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import cards.model.*;
import cards.local.NormalizeDeck;
import java.util.*;
import deckserver.servlet.DeckServlet;


public class _showdeck extends com.orionserver.http.OrionHttpJspPage {


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
    _showdeck page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
      out.write(__oracle_jsp_text[4]);
      {
        String __url=OracleJspRuntime.toStr("../../javascript/game.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[5]);
       
         DeckParams p = (DeckParams) request.getAttribute("dparams");
         Map deck = DeckServlet.getDeckHtmlMap(p);
         Map map = (Map) deck.get("Vampire");
         Map mapi = (Map) deck.get("Imbued");
         deck.remove("Vampire");
         deck.remove("Imbued");
         if(map == null) map = new HashMap();
         if(mapi == null) mapi = new HashMap();
         int csize = DeckServlet.sumMap(map.values()) + DeckServlet.sumMap(mapi.values());
      
      out.write(__oracle_jsp_text[6]);
       out.write(csize+""); 
      out.write(__oracle_jsp_text[7]);
       if(mapi.size() > 0) out.write("Vampires: <br/>"); 
      out.write(__oracle_jsp_text[8]);
       for(Iterator i = map.keySet().iterator();i.hasNext();) { 
          CardEntry card = (CardEntry) i.next();
          request.setAttribute("cparams", new CardParams(card));
          out.write(map.get(card).toString() + "x "); 
      out.write(__oracle_jsp_text[9]);
       out.write(card.getCardId()); 
      out.write(__oracle_jsp_text[10]);
       out.write(card.getName() + "(G" + card.getGroup() + ")"); 
      out.write(__oracle_jsp_text[11]);
       } 
         if(mapi.size() > 0) {
           out.write("Imbued: <br/>");
           for(Iterator i = mapi.keySet().iterator();i.hasNext();) { 
            CardEntry card = (CardEntry) i.next();
            request.setAttribute("cparams", new CardParams(card));
            out.write(mapi.get(card).toString() + "x "); 
      out.write(__oracle_jsp_text[12]);
       out.write(card.getCardId()); 
      out.write(__oracle_jsp_text[13]);
       out.write(card.getName() + "(G" + card.getGroup() + ")"); 
      out.write(__oracle_jsp_text[14]);
         }
         } 
         Collection sum = new ArrayList();
         for(Iterator i = deck.values().iterator();i.hasNext();) {
             sum.addAll(((Map)i.next()).values());
             }
         int size = DeckServlet.sumMap(sum);
      
      out.write(__oracle_jsp_text[15]);
       out.write(size + ""); 
      out.write(__oracle_jsp_text[16]);
       map = (Map) deck.get("Master");
         if(map == null) map = new HashMap();
         deck.remove("Master");
      out.write(__oracle_jsp_text[17]);
       out.write(DeckServlet.sumMap(map.values()) + ""); 
      out.write(__oracle_jsp_text[18]);
       for(Iterator i = map.keySet().iterator();i.hasNext();) { 
          CardEntry card = (CardEntry) i.next();
          request.setAttribute("cparams", new CardParams(card));
          out.write(map.get(card).toString() + "x ");
      out.write(__oracle_jsp_text[19]);
       out.write(card.getCardId()); 
      out.write(__oracle_jsp_text[20]);
       out.write(card.getName()); 
      out.write(__oracle_jsp_text[21]);
       }
         Collection types = new TreeSet(deck.keySet());
         for(Iterator j = types.iterator(); j.hasNext() ; ) { 
             Object type = j.next();
             map = (Map) deck.get(type);
             out.write(type.toString()); 
      out.write(__oracle_jsp_text[22]);
       out.write(DeckServlet.sumMap(map.values())+"");
      out.write(__oracle_jsp_text[23]);
       for(Iterator i = map.keySet().iterator();i.hasNext();) { 
          CardEntry card = (CardEntry) i.next();
          request.setAttribute("cparams", new CardParams(card));
          out.write(map.get(card).toString() + "x ");
      out.write(__oracle_jsp_text[24]);
       out.write(card.getCardId()); 
      out.write(__oracle_jsp_text[25]);
       out.write(card.getName()); 
      out.write(__oracle_jsp_text[26]);
        } 
      } 
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
    "\n ".toCharArray();
    __oracle_jsp_text[5] = 
    "\n".toCharArray();
    __oracle_jsp_text[6] = 
    "\n<b>Crypt: (".toCharArray();
    __oracle_jsp_text[7] = 
    ")</b><br/>\n".toCharArray();
    __oracle_jsp_text[8] = 
    "\n".toCharArray();
    __oracle_jsp_text[9] = 
    "\n    <A HREF=\"javascript:openWin('".toCharArray();
    __oracle_jsp_text[10] = 
    "');\">\n ".toCharArray();
    __oracle_jsp_text[11] = 
    "\n</a> \n<br/>\n".toCharArray();
    __oracle_jsp_text[12] = 
    "\n      <A HREF=\"javascript:openWin('".toCharArray();
    __oracle_jsp_text[13] = 
    "');\">\n   ".toCharArray();
    __oracle_jsp_text[14] = 
    "\n</a> \n<br/>\n".toCharArray();
    __oracle_jsp_text[15] = 
    "\n<b>Library: (".toCharArray();
    __oracle_jsp_text[16] = 
    ")</b><br/>\n".toCharArray();
    __oracle_jsp_text[17] = 
    "\nMaster: (".toCharArray();
    __oracle_jsp_text[18] = 
    ")<br/>\n".toCharArray();
    __oracle_jsp_text[19] = 
    "\n<A HREF=\"javascript:openWin('".toCharArray();
    __oracle_jsp_text[20] = 
    "');\">\n ".toCharArray();
    __oracle_jsp_text[21] = 
    "\n</A>\n<!--jsp:include page=\"../state/card.jsp\"/-->\n<br/>\n".toCharArray();
    __oracle_jsp_text[22] = 
    ": (".toCharArray();
    __oracle_jsp_text[23] = 
    ")<br/>\n".toCharArray();
    __oracle_jsp_text[24] = 
    "\n<A HREF=\"javascript:openWin('".toCharArray();
    __oracle_jsp_text[25] = 
    "');\">\n ".toCharArray();
    __oracle_jsp_text[26] = 
    "\n</A>\n<!--jsp:include page=\"../state/card.jsp\"/-->\n<br/>\n".toCharArray();
    __oracle_jsp_text[27] = 
    "\n   \n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
