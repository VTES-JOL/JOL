<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="deckserver.dwr.Utils" %>
<%@page import="deckserver.game.cards.CardEntry" %>
<%@page import="deckserver.util.DeckParams" %>
<%@page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.TreeMap" %>
<%
    DeckParams p = (DeckParams) request.getAttribute("dparams");
    Map<String, TreeMap<CardEntry, Integer>> deck = Utils.getDeckHtmlMap(p);
    Map<CardEntry, Integer> vampires = deck.get("Vampire");
    Map<CardEntry, Integer> imbued = deck.get("Imbued");
    deck.remove("Vampire");
    deck.remove("Imbued");
    if (vampires == null) vampires = new HashMap<CardEntry, Integer>();
    if (imbued == null) imbued = new HashMap<CardEntry, Integer>();
    int csize = Utils.sumMap(vampires.values()) + Utils.sumMap(imbued.values());
    int sum = 0;
    for (TreeMap<CardEntry, Integer> libraryCard : deck.values()) {
        for (Integer count : libraryCard.values()) {
            sum += count;
        }
    }
    request.setAttribute("vampires", vampires);
    request.setAttribute("imbued", imbued);
    request.setAttribute("deck", deck);
%>
<h5>Crypt: (<%= csize %>)</h5>
<ul class='deck-list'>
    <c:forEach items="${vampires.keySet()}" var="card">
        <li>${vampires.get(card)} x <a onclick="getCardDeck(event, null,'${card.cardId}')">${card.name}
            (G${card.group})</a>
        </li>
    </c:forEach>
    <c:forEach items="${imbued.keySet()}" var="card">
        <li>${imbued.get(card)} x <a onclick="getCardDeck(event, null,'${card.cardId}')">${card.name}
            (G${card.group})</a></li>
    </c:forEach>
</ul>

<h5>Library: (<%= sum %>)</h5>
<c:forEach items="${deck.keySet()}" var="type">
    <%
        String type = (String) pageContext.findAttribute("type");
        Map typeMap = deck.get(type);
        int typeCount = Utils.sumMap(typeMap.values());
        pageContext.setAttribute("typeCount", typeCount);
        pageContext.setAttribute("typeMap", typeMap);
    %>
    <h5>${type}: (${typeCount})</h5>
    <ul class='deck-list'>
        <c:forEach items="${typeMap.keySet()}" var="card">
            <li>${typeMap.get(card)} x <a onclick="getCardDeck(event, null,'${card.cardId}')">${card.name}</a></li>
        </c:forEach>
    </ul>
</c:forEach>
