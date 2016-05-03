package _web_2d_inf._jsps._state;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;


public class _hand extends com.orionserver.http.OrionHttpJspPage {


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
    _hand page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
       HandParams p = (HandParams) request.getAttribute("hparams"); 
      out.write(__oracle_jsp_text[1]);
       out.write(p.getColor()); 
      out.write(__oracle_jsp_text[2]);
       out.write(p.getText());
                     out.write(" (" + p.getSize() + ")");  
      out.write(__oracle_jsp_text[3]);
       for(int i = 0; i < p.getSize(); i++) { 
           request.setAttribute("cparams", p.getCardParam(i)); 
      out.write(__oracle_jsp_text[4]);
      {
        String __url=OracleJspRuntime.toStr("card.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[5]);
       } 
      out.write(__oracle_jsp_text[6]);

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
  
  private static final char __oracle_jsp_text[][]=new char[7][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n ".toCharArray();
    __oracle_jsp_text[1] = 
    "\n<FONT COLOR=".toCharArray();
    __oracle_jsp_text[2] = 
    ">\n            ".toCharArray();
    __oracle_jsp_text[3] = 
    "\n<OL>\n".toCharArray();
    __oracle_jsp_text[4] = 
    "\n   <LI>".toCharArray();
    __oracle_jsp_text[5] = 
    "</LI>\n".toCharArray();
    __oracle_jsp_text[6] = 
    "\n</OL>".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
