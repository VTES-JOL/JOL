<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="deckserver.client.JolGame" %>
<%@page import="deckserver.game.cards.CardEntry" %>
<%@page import="deckserver.game.state.Card" %>
<%@ page import="deckserver.util.CardParams" %>
<%
    CardParams p = (CardParams) request.getAttribute("cparams");
    JolGame game = (JolGame) request.getAttribute("game");
    Card c = p.getCard();
    int capacity = game.getCapacity(c.getId());
    int counters = game.getCounters(c.getId());
    boolean locked = game.isTapped(c.getId());
    String label = game.getText(c.getId());
    boolean nested = p.doNesting();
    Card[] cards = c.getCards();
    boolean hasCards = cards != null && cards.length > 0;
    CardEntry cardEntry = p.getEntry();
    boolean isCrypt = cardEntry.isCrypt();
    boolean hasLife = cardEntry.hasLife();

    request.setAttribute("p", p);
    request.setAttribute("c", c);
    request.setAttribute("game", game);
    request.setAttribute("capacity", capacity);
    request.setAttribute("counters", counters);
    request.setAttribute("locked", locked);
    request.setAttribute("label", label);
    request.setAttribute("nested", nested);
    request.setAttribute("cards", cards);
    request.setAttribute("hasCards", hasCards);
    request.setAttribute("isCrypt", isCrypt);
    request.setAttribute("hasLife", hasLife);
    request.setAttribute("cardEntry", cardEntry);
%>

<c:if test="${p.hidden}">
    XXXXXX
</c:if>
<c:if test="${!p.hidden}">
    <a class="card-name" onclick="getCard(event, '<%= p.getId() %>');"><%= p.getName() %>
    </a>
</c:if>
<c:if test="${game != null}">
    <c:if test="${capacity > 0 && isCrypt}">
        <small class="counter blood"><%= counters %> / <%= capacity %>
        </small>
    </c:if>
    <c:if test="${counters > 0 && hasLife}">
        <small class="counter life"><%= counters %>
        </small>
    </c:if>
    <c:if test="${counters > 0 && !isCrypt && !hasLife}">
        <small class="counter"><%= counters %>
        </small>
    </c:if>
    <c:if test="${locked}">
        <small class="label label-dark">LOCKED</small>
    </c:if>
    <c:if test="${label.length() > 0}">
        <small class="label label-light"><%= label %>
        </small>
    </c:if>
    <c:if test="${nested && hasCards}">
        <ol>
            <c:forEach items="${cards}" var="child">
                <%
                    Card child = (Card) pageContext.findAttribute("child");
                    request.setAttribute("cparams", new CardParams(child));
                %>
                <li>
                    <jsp:include page="card.jsp"/>
                </li>
            </c:forEach>
        </ol>
    </c:if>
</c:if>
<c:if test="${game == null && isCrypt}">
    ( <%= cardEntry.getGroup() %>)
</c:if>