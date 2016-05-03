<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckserver.util.*" %>
<%@page import="nbclient.vtesmodel.JolGame" %>
<%@page import="nbclient.vtesmodel.JolAdminFactory" %>
<%@page import="nbclient.model.GameAction" %>
<% WebParams params = (WebParams) session.getAttribute("wparams");
   JolGame game = (JolGame) request.getAttribute("game");
   %>
<table><tr><td align=left>
<font color=yellow><% out.write(game.getName()); %></font><font color=white>Current Turn: <% out.write(game.getCurrentTurn() + " " + game.getPhase()); %>.
</td><td align=right>
Old turns:
<select id="oldturns" name="oldturns">
<% String[] turns = game.getTurns();
   for(int i = turns.length - 1; i >= 0; i--) { 
      String link = JolAdminFactory.INSTANCE.getGameId(game.getName()) + "-" + turns[i]; %>
<option value="<% out.write(link);%>"><%    out.write(turns[i]); %></option>
<%    } %>
</select>
<A HREF="javascript:openTurnWin();">View old turn</A><BR>
</td></tr><tr><td colspan=2>
<SELECT NAME="commands" SIZE=10 MULTIPLE>
<option>---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------</option>
<% GameAction[] actions = game.getActions(game.getCurrentTurn());
   for(int i = 0; i < actions.length; i++) { %>
<OPTION <% if(i == actions.length - 1) out.print("SELECTED"); %>>
<% if(actions[i].isCommand()) out.print("<b>");
   out.print(actions[i].getText()); 
   if(actions[i].isCommand()) out.print("</b>"); %>
</OPTION>
<% } %>
</SELECT>
</td></tr></table>