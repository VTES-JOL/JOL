<%@page import="deckserver.util.HandParams" %>
<% HandParams p = (HandParams) request.getAttribute("hparams"); %>
<h5 class="game-header label-basic"><%= p.getText() %> ( <%= p.getSize() %>)</h5>
<ol class="card-list">
    <% for (int i = 0; i < p.getSize(); i++) {
        request.setAttribute("cparams", p.getCardParam(i)); %>
    <li>
        <jsp:include page="card.jsp"/>
    </li>
    <% } %>
</ol>
