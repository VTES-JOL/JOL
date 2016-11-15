<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.client.JolGame" %>
<%@page import="deckserver.dwr.Utils" %>
<%@page import="deckserver.util.RegionParams" %>

<% String curPlayer = Utils.getPlayer(request);
    JolGame game = (JolGame) request.getAttribute("game");
    String player = (String) request.getAttribute("pparam");
    boolean active = player.equals(curPlayer);
    boolean edge = player.equals(game.getEdge());
    String[] players = game.getPlayers();
    int index = -1;
    for (int i = 0; i < players.length; i++)
        if (players[i].equals(player)) index = i + 1;
%>
<b><%= player + (edge ? "<span class='edge'>(EDGE)</span>" : "") %>
</b>
(Pool: <%= game.getPool(player) %>)
<br/>
<table>
    <tr>
        <td align="left">
            <span class="library">
                Library:<%= game.getState().getPlayerLocation(player, JolGame.LIBRARY).getCards().length %>
            </span>
        </td>
        <td align="center">
            <span class="crypt">
                Crypt:<%= game.getState().getPlayerLocation(player, JolGame.CRYPT).getCards().length %>
            </span>
        </td>
        <td align="right">
            <span class="hand">
                Hand:<%= game.getState().getPlayerLocation(player, JolGame.HAND).getCards().length %>
            </span>
        </td>
    </tr>
</table>
<hr/>
<% request.setAttribute("rparams", new RegionParams(game, player, index, "READY", JolGame.READY_REGION, false)); %>
<jsp:include page="region.jsp"/>
<hr/>
<% request.setAttribute("rparams", new RegionParams(game, player, index, "TORPOR", JolGame.TORPOR, false)); %>
<jsp:include page="region.jsp"/>
<hr/>
<% request.setAttribute("rparams", new RegionParams(game, player, index, "INACTIVE", JolGame.INACTIVE_REGION, !active)); %>
<jsp:include page="region.jsp"/>
<hr/>
<% request.setAttribute("rparams", new RegionParams(game, player, index, "ASHHEAP", JolGame.ASHHEAP, false)); %>
<jsp:include page="region.jsp"/>
