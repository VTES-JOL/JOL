<%@page import="deckserver.util.HandParams" %>
<% HandParams p = (HandParams) request.getAttribute("hparams"); %>
<FONT COLOR=<% out.write(p.getColor()); %>>
        <% out.write(p.getText());
               out.write(" (" + p.getSize() + ")");  %>
    <OL>
        <% for (int i = 0; i < p.getSize(); i++) {
            request.setAttribute("cparams", p.getCardParam(i)); %>
        <LI>
            <jsp:include page="card.jsp"/>
        </LI>
        <% } %>
    </OL>