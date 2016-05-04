<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckserver.util.*"%>
<%@page import="deckserver.rich.*"%>
<%@page import="nbclient.vtesmodel.*" %>
<%@page import="java.util.Date" %>
<% WebParams p = (WebParams) session.getAttribute("wparams");
   if(p == null) return;
   JolAdminFactory admin = AdminFactory.get(application);
   AdminBean abean = AdminFactory.getBean(application);
   String player = p.getPlayer();
   String game = (String) request.getParameter("game");
   boolean interactive = (player != null) && admin.doInteractive(player);
   GameModel model = abean.getGameModel(game);
   GameView view = model.getView(player);
   boolean refresh = view.isChanged();
   view.clearAccess();
   Date stamp = new Date(model.getTimestamp());
  // boolean refresh = !admin.haveAccessed(game).contains(player);
  // Date stamp = admin.getGameTimeStamp(game);
   int interval = RefreshInterval.calc(stamp);
   String baseurl = request.getRequestURL().toString();
   String gameurl = baseurl.substring(0,baseurl.indexOf("/jol3") + 5) + "/" + game;
   %>
<html>
<head>
<script type="text/javascript">

function rls()
{
  // location.url = 'http://espn.go.com';
  // if( !document.getElementById('dontpoll').checked) {
 //   location = 'http://espn.go.com';
     location.reload();
  //   }
  // else {
  // setTimeout("rls()",5000);
  // }
}

function start()
{
if( <% out.write(interactive + ""); %> ) {

 if ( <% out.write(refresh + ""); %> )  {
  with (parent) {
   location = "<% out.write(gameurl); %>";
   }
  }
 else {
  setTimeout("rls()", <% out.write(interval + ""); %> ); 
  }
 }
}
</script>
</head>
<body>
<script type=text/javascript>
    start();
</script>

</body>

</html>
