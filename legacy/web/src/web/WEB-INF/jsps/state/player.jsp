<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckserver.util.*"%>
<%@page import="nbclient.vtesmodel.JolGame" %>
<% WebParams params = (WebParams) session.getAttribute("wparams");
   if(params == null) return;
   JolGame game = (JolGame) request.getAttribute("game");
   String player = (String) request.getAttribute("pparam");
   boolean active = player.equals(params.getPlayer());
   boolean edge = player.equals(game.getEdge());
   String[] players = game.getPlayers();
   int index = -1;
   for(int i = 0; i < players.length; i++)
       if(players[i].equals(player)) index = i + 1;
   %>
<b><% out.write(player + (edge ? "<font color=yellow>(EDGE)</font>" : "")); %></b>
(Pool: <% out.write(String.valueOf(game.getPool(player))); %>)
<BR>
<table><tr><td align=left>
<FONT COLOR=olive>Library:<% out.write(String.valueOf(game.getState().getPlayerLocation(player,JolGame.LIBRARY).getCards().length)); %><BR>
</td><td align=center><FONT COLOR=teal>Crypt:<% out.write(String.valueOf(game.getState().getPlayerLocation(player,JolGame.CRYPT).getCards().length)); %><BR>
</td><td align=right><FONT COLOR=fuschia>Hand:<% out.write(String.valueOf(game.getState().getPlayerLocation(player,JolGame.HAND).getCards().length)); %><BR>
</td></tr></table>
<HR>
<% request.setAttribute("rparams",new RegionParams(game,player,index,"lime","READY",JolGame.READY_REGION,false)); %>
<jsp:include page="region.jsp"/>
<hr/>
<% request.setAttribute("rparams",new RegionParams(game,player,index,"aqua","TORPOR",JolGame.TORPOR,false)); %>
<jsp:include page="region.jsp"/>
<hr/>
<% request.setAttribute("rparams",new RegionParams(game,player,index,"blue","INACTIVE",JolGame.INACTIVE_REGION,!active)); %>
<jsp:include page="region.jsp"/>
<hr/>
<% request.setAttribute("rparams",new RegionParams(game,player,index,"silver","ASHHEAP",JolGame.ASHHEAP,false)); %>
<jsp:include page="region.jsp"/>
