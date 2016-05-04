<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckserver.util.*" %>
<%@page import="nbclient.vtesmodel.JolGame" %>
<% JolGame game = (JolGame) request.getAttribute("game");
   %>
  <TITLE><% out.write(game.getName()); %> Final</TITLE>
  <META NAME="decription" CONTENT="JOL3 Version 0.2">
  <META NAME="keywords" CONTENT="<% out.write(game.getName()); %>, JOL">
  <META NAME="robots" CONTENT="noindex, nofollow">
  <META NAME="rating" CONTENT="general">
  <META NAME="generator" CONTENT="vi">
<jsp:include page="styles.jsp"/>
 </HEAD>
 <jsp:include page="../../javascript/game.jsp"/>
 <BODY BGCOLOR="black" 
       TEXT="red"
       LINK="yellow"
       VLINK="yellow"
       ALINK="yellow"
       onLoad="collapse();">
  <TABLE border=2>
<% if(showPlayer) { %>
   <TR>
<%   if(isPlayer) { %>
    <TD valign=top WIDTH="30%">
       <%  String player = params.getPlayer();
           request.setAttribute("hparams",new HandParams(game,player,"fuschia","Cards in hand",JolGame.HAND)); %>
       <jsp:include page="hand.jsp"/>
    </td>
    <% } %>
    <td valign=top>
       <jsp:include page="command.jsp"/>
    </td>
   </TR>
<% } %>
   <tr>
    <TD colspan='2'>
      <jsp:include page="messages.jsp"/>
    </TD>
   </TR>
   <TR>
    <TD COLSPAN='2'>
      <jsp:include page="state.jsp"/>
    </TD>
   </TR>
  </TABLE>
 </BODY>

    

