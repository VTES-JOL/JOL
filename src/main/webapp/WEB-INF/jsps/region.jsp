<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.client.JolGame" %>
<%@page import="deckserver.util.RegionParams" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
    RegionParams r = (RegionParams) request.getAttribute("rparams");
%>
<h5 class="region-header">
    <a href="javascript:details('<%= r.getIndex() %>');" id="<%= r.getIndex() %>">
        <i class="toggle"></i>
        <%= r.getText() %> ( <%= r.getSize() %> )
    </a>
</h5>

<ol class="card-list" id="region<%= r.getIndex() %>">
    <% for (int i = 0; i < r.getSize(); i++) {
        request.setAttribute("cparams", r.getCardParam(i));
    %>
    <li>
        <jsp:include page="card.jsp"/>
    </li>
    <% } %>
</ol>
