<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.client.JolGame" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
%>
<table class="player-state">
    <tr>
        <% String[] players = game.getPlayers();
            for (String player : players) {
                request.setAttribute("pparam", player);
        %>
        <td valign="top" class="player <%= game.getActivePlayer().equals(player) ? "player-active" : "" %>">
            <jsp:include page="player.jsp"/>
        </td>
        <%
            }
        %>
    </tr>
</table>