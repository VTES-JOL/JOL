<%@page import="cards.model.CardEntry" %>
<%@page import="deckserver.util.CardParams" %>
<%@page import="deckserver.util.DeckParams" %>
<%@page import="deckserver.util.WebParams" %>
<jsp:include page="../../javascript/game.jsp"/>
<%
    DeckParams p = (DeckParams) request.getAttribute("dparams");
    WebParams params = (WebParams) request.getSession().getAttribute("wparams");
%>
<form method=post>
    <input type="hidden" name="editinit" value="true"/>
    Type:
    <select name=type>
        <option value="All" <% if (p.getType().equals("All")) { %> SELECTED <%}%> >
            All
        </option>
        <% for (int i = 0; i < CardEntry.types.length; i++) {
            String type = CardEntry.types[i]; %>
        <option value="<% out.write(type); %>" <% if (p.getType().equals(type)) { %> SELECTED <%}%> >
            <% out.write(type); %>
        </option>
        <% } %>
    </select>
    Search on
    <input name=query value="<% out.write(p.getQuery()); %>"/>
    <input type=submit name=construct value=Search/>
    <hr/>
    <% CardEntry[] cards = p.getCards();
        for (int i = 0; i < cards.length; i++) { %>
    <input type=checkbox name=newcard value="<% out.write(cards[i].getCardId()); %>">
    <% request.setAttribute("cparams", new CardParams(cards[i])); %>
    <jsp:include page="../state/card.jsp"/>
    </input>
    <% } %>

    <table>
        <td rowspan=10>
    <textarea cols=50 rows=<% out.write(Math.max(10,Math.min(p.getDeckObj().getCards().length,50)) + "");%> name=deck><%
        out.write(p.getDeckObj().getDeckString());
    %></textarea>
        </td>
        <td></td>
        <td rowspan=10">
            <jsp:include page="./showdeck.jsp"/>
            <% // DeckServlet.printDeckHtml(params,p.getDeckObj(),new PrintWriter(out));
            %>
        </td>
        <tr>
            <td><input type=submit name=construct value="Adjust deck"/></td>
        </tr>
        <tr>
            <td>Deck name:<input name=deckname value="<% out.write(p.getName()); %>"/></td>
        </tr>
        <tr>
            <td><input type=submit name=submit value="Submit deck"/></td>
        </tr>
    </table>
</form>

Lines in the deck submission not matched to cards:<br/>
<b>
    <% String[] errors = p.getErrors();
        for (int i = 0; i < errors.length; i++)
            out.write(errors[i] + "<br/>");
    %>
</b>
<p>

    Use this page to enter your decks. Use the search button on the top to search through all
    available cards. Check the boxes next to the cards you want to include in your deck. Hit the
    "Adjust deck" button - this adds all the checked cards to the deck. You can add multiples of
    a given card by changing the numeral in the deck listing. When you're finished, hit the "Submit
    deck" button. Then give the deck a name, and you're done.
<p>
    Alternately, you can simply cut/paste a deck into the text area. A line is formatted like:
<pre>
1x Side Strike
</pre>
but unique prefixes (like in this case "side s") can be used, caps are optional, and the 1x is optional.
Lines that aren't recognized by the software are ignored, so you can even paste decks with grouping
or other text included into the text box.
