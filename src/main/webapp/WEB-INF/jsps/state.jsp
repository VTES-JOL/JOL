<%@page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="java.util.List" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
%>

<% List<String> players = game.getPlayers();
    for (String player : players) {
        request.setAttribute("pparam", player);
%>
<div class="col-sm player <%= game.getActivePlayer().equals(player) ? "player-active" : "" %>" data-player="<%= player %>" data-pool="<%= game.getPool(player) %>">
    <jsp:include page="player.jsp"/>
</div>
<%
    }
%>
