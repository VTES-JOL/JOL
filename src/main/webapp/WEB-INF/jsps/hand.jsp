<%@page import="deckserver.util.HandParams" %>
<% HandParams p = (HandParams) request.getAttribute("hparams"); %>
<div class="game-header">
    <h5><%= p.getText() %> ( <%= p.getSize() %>)</h5>
</div>
<ol class="card-list">
    <% for (int i = 0; i < p.getSize(); i++) {
        request.setAttribute("cparams", p.getCardParam(i)); %>
    <li>
        <jsp:include page="card.jsp"/>
    </li>
    <% } %>
</ol>