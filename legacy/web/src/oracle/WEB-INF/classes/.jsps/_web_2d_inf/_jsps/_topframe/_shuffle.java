package _web_2d_inf._jsps._topframe;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import cards.model.*;
import java.util.*;


public class _shuffle extends com.orionserver.http.OrionHttpJspPage {


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
    _shuffle page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
       
         Map map = (Map) request.getAttribute("sparams");
         CardEntry[] c = (CardEntry[]) map.get("crypt");
         CardEntry[] l = (CardEntry[]) map.get("library");
      
      out.write(__oracle_jsp_text[2]);
       for(int i = 0; i < c.length; i++) { 
          CardEntry card = c[i]; 
      out.write(__oracle_jsp_text[3]);
       out.write(card.getCardId()); 
      out.write(__oracle_jsp_text[4]);
       out.write(card.getName()); 
      out.write(__oracle_jsp_text[5]);
       } 
      out.write(__oracle_jsp_text[6]);
       for(int i = 0; i < l.length; i++) { 
          CardEntry card = l[i]; 
      out.write(__oracle_jsp_text[7]);
       out.write(card.getCardId()); 
      out.write(__oracle_jsp_text[8]);
       out.write(card.getName()); 
      out.write(__oracle_jsp_text[9]);
       } 
      out.write(__oracle_jsp_text[10]);

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
  
  private static final char __oracle_jsp_text[][]=new char[11][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n".toCharArray();
    __oracle_jsp_text[1] = 
    "\n".toCharArray();
    __oracle_jsp_text[2] = 
    "\n<b>Crypt:</b><br/>\n".toCharArray();
    __oracle_jsp_text[3] = 
    "\n    <A HREF=\"javascript:getCardDeck(null,'".toCharArray();
    __oracle_jsp_text[4] = 
    "');\">\n ".toCharArray();
    __oracle_jsp_text[5] = 
    "\n</a> \n<br/>\n".toCharArray();
    __oracle_jsp_text[6] = 
    "\n<b>Library:</b><br/>\n".toCharArray();
    __oracle_jsp_text[7] = 
    "\n    <A HREF=\"javascript:getCardDeck(null,'".toCharArray();
    __oracle_jsp_text[8] = 
    "');\">\n ".toCharArray();
    __oracle_jsp_text[9] = 
    "\n</a> \n<br/>\n".toCharArray();
    __oracle_jsp_text[10] = 
    "\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
