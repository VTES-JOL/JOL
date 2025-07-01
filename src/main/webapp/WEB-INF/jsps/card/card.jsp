<%@ page import="net.deckserver.game.ui.state.CardDetail" %>
<%
    String detail = request.getParameter("detail");
    CardDetail cardDetail = (CardDetail) request.getAttribute(detail);
    String lockStyle = cardDetail.isLocked() ? "ratio-10x7" : "ratio-7x10";
%>
<div class="ratio border border-2 border-black rounded rounded-3 bg-light vtes-card <%= lockStyle %>">
    <div class="content">
        <div style="font-family: 'matrix',sans-serif; font-size: 10cqw" class="p-1"><%= cardDetail.getName()%>
        </div>
        <div style="font-family: 'Gill Sans', sans-serif;">Test</div>
        <span style="font-family: 'Quorum Black', sans-serif" class="badge rounded-pill bg-danger fs-6">8</span>
    </div>
</div>
