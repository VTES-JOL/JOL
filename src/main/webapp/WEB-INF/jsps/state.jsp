<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.client.JolGame" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
%>
<table class="game-state">
    <tr>
        <% String[] players = game.getPlayers();
            for (String player : players) {
                request.setAttribute("pparam", player);
        %>
        <td valign="top" class="game-row <%= game.getActivePlayer().equals(player) ? "player-active" : "" %>">
            <jsp:include page="player.jsp"/>
        </td>
        <%
            }
        %>
    </tr>
</table>