package _web_2d_inf._jsps._dwr;

import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.*;
import nbclient.vtesmodel.*;


public class _region extends com.orionserver.http.OrionHttpJspPage {


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
    _region page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
      out.write(__oracle_jsp_text[2]);
      out.write(__oracle_jsp_text[3]);
       JolGame game = (JolGame) request.getAttribute("game");
         RegionParams r = (RegionParams) request.getAttribute("rparams");
         
      out.write(__oracle_jsp_text[4]);
       out.write(r.getIndex());
      out.write(__oracle_jsp_text[5]);
       out.write(r.getIndex());
      out.write(__oracle_jsp_text[6]);
       out.write(r.getColor()); 
      out.write(__oracle_jsp_text[7]);
       out.write(r.getText()); 
      out.write(__oracle_jsp_text[8]);
       out.write(String.valueOf(r.getSize())); 
      out.write(__oracle_jsp_text[9]);
       out.write(r.getIndex());
      out.write(__oracle_jsp_text[10]);
       for(int i = 0; i < r.getSize(); i++) { 
                    request.setAttribute("cparams", r.getCardParam(i));
                    
      out.write(__oracle_jsp_text[11]);
      {
        String __url=OracleJspRuntime.toStr("card.jsp");
        // Include 
        pageContext.include( __url,false);
        if (pageContext.getAttribute(OracleJspRuntime.JSP_REQUEST_REDIRECTED, PageContext.REQUEST_SCOPE) != null)
          return;
      }

      out.write(__oracle_jsp_text[12]);
       } 
      out.write(__oracle_jsp_text[13]);

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
  
  private static final char __oracle_jsp_text[][]=new char[14][];
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
    "\n\n<a href=\"javascript:details('".toCharArray();
    __oracle_jsp_text[5] = 
    "');\" id=\"".toCharArray();
    __oracle_jsp_text[6] = 
    "\">-</a>\n<span><FONT COLOR=".toCharArray();
    __oracle_jsp_text[7] = 
    "> ".toCharArray();
    __oracle_jsp_text[8] = 
    " (".toCharArray();
    __oracle_jsp_text[9] = 
    ")</span>\n<span id=\"region".toCharArray();
    __oracle_jsp_text[10] = 
    "\">\n    <ol>\n         ".toCharArray();
    __oracle_jsp_text[11] = 
    "\n        <LI>".toCharArray();
    __oracle_jsp_text[12] = 
    "</LI>\n         ".toCharArray();
    __oracle_jsp_text[13] = 
    "\n    </ol>\n</span>\n ".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
