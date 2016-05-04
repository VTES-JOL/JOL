<%@page import="deckserver.portal.PortalParams" %>
<%@page import="nbclient.vtesmodel.JolAdminFactory" %>
<% PortalParams params = (PortalParams) request.getAttribute("params");
    JolAdminFactory admin = JolAdminFactory.INSTANCE; %>
Decks:<br/>
<% String[] decks = admin.getDeckNames(player);
    if (decks == null || decks.length == 0) { %>
no decks.<br/>
<% } else { %>
<ul>
    <% for (int i = 0; i < decks.length; i++) { %>
    <li><a href="<% out.write(params.getPrefix()); %>?page=show&deck=<% out.write(decks[i]); %>"><%
        out.write(decks[i]); %></a></li>
    <% }
    } %>
</ul>
<% } %>
Games:<br/>
<% String
    [
    ]
    games
    =
    admin
    .
    getGames
    (
    player
    )
    ;
    boolean
    invites
    =
    false
    ;
    if
    (
    games
    ==
    null
    ||
    games
    .
    length
    ==
    0
    )
    { %>
no games.<br/>
<% }
    else
    { %>
<ul>
    <% for
        (
        int
        i
        =
        0
        ;
        i
        <
        games
        .
        length
        ;
        i
        ++
        )
        {
        if
        (
        admin
        .
        isOpen
        (
        games
        [
        i
        ]
        )
        )
        invites
        =
        true
        ;
        else
        { %>
    <li><a href="<% out.write(params.getPrefix() + games[i]); %>"><% out
        .
        write
        (
        games
        [
        i
        ]
        )
        ; %></a></li>
    <% }
        } %>
</ul>
<% }
    if
    (
    invites
    )
    { %>
<form><input type=button name="Open games"/><input type=hidden name="page" value="user"/></form>
<% } %>