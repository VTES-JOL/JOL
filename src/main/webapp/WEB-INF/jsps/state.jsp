<%@page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="java.util.List" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
%>
<table class="player-state">
    <tr>
        <% List<String> players = game.getPlayers();
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