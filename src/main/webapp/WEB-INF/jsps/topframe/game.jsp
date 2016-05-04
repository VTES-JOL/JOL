<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.util.WebParams" %>
<%@page import="nbclient.vtesmodel.JolAdminFactory" %>
<%@ page import="nbclient.vtesmodel.JolGame" %>
<% WebParams p = (WebParams) session.getAttribute("wparams");
    if (p == null) return;
    String prefix = p.getPrefix();
    String name = p.getGame();
    String player = p.getPlayer();
    JolGame game = JolAdminFactory.INSTANCE.getGame(name);
    int counter = game.getGameCounter();
    if (player == null) player = "";
%>
<html>
<head>
    <title> JOL - <% out.write(name); %>
    </title>
</head>
<frameset border="0" cols="100%,*">
    <frame name="stateframe<% out.write(name); %>"
           src="<% out.write(prefix); %>state.jsp?game=<% out.write(name);%>&player=<% out.write(player);%>"/>
    <frame name="pollframe" src="<% out.write(prefix); %>poll.jsp?game=<% out.write(name); %>"/>
</frameset>

</html>
