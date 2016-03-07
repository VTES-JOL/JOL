<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckserver.util.*"%>
<%@page import="nbclient.vtesmodel.JolGame"%>
<% WebParams params = (WebParams) request.getSession().getAttribute("wparams");
   if(params == null) { params = new WebParams(request);
   String game = request.getParameter("game");
   params.setGame(game);
   String player = request.getParameter("player"); // PENDING from session
   if(player == null) player = "";
   params.setPlayer(player);
   }
   String game = params.getGame();
   JolGame jgame = AdminFactory.get(application).getGame(game);
   request.setAttribute("game",jgame);
   request.setAttribute("params",params);
   %>
<html>
<head>
  <jsp:forward page="../state/game.jsp"/>
</html>
