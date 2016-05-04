<%@page contentType="text/html" %>
<%@page import="deckserver.util.WebParams" %>
<%
    String prefix = application.getInitParameter("prefix");
    WebParams params = (WebParams) session.getAttribute("wparams");
    String error = (params != null) ? params.getStatusMsg() : null;
%>

<html>
<head><title>JOL 3</title></head>
<body>

<p>
    Try the <a href="/jol3/beta">beta</a> site, its much improved, though both methods of playing games work
    interchangably for now.

<p>
    Welcome to JOL-3, the lastest version of Jyhad-OnLine. This version is fully interactive,
    as all commands are available through the web site, making it possible to play real-time games
    across the network with only a browser for a client. The new JOL
    command <a href="/commands.html">set</a> is streamlined and hopefully
    more useful.
<p>
    The first tournament of 2005 is ongoing, see the tourney <a
        href="http://www.mnsi.net/~ghost/jyhad/jt2005.htm">page</a>
    for links to games and status.
<p>
    To play games on this server, register and login using the links below, then construct some decks.
    You can cut/paste decks into the deck construction window from an external tool or editor if you like.
    Cards go on separate lines, you can prefix with 1x or 2x or 10x if you want multiples of a card. Prefixes
    of card names are accepted, as well as some common nicknames like WWEF. Once you've got a deck, send
    email to register@deckserver.net and I'll put you in a game. If you've rounded up a group to play
    a game together, let me know and I'll put you all in the same game. Because JOL3 can be truly interactive,
    games can go fast if everybody is on-line at the same time.
<p>

<p>
<ul>
    <li><a href="<%out.write(prefix);%>register">Register</a> if you haven't registered to this site left.
    <li><a href="<%out.write(prefix);%>login">Log in</a> if you've already registered. Login information is
        preserved throughout your browser session. One login is used for all your games. You
        have to be logged on to use any of the subsequent pages.
    <li> Player <a href="<%out.write(prefix);%>player">home page</a>. This page links to all your games and decks.
    <li><a href="<%out.write(prefix);%>deck">Deck construction</a>. Any deck used in a JOL-3 game must be first
        registered through this page.
    <li> Or go to deckserver.net<%out.write(prefix);%>/{game name} to go to a game.
</ul>

<p>
    This site is being supplied in 'alpha' condition. There are lots of likely bugs, and many
    pages and functionality is only half done. Also, I'm not a html wizard, so I'm actively
    soliciting any help in designing these pages so they work better. Take a look at
    the html source, you can see its structured very simply, and thats a good thing because
    its all generated, but if there is a better form it can take please pitch in code!. Please
    look at the todo list below and see if you can contribute expertise.

<p>
    Known bugs:
<ol>
    <li> Exception thrown if cards drawn on empty deck.
    <li> Pages don't cope very well with server bounce.
    <li> Sessions expire too quickly, need better session storage
    <li> Sessions authenticate according to originating browser
</ol>

<p>
    Here is the list of small tweaks I'm working on:
<ol>
    <li> More compact game history (1 item for transfer, for example)
    <li> burn a card should put all the contained cards (blood dolls, etc) in the ashheap too
    <li> More descriptive text on all the pages
    <li> List of all games from this page.
    <li> help/tutorials
    <li> Sort by type in deck presentations.
    <li> Re-edit decks.
    <li> Better 5-meth table display
    <li> Vampire capacity display
    <li> Initial library/crypt sizes displayed in state.
    <li> Server-level logging.
    <li> better table sizing
</ol>

<p>
    And some bigger tweaks that are necessary to clear alpha:
<ol>
    <li> more active controls on the game page - do away with the command text area.
</ol>

<p>
    And some even bigger features that are really necessary before 1.0
<ol>
    <li> Undo
    <li> Non-game pages combined into single "portal" page with login, decks, news, games all integrated
</ol>

<p>
    And some nice-to-haves
<ol>
    <li> Pool calculations
    <li> vp/votes for each player shown in the player's region
    <li> hide/showall buttons
</ol>
</body>
</html>
