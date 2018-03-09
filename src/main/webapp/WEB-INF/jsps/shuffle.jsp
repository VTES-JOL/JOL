<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="net.deckserver.game.storage.cards.CardEntry" %>
<%@page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%
    Map map = (Map) request.getAttribute("sparams");
    List<CardEntry> c = (List<CardEntry>) map.get("crypt");
    List<CardEntry> l = (List<CardEntry>) map.get("library");
    request.setAttribute("c", c);
    request.setAttribute("l", l);
%>
<b>Crypt:</b><br/>
<c:forEach items="${c}" var="card">
    <a class="card-name" title="${card.cardId}">${card.name}</a>
    <br/>
</c:forEach>
<b>Library:</b><br/>
<c:forEach items="${l}" var="card">
    <a class="card-name" title="${card.cardId}">${card.name}</a>
    <br/>
</c:forEach>