<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="nbclient.vtesmodel.JolGame" %>
<% JolGame game = (JolGame) request.getAttribute("game"); %>
<table border=2>
    <% String[] players = game.getPlayers();
        for (int i = 0; i < players.length; i++) {
            request.setAttribute("pparam", players[i]); %>
    <TD VALIGN=top>
        <jsp:include page="player.jsp"/>
    </TD>
    <% } %>
</table>