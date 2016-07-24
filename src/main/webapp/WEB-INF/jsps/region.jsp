<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.client.JolGame" %>
<%@page import="deckserver.util.RegionParams" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
    RegionParams r = (RegionParams) request.getAttribute("rparams");
%>

<a href="javascript:details('<%= r.getIndex() %>');" id="<%= r.getIndex() %>">-</a>
<span>
    <%= r.getText() %>
    <%= r.getSize() %>
</span>

<span id="region<%= r.getIndex() %>">
    <ol>
         <% for (int i = 0; i < r.getSize(); i++) {
             request.setAttribute("cparams", r.getCardParam(i));
         %>
        <li>
            <jsp:include page="card.jsp"/>
        </li>
         <% } %>
    </ol>
</span>
 