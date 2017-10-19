<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="deckserver.game.cards.CardEntry" %>
<%@page import="java.util.Map" %>
<%
    Map map = (Map) request.getAttribute("sparams");
    CardEntry[] c = (CardEntry[]) map.get("crypt");
    CardEntry[] l = (CardEntry[]) map.get("library");
    request.setAttribute("c", c);
    request.setAttribute("l", l);
%>
<b>Crypt:</b><br/>
<c:forEach items="${c}" var="card">
    <a href="javascript:getCardDeck(null, '${card.cardId}');">${card.name}</a>
    <br/>
</c:forEach>
<b>Library:</b><br/>
<c:forEach items="${l}" var="card">
    <a href="javascript:getCardDeck(null, '${card.cardId}');">${card.name}</a>
    <br/>
</c:forEach>