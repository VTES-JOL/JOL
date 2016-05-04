<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<%@page import="deckserver.util.RegionParams" %>
<%@page import="deckserver.util.WebParams" %>
<%@ page import="nbclient.vtesmodel.JolGame" %>
<% WebParams params = (WebParams) session.getAttribute("wparams");
    JolGame game = (JolGame) request.getAttribute("game");
    RegionParams r = (RegionParams) request.getAttribute("rparams");
%>

<a href="javascript:details('<% out.write(r.getIndex());%>');" id="<% out.write(r.getIndex());%>">-</a>
<span><FONT
        COLOR=<% out.write(r.getColor()); %>> <% out.write(r.getText()); %> (<% out.write(String.valueOf(r.getSize())); %>)</span>
<span id="region<% out.write(r.getIndex());%>">
    <ol>
         <% for (int i = 0; i < r.getSize(); i++) {
             request.setAttribute("cparams", r.getCardParam(i));
         %>
        <LI><jsp:include page="card.jsp"/></LI>
         <% } %>
    </ol>
</span>
 