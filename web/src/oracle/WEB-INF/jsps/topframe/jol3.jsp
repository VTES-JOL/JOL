<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@page import="nbclient.vtesmodel.JolAdminFactory"%>
<html>
 <head>
  <title>JOL3</title>
  <meta name="decription" content="JOL3 Version 1.1"></meta>
  <meta name="keywords" content="JOL, VTES"></meta>
  <meta name="robots" content="noindex, nofollow"></meta>
  <meta name="rating" content="general"></meta>
  <meta name="generator" content="vi"></meta>
  <script type='text/javascript' src='/jol3/dwr/interface/DS.js'></script>
  <script type='text/javascript' src='/jol3/dwr/engine.js'></script>
  <script type='text/javascript' src='/jol3/dwr/util.js'></script>
  <script type='text/javascript' src='/jol3/ds.js'></script>
  <link rel="stylesheet" type="text/css" href="/jol3/styles.css"/>
 </head>
 <body onload="init();"><div id="dsdebug"></div><div id="loadmsg">Loading...</div><div id="loaded"
                                                                                       style="display :none;">
 <div id="disabledZone" style="position: absolute; z-index: 1000; left: 0px; top: 0px; width: 100%; height: 100%; visibility: hidden;">
<div id="messageZone" style="padding: 4px; background: red none repeat scroll 0%; position: absolute; top: 0px; right: 0px; -moz-background-clip: -moz-initial; -moz-background-origin: -moz-initial; -moz-background-inline-policy: -moz-initial; color: white; font-family: Arial,Helvetica,sans-serif;">Loading</div>
</div>                                                                                      
   <jsp:include page="/WEB-INF/jsps/topframe/topbar.jsp"/>
    
   <div id="content">
    <input type="hidden" name="contentselect" id="contentselect" value="main"/>
     
    <div id="main">
     <jsp:include page="/WEB-INF/jsps/topframe/main.jsp"/>
    </div>
     
    <div id="game" style="display :none;">
     <jsp:include page="/WEB-INF/jsps/dwr/game.jsp"/>
    </div>
     
    <div id="deck" style="display :none;">
     <jsp:include page="/WEB-INF/jsps/topframe/deck.jsp"/>
    </div>
    
    <div id="bugss" style="display :none;">
     <jsp:include page="/WEB-INF/jsps/topframe/bugs.jsp"/>
    </div>
    
    <div id="admin" style="display :none;">
      <jsp:include page="/WEB-INF/jsps/topframe/admin.jsp"/>
    </div>
<% String player = (String) request.getSession(true).getAttribute("meth");
   if(JolAdminFactory.INSTANCE.isSuperUser(player)) {
   %>   
    <div id="suser" style="display :none;">
      <jsp:include page="/WEB-INF/jsps/topframe/super.jsp"/>
    </div>
<% } %>
    <div id="help" style="display :none;">
     <iframe width="100%" height="100%" src="/doc/commands.html">
     </iframe>
    </div>
   </div>
  </div></body>
</html>
