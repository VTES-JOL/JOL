<%@page import="deckserver.util.*" %>
<%@page import="nbclient.model.Card" %>
<%@page import="cards.model.CardEntry" %>
<%@page import="nbclient.vtesmodel.*" %>
<% CardParams p = (CardParams) request.getAttribute("cparams");
   JolGame game = (JolGame) request.getAttribute("game");
   Card c = p.getCard();
   if (p.isHidden()) { %>
XXXXXX
<% } else { %>
<A HREF="#cd" onclick="javascript:getCard('<% out.write(game.getName() + "','" + p.getId()); %>');">
 <% out.write(p.getName()); %>
</A>
<% }
  if(game != null) {
   int counters = game.getCounters(c.getId());
   int capac = game.getCapacity(c.getId());
   if(counters > 0 || capac > 0) {
       String lab = (capac > 0) ? "Blood" : "Counters";
       out.write(", " + lab + ": " + counters);
       if(capac > 0) out.write("/" + capac);
   }
   if(game.isTapped(c.getId())) {
       out.write(", TAPPED");
   }
   String text = game.getText(c.getId());
   if(text != null && text.length() > 0) {
       out.write("," + text);
       }
   if(p.doNesting()) { 
       Card[] cards = (Card[])c.getCards();
       if(cards != null && cards.length > 0) %>
       <ol>
       <% for(int i = 0; i < cards.length; i++) { 
               request.setAttribute("cparams", new CardParams(cards[i])); %>
               <jsp:include page="card.jsp"/>
<%         } %>
       </ol>
<%  }
  } else { // print extra stuff during card viewing
      CardEntry card = p.getEntry();
      if(card.isCrypt()) {
          String[] text = card.getFullText();
          String group = "G" + card.getGroup();
          out.write("(" + group + ")");
          }
  } %>