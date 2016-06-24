<%@page import="deckserver.interfaces.CardEntry" %>
<%@page import="deckserver.util.CardParams" %>
<%@page import="deckserver.util.DeckParams" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsps/scripts.jsp"/>
<%
    DeckParams p = (DeckParams) request.getAttribute("dparams");
    String[] types = CardEntry.types;
    CardEntry[] cards = p.getCards();
    String[] errors = p.getErrors();
    request.setAttribute("types", types);
    request.setAttribute("p", p);
    request.setAttribute("cards", cards);
    request.setAttribute("errors", errors);
%>
<form method=post>
    <input type="hidden" name="editinit" value="true"/>
    Type:
    <select name="type" title="type">
        <option value="All" <% if (p.getType().equals("All")) { %> selected <%}%> >
            All
        </option>
        <c:forEach items="${types}" var="type">
            <option value="${type}" ${p.type.equals(type) ? "selected" : ""}>${type}</option>
        </c:forEach>
    </select>
    Search on
    <input name="query" value="<%= p.getQuery() %>" title="query"/>
    <input type="submit" name="construct" value="Search"/>
    <hr/>
    <c:forEach items="${cards}" var="card">
        <input type="checkbox" name="newcard" title="newcard" value="${card.cardId}"/>
        <%
            CardEntry card = (CardEntry) pageContext.findAttribute("card");
            request.setAttribute("cparams", new CardParams(card));
        %>
        <jsp:include page="/WEB-INF/jsps/card.jsp"/>
    </c:forEach>
    <table>
        <tr>
            <td>
                <label>Deck name:</label>
                <input title="deckname" name="deckname" value="<%= p.getName() %>"/>
                <input type="submit" name="construct" value="Adjust deck"/>
            </td>
            <td><input type="submit" name="submit" value="Submit deck"/></td>
        </tr>
        <tr>
            <td rowspan="10">
    <textarea title="deck" cols="50"
              rows="<%= Math.max(10,Math.min(p.getDeckObj().getCards().length,50)) %>"
              name="deck"><%=p.getDeckObj().getDeckString()%></textarea>
            </td>
            <td rowspan="10">
                <jsp:include page="showdeck.jsp"/>
            </td>
        </tr>
    </table>
</form>

Lines in the deck submission not matched to cards:
<ul>
    <c:forEach items="${errors}" var="error">
        <li>${error}</li>
    </c:forEach>
</ul>

<p>
    Use this page to enter your decks. Use the search button on the top to search through all
    available cards. Check the boxes next to the cards you want to include in your deck. Hit the
    "Adjust deck" button - this adds all the checked cards to the deck. You can add multiples of
    a given card by changing the numeral in the deck listing. When you're finished, hit the "Submit
    deck" button. Then give the deck a name, and you're done.
</p>
<p>
    Alternately, you can simply cut/paste a deck into the text area. A line is formatted like:
</p>
<pre>
1x Side Strike
</pre>
<p>
    but unique prefixes (like in this case "side s") can be used, caps are optional, and the 1x is optional.
    Lines that aren't recognized by the software are ignored, so you can even paste decks with grouping
    or other text included into the text box.
</p>