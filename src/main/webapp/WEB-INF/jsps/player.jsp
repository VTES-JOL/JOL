<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="net.deckserver.Utils" %>
<%@page import="net.deckserver.dwr.jsp.RegionParams" %>
<%@page import="net.deckserver.dwr.model.JolGame" %>

<% String curPlayer = Utils.getPlayer(request);
    JolGame game = (JolGame) request.getAttribute("game");
    String player = (String) request.getAttribute("pparam");
    boolean active = player.equals(curPlayer);
    boolean edge = player.equals(game.getEdge());
    String[] players = game.getPlayers();
    int index = -1;
    for (int i = 0; i < players.length; i++)
        if (players[i].equals(player)) index = i + 1;
    String poolStyle = game.getPool(player) == 0 ? "pool-ousted" : "pool";
    poolStyle = game.getPool(player) < 0 ? "pool-sacked" : poolStyle;

    request.setAttribute("edge", edge);
%>
<div class="game-header">
    <h5><%= player %></h5>
    <c:if test="${edge}">
        <span class="label label-basic edge">Edge</span>
    </c:if>
    <span class="label label-basic <%= poolStyle %>">Pool: <%= game.getPool(player) %></span>
</div>
<div class="padded">
    <small>
        Crypt: <%= game.getState().getPlayerLocation(player, JolGame.CRYPT).getCards().length %> -
        Library: <%= game.getState().getPlayerLocation(player, JolGame.LIBRARY).getCards().length %> -
        Hand: <%= game.getState().getPlayerLocation(player, JolGame.HAND).getCards().length %>
    </small>
    <% request.setAttribute("rparams", new RegionParams(game, player, index, "READY", JolGame.READY_REGION, "r", false)); %>
    <jsp:include page="region.jsp"/>

    <% request.setAttribute("rparams", new RegionParams(game, player, index, "TORPOR", JolGame.TORPOR, "t", false)); %>
    <jsp:include page="region.jsp"/>

    <% request.setAttribute("rparams", new RegionParams(game, player, index, "INACTIVE", JolGame.INACTIVE_REGION, "i", !active)); %>
    <jsp:include page="region.jsp"/>

    <% request.setAttribute("rparams", new RegionParams(game, player, index, "ASHHEAP", JolGame.ASHHEAP, "a", false)); %>
    <jsp:include page="region.jsp"/>

    <% request.setAttribute("rparams", new RegionParams(game, player, index, "RFG", JolGame.RFG, "rfg", false)); %>
    <jsp:include page="region.jsp"/>

    <% request.setAttribute("rparams", new RegionParams(game, player, index, "RESEARCH", JolGame.RESEARCH, "res", !active)); %>
    <jsp:include page="region.jsp"/>
</div>