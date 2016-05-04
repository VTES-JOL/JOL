<%@page import="deckserver.util.WebParams" %>
<%
    String prefix = application.getInitParameter("prefix");
    WebParams params = (WebParams) session.getAttribute("params");
    String error = (params != null) ? params.getStatusMsg() : null;
    if (error != null && error.length() > 0) out.write("STATUS : " + error); %>
<p>
    Welcome to JOL-3, the lastest version of Jyhad-OnLine. This version is fully interactive,
    as all commands are available through the web site, making it possible to play real-time games
    across the network with only a browser for a client. The new JOL
    command set is streamlined and hopefully
    more useful.
<p>
    To play games on this server, register and login using the links below, then construct some decks.
    You can cut/paste decks into the deck construction window from an external tool or editor if you like.
    Cards go on separate lines, you can prefix with 1x or 2x or 10x if you want multiples of a card. Prefixes
    of card names are accepted, as well as some common nicknames like WWEF. Once you've got a deck, send
    email to register@deckserver.net and I'll put you in a game. If you've rounded up a group to play
    a game together, let me know and I'll put you all in the same game. Because JOL3 can be truly interactive,
    games can go fast if everybody is on-line at the same time.
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
</ol>

<p>
    Here is the list of small tweaks I'm working on:
<ol>
    <li> More compact game history (1 item for transfer, for example)
    <li> burn a card should put all the contained cards (blood dolls, etc) in the ashheap too
    <li> More descriptive text on all the pages
    <li> List of all games from this page.
    <li> help/tutorials
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
