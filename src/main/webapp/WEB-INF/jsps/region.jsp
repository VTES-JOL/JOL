<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="net.deckserver.dwr.jsp.RegionParams" %>
<%
    RegionParams r = (RegionParams) request.getAttribute("rparams");
    boolean empty = r.isEmpty();
    String region = r.getRegion();
%>
<div class="region-empty-<%= empty %> <%= region %>" data-region="<%= region %>">
    <h5 class="region-header">
        <a href="javascript:details('<%= r.getIndex() %>');" id="<%= r.getIndex() %>">
            <i class="toggle"></i>
            <%= r.getText() %> ( <%= r.getSize() %> )
        </a>
    </h5>

    <ol class="card-list" id="region<%= r.getIndex() %>">
        <% for (int i = 0; i < r.getSize(); i++) {
            request.setAttribute("cparams", r.getCardParam(i));
            request.setAttribute("coordinates", String.valueOf(i + 1));
            request.setAttribute("region", region);
        %>
        <li>
            <jsp:include page="card.jsp"/>
        </li>
        <% }
            request.removeAttribute("coordinates");
            request.removeAttribute("region"); %>
    </ol>
</div>
