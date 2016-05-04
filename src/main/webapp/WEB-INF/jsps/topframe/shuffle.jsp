<%@page import="cards.model.CardEntry" %>
<%@page import="java.util.Map" %>
<%
    Map map = (Map) request.getAttribute("sparams");
    CardEntry[] c = (CardEntry[]) map.get("crypt");
    CardEntry[] l = (CardEntry[]) map.get("library");
%>
<b>Crypt:</b><br/>
<% for (int i = 0; i < c.length; i++) {
    CardEntry card = c[i]; %>
<A HREF="javascript:getCardDeck(null,'<% out.write(card.getCardId()); %>');">
    <% out.write(card.getName()); %>
</a>
<br/>
<% } %>
<b>Library:</b><br/>
<% for (int i = 0; i < l.length; i++) {
    CardEntry card = l[i]; %>
<A HREF="javascript:getCardDeck(null,'<% out.write(card.getCardId()); %>');">
    <% out.write(card.getName()); %>
</a>
<br/>
<% } %>
