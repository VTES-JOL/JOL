<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="deckserver.client.JolGame" %>
<%@page import="deckserver.game.cards.CardEntry" %>
<%@page import="deckserver.game.state.Card" %>
<%@ page import="deckserver.util.CardParams" %>
<%
    CardParams p = (CardParams) request.getAttribute("cparams");
    JolGame game = (JolGame) request.getAttribute("game");
    Card c = p.getCard();

    request.setAttribute("p", p);
    request.setAttribute("c", c);
%>

<c:if test="${p.hidden}">
    XXXXXX
</c:if>
<c:if test="${!p.hidden}">
    <a href="javascript:getCard('<%= p.getId() %>');"><%= p.getName() %>
    </a>
</c:if>
<%
    if (game != null) {
        StringBuilder builder = new StringBuilder();
        int counters = game.getCounters(c.getId());
        int capacity = game.getCapacity(c.getId());
        // Capacity / Blood
        if (counters > 0 || capacity > 0) {
            builder.append(", ").append((capacity > 0) ? "Blood" : "Counters").append(": ").append(counters);
            if (capacity > 0) builder.append("/").append(capacity);
        }
        // Tapped
        if (game.isTapped(c.getId())) {
            builder.append(", LOCKED");
        }
        // Label Text
        String text = game.getText(c.getId());
        if (text != null && text.length() > 0) {
            builder.append(",").append(text);
        }
        out.write("<span class='cardtext'>" + builder.toString() + "</span>");
        // Other cards attached
        if (p.doNesting()) {
            Card[] cards = c.getCards();
            if (cards != null && cards.length > 0) {
                out.write("<ol>");
                for (Card card : cards) {
                    request.setAttribute("cparams", new CardParams(card));
                    out.write("<li>");
%>
<jsp:include page="card.jsp"/>
<%
                    out.write("</li>");
                }
                out.write("</ol>");
            }
        }
    } else {
        CardEntry card = p.getEntry();
        if (card.isCrypt()) {
            out.write("(" + card.getGroup() + ")");
        }
    }
%>