<%@page contentType="text/html" %>
<%@page import="deckserver.util.WebParams" %>
<%
    WebParams params = (WebParams) session.getAttribute("wparams");
%>

<html>
    <head><title>JOL 3</title></head>
    <body>

        <p>
            Try the <a href="${pageContext.request.contextPath}/beta">beta</a> site, its much improved, though both
            methods of playing games work interchangeably for now.
        </p>

        <p>
            Welcome to JOL-3, the lastest version of Jyhad-OnLine. This version is fully interactive, as all commands
            are available through the web site, making it possible to play real-time games across the network with only
            a browser for a client. The new JOL command <a
                href="${pageContext.request.contextPath}/commands.html">set</a> is streamlined and hopefully more
            useful.
        </p>
        <p>
            To play games on this server, register and login using the links below, then construct some decks.
            You can cut/paste decks into the deck construction window from an external tool or editor if you like.
            Cards go on separate lines, you can prefix with 1x or 2x or 10x if you want multiples of a card. Prefixes
            of card names are accepted, as well as some common nicknames like WWEF. Once you've got a deck, send
            email to register@deckserver.net and I'll put you in a game. If you've rounded up a group to play
            a game together, let me know and I'll put you all in the same game. Because JOL3 can be truly interactive,
            games can go fast if everybody is on-line at the same time.
        </p>
        <ul>
            <li><a href="${pageContext.request.contextPath}/register">Register</a> if you haven't registered to this
                site left.
            </li>
            <li><a href="${pageContext.request.contextPath}/login">Log in</a> if you've already registered. Login
                information is
                preserved throughout your browser session. One login is used for all your games. You
                have to be logged on to use any of the subsequent pages.
            </li>
            <li> Player <a href="${pageContext.request.contextPath}/player">home page</a>. This page links to all your
                games and
                decks.
            </li>
            <li><a href="${pageContext.request.contextPath}/deck">Deck construction</a>. Any deck used in a JOL-3 game
                must be first
                registered through this page.
            </li>
            <li> Or go to deckserver.net<${pageContext.request.contextPath}>/{game name} to go to a game.</li>
        </ul>
    </body>
</html>
