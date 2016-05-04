<%@page import="deckserver.portal.PortalParams" %>
<%@page import="nbclient.vtesmodel.JolAdminFactory" %>
<% PortalParams params = (PortalParams) request.getAttribute("params");
    JolAdminFactory admin = JolAdminFactory.INSTANCE; %>
Decks:<br/>
<% String[] decks = admin.getDeckNames(player);
    if (decks == null) decks = new String[0];
    if (decks.length == 0) { %>
no decks.<br/>
<% } else { %>
<ul>
    <% for (int i = 0; i < decks.length; i++) { %>
    <li><a href="<% out.write(params.getPrefix()); %>?page=deck&deck='<% out.write(decks[i]); %>'"><%
        out.write(decks[i]); %></a>
        Delete:<a
                href="<% out.write(params.getPrefix()); %>?page=user&delete=yes&deck='<% out.write(decks[i]); %>'">yes</a>
    </li>
    <% }
    } %>
</ul>
<% } %>
Open Games:<br/>
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
    if
    (
    games
    ==
    null
    )
    games
    =
    new
    String
    [
    0
    ]
    ;
    Collection
    v
    =
    new
    ArrayList
    (
    )
    ;
    for
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
    {
    v
    .
    add
    (
    games
    [
    i
    ]
    )
    ;
    }
    }
    if
    (
    v
    .
    length
    ==
    null
    )
    { %>
none
<% }
    else
    { %>
Register a deck for a game:<br/>
<form method=post>
    <select name=reggame>
        <% for
            (
            Iterator
            it
            =
            v
            .
            iterator
            (
            )
            ;
            it
            .
            hasNext
            (
            )
            ;
            )
            {
            String
            game
            =
            (
            String
            )
            it
            .
            next
            (
            )
            ; %>
        <option value="<% out.write(game); %>"><% out
            .
            write
            (
            game
            )
            ; %></option>
        <% } %>
    </select>
    <select name=regdeck>
        <% for
            (
            int
            j
            =
            0
            ;
            j
            <
            decks
            .
            length
            ;
            j
            ++
            )
            { %>
        <option value="<% out.write(decks[i]); %>"><% out
            .
            write
            (
            decks
            [
            i
            ]
            )
            ; %></option>
        <% } %>
    </select>
    <input type=submit value=Register/>
</form>
<% } %>