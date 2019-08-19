<%@page import="net.deckserver.dwr.jsp.HandParams" %>
<% HandParams p = (HandParams) request.getAttribute("hparams"); %>
<ol class="card-list">
    <% for (int i = 0; i < p.getSize(); i++) {
        request.setAttribute("cparams", p.getCardParam(i));
        request.setAttribute("region", "hand");
        request.setAttribute("coordinates", String.valueOf(i + 1));
    %>
    <li>
        <jsp:include page="card.jsp"/>
    </li>
    <% }
        request.removeAttribute("region");
        request.removeAttribute("coordinates"); %>
</ol>
