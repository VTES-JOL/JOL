<%@page import="cards.model.CardEntry" %>
<%@page import="deckserver.servlet.DeckServlet" %>
<%@page import="deckserver.util.CardParams" %>
<%@page import="deckserver.util.DeckParams" %>
<%@page import="java.util.*" %>
<jsp:include page="../../javascript/game.jsp"/>
<%
    DeckParams p = (DeckParams) request.getAttribute("dparams");
    Map deck = DeckServlet.getDeckHtmlMap(p);
    Map map = (Map) deck.get("Vampire");
    Map mapi = (Map) deck.get("Imbued");
    deck.remove("Vampire");
    deck.remove("Imbued");
    if (map == null) map = new HashMap();
    if (mapi == null) mapi = new HashMap();
    int csize = DeckServlet.sumMap(map.values()) + DeckServlet.sumMap(mapi.values());
%>
<b>Crypt: (<% out.write(csize + ""); %>)</b><br/>
<% if (mapi.size() > 0) out.write("Vampires: <br/>"); %>
<% for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
    CardEntry card = (CardEntry) i.next();
    request.setAttribute("cparams", new CardParams(card));
    out.write(map.get(card).toString() + "x "); %>
<A HREF="javascript:openWin('<% out.write(card.getCardId()); %>');">
    <% out.write(card.getName() + "(G" + card.getGroup() + ")"); %>
</a>
<br/>
<% }
    if (mapi.size() > 0) {
        out.write("Imbued: <br/>");
        for (Iterator i = mapi.keySet().iterator(); i.hasNext(); ) {
            CardEntry card = (CardEntry) i.next();
            request.setAttribute("cparams", new CardParams(card));
            out.write(mapi.get(card).toString() + "x "); %>
<A HREF="javascript:openWin('<% out.write(card.getCardId()); %>');">
    <% out.write(card.getName() + "(G" + card.getGroup() + ")"); %>
</a>
<br/>
<% }
}
    Collection sum = new ArrayList();
    for (Iterator i = deck.values().iterator(); i.hasNext(); ) {
        sum.addAll(((Map) i.next()).values());
    }
    int size = DeckServlet.sumMap(sum);
%>
<b>Library: (<% out.write(size + ""); %>)</b><br/>
<% map = (Map) deck.get("Master");
    if (map == null) map = new HashMap();
    deck.remove("Master");%>
Master: (<% out.write(DeckServlet.sumMap(map.values()) + ""); %>)<br/>
<% for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
    CardEntry card = (CardEntry) i.next();
    request.setAttribute("cparams", new CardParams(card));
    out.write(map.get(card).toString() + "x ");%>
<A HREF="javascript:openWin('<% out.write(card.getCardId()); %>');">
    <% out.write(card.getName()); %>
</A>
<!--jsp:include page="../state/card.jsp"/-->
<br/>
<% }
    Collection types = new TreeSet(deck.keySet());
    for (Iterator j = types.iterator(); j.hasNext(); ) {
        Object type = j.next();
        map = (Map) deck.get(type);
        out.write(type.toString()); %>: (<% out.write(DeckServlet.sumMap(map.values()) + "");%>)<br/>
<% for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
    CardEntry card = (CardEntry) i.next();
    request.setAttribute("cparams", new CardParams(card));
    out.write(map.get(card).toString() + "x ");%>
<A HREF="javascript:openWin('<% out.write(card.getCardId()); %>');">
    <% out.write(card.getName()); %>
</A>
<!--jsp:include page="../state/card.jsp"/-->
<br/>
<% }
} %>
   
