<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="net.deckserver.game.storage.state.RegionType" %>
<%@ page import="net.deckserver.dwr.model.JolGame" %>
<%@ page import="net.deckserver.game.interfaces.state.Card" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
    String player = (String) request.getAttribute("player");
    String viewer = (String) request.getAttribute("viewer");
    Card[] cards = game.getState().getPlayerLocation(player, RegionType.HAND.xmlLabel()).getCards();
%>
<c:forEach items="<%= cards %>" var="card" varStatus="counter">
    <jsp:include page="card-simple.jsp">
        <jsp:param name="player" value="<%= player %>"/>
        <jsp:param name="region" value="<%= RegionType.HAND %>"/>
        <jsp:param name="id" value="${card.id}"/>
        <jsp:param name="index" value="${counter.count}"/>
        <jsp:param name="visible" value="true"/>
    </jsp:include>
</c:forEach>
