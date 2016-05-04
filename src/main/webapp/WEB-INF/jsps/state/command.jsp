<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.util.WebParams" %>
<%@page import="nbclient.vtesmodel.JolGame" %>
<% WebParams params = (WebParams) session.getAttribute("wparams");
    JolGame game = (JolGame) request.getAttribute("game");
    String player = params.getPlayer();
    String[] players = game.getPlayers();
    boolean active = player.equals(game.getActivePlayer());
%>
<form target="_top" action="<% out.write(params.getPrefix() + params.getGame()); %>" method=post>
    <table>
        <tr>
            <td rowspan=2>
                <table>
                    <tr>
                        <td>
                            <% if (active) { %>
                            Phase: <select name=phase>
                            <% boolean show = false;
                                String phase = game.getPhase();
                                for (int i = 0; i < game.TURN_PHASES.length; i++) {
                                    if (phase.equals(game.TURN_PHASES[i])) show = true;
                                    if (show) { %>
                            <option value="<% out.write(game.TURN_PHASES[i]); %>"><%
                                out.write(game.TURN_PHASES[i]);%></option>
                            <% }
                            } %>
                        </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <% } %>
                            JOL COMMAND: (<A HREF="javascript:openHelpWin()">docs</a>)
                            <input name=command size=25 maxlength=100>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            CHAT MESSAGE: <input name=message size=25 maxlength=120>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            PING:
                            <select name=ping>
                                <option value="" SELECTED></option>
                                <% for (int i = 0; i < players.length; i++) { %>
                                <option value="<% out.write(players[i]); %>"><%
                                    out.write(players[i] + "(" + game.getPingTag(players[i]) + ")");%></option>
                                <% } %>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <% if (active) { %>
                            End Turn? <select name="newturn">
                            <option value="no" SELECTED>No</option>
                            <option value="yes">Yes</option>
                        </select>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <% } %>
                            <input type="submit" value="Submit"/>
                            <% String res = params.getStatusMsg();
                                if (res != null) { %>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <% out.write("Status: " + res);
                            } %>
                            <%--  %></td></tr><tr><td>
                            <input type=checkbox id=dontpoll /> --%>
                        </td>
                    </tr>
                </table>
            </td>
            <td valign=top>
                Global notes and pending actions:<br>
                <textarea rows="4" cols="50" name="global"><% out.write(game.getGlobalText()); %></textarea>
            </td>
        </tr>
        <tr>
            <td valign=top>
                Private notepad:<br>
                <textarea rows="4" cols="50" name="notes"><% out.write(game.getPlayerText(player)); %></textarea>
            </td>
        </tr>
    </table>
</form>