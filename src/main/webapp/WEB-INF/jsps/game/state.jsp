<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="net.deckserver.dwr.model.JolGame" %>
<%
    JolGame game = (JolGame) request.getAttribute("game");
%>
<c:forEach items="<%=game.getPlayers()%>" var="player" varStatus="playerIndex">
    <jsp:include page="player.jsp">
        <jsp:param name="player" value="${player}"/>
        <jsp:param name="playerIndex" value="${playerIndex.count}"/>
    </jsp:include>
</c:forEach>
