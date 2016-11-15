<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.client.JolGame" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
%>
<table border="2">
    <% String[] players = game.getPlayers();
        for (String player : players) {
            request.setAttribute("pparam", player);
    %>
    <td valign="top" width="20%">
        <jsp:include page="player.jsp"/>
    </td>
    <%
        }
    %>
</table>