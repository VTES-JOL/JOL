
import oracle.jsp.runtime.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import deckserver.util.WebParams;


public class _main extends com.orionserver.http.OrionHttpJspPage {


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
    _main page = this;
    ServletConfig config = pageContext.getServletConfig();

    try {


      out.write(__oracle_jsp_text[0]);
      out.write(__oracle_jsp_text[1]);
       
      String prefix = application.getInitParameter("prefix");
      WebParams params = (WebParams)session.getAttribute("wparams");
      String error = (params != null) ? params.getStatusMsg() : null;
      
      out.write(__oracle_jsp_text[2]);
      out.write(prefix);
      out.write(__oracle_jsp_text[3]);
      out.write(prefix);
      out.write(__oracle_jsp_text[4]);
      out.write(prefix);
      out.write(__oracle_jsp_text[5]);
      out.write(prefix);
      out.write(__oracle_jsp_text[6]);
      out.write(prefix);
      out.write(__oracle_jsp_text[7]);

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
  
  private static final char __oracle_jsp_text[][]=new char[8][];
  static {
    try {
    __oracle_jsp_text[0] = 
    "\n".toCharArray();
    __oracle_jsp_text[1] = 
    "\n".toCharArray();
    __oracle_jsp_text[2] = 
    "\n   \n<html>\n<head><title>JOL 3</title></head>\n<body>\n\n<p>\nTry the <a href=\"/jol3/beta\">beta</a> site, its much improved, though both methods of playing games work interchangably for now.\n\n<p>\nWelcome to JOL-3, the lastest version of Jyhad-OnLine.  This version is fully interactive,\nas all commands are available through the web site, making it possible to play real-time games\nacross the network with only a browser for a client.  The new JOL\ncommand <a href=\"/doc/commands.html\">set</a> is streamlined and hopefully\nmore useful.\n<p>\nThe first tournament of 2005 is ongoing, see the tourney <a href=\"http://www.mnsi.net/~ghost/jyhad/jt2005.htm\">page</a>\nfor links to games and status.\n<p>\nTo play games on this server, register and login using the links below, then construct some decks.\nYou can cut/paste decks into the deck construction window from an external tool or editor if you like.\nCards go on separate lines, you can prefix with 1x or 2x or 10x if you want multiples of a card.  Prefixes\nof card names are accepted, as well as some common nicknames like WWEF.  Once you've got a deck, send\nemail to register@deckserver.net and I'll put you in a game.  If you've rounded up a group to play\na game together, let me know and I'll put you all in the same game.  Because JOL3 can be truly interactive,\ngames can go fast if everybody is on-line at the same time.\n<p>\n\n<p>\n<ul>\n<li> <a href=\"".toCharArray();
    __oracle_jsp_text[3] = 
    "register\">Register</a> if you haven't registered to this site left.\n<li> <a href=\"".toCharArray();
    __oracle_jsp_text[4] = 
    "login\">Log in</a> if you've already registered.  Login information is\npreserved throughout your browser session.  One login is used for all your games.  You \nhave to be logged on to use any of the subsequent pages.\n<li> Player <a href=\"".toCharArray();
    __oracle_jsp_text[5] = 
    "player\">home page</a>.  This page links to all your games and decks.\n<li> <a href=\"".toCharArray();
    __oracle_jsp_text[6] = 
    "deck\">Deck construction</a>.  Any deck used in a JOL-3 game must be first\nregistered through this page.\n<li> Or go to deckserver.net".toCharArray();
    __oracle_jsp_text[7] = 
    "/{game name} to go to a game. \n</ul>\n\n<p>\nThis site is being supplied in 'alpha' condition.  There are lots of likely bugs, and many\npages and functionality is only half done.  Also, I'm not a html wizard, so I'm actively\nsoliciting any help in designing these pages so they work better.  Take a look at\nthe html source, you can see its structured very simply, and thats a good thing because\nits all generated, but if there is a better form it can take please pitch in code!.  Please\nlook at the todo list below and see if you can contribute expertise.\n\n<p>\nKnown bugs:\n<ol>\n<li> Exception thrown if cards drawn on empty deck.\n<li> Pages don't cope very well with server bounce.\n<li> Sessions expire too quickly, need better session storage\n<li> Sessions authenticate according to originating browser\n</ol>\n\n<p>\nHere is the list of small tweaks I'm working on:\n<ol>\n<li> More compact game history (1 item for transfer, for example)\n<li> burn a card should put all the contained cards (blood dolls, etc) in the ashheap too\n<li> More descriptive text on all the pages\n<li> List of all games from this page.\n<li> help/tutorials\n<li> Sort by type in deck presentations.\n<li> Re-edit decks.\n<li> Better 5-meth table display\n<li> Vampire capacity display\n<li> Initial library/crypt sizes displayed in state.\n<li> Server-level logging.\n<li> better table sizing\n</ol>\n\n<p> \nAnd some bigger tweaks that are necessary to clear alpha:\n<ol>\n<li> more active controls on the game page - do away with the command text area.\n</ol>\n\n<p> \nAnd some even bigger features that are really necessary before 1.0\n<ol>\n<li> Undo\n<li> Non-game pages combined into single \"portal\" page with login, decks, news, games all integrated\n</ol>\n\n<p> \nAnd some nice-to-haves\n<ol>\n<li> Pool calculations\n<li> vp/votes for each player shown in the player's region\n<li> hide/showall buttons\n</ol>\n</body>\n</html>\n".toCharArray();
    }
    catch (java.lang.Throwable th) {
      java.lang.System.err.println(th);
    }
  }
}
