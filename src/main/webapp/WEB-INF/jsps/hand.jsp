<%@page import="deckserver.util.HandParams" %>
<% HandParams p = (HandParams) request.getAttribute("hparams"); %>
<div>
    <%= p.getText() %> ( <%= p.getSize() %>)
    <ol>
        <% for (int i = 0; i < p.getSize(); i++) {
            request.setAttribute("cparams", p.getCardParam(i)); %>
        <li>
            <jsp:include page="card.jsp"/>
        </li>
        <% } %>
    </ol>
</div>