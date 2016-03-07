<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckserver.portal.*" %>
<%@page import="nbclient.vtesmodel.JolGame" %>
<% PortalParams params = (PortalParams) session.getAttribute("params");
   String page = params.getPage(); %>
<TITLE>JOL3</TITLE>
<META NAME="decription" CONTENT="JOL3 Version 0.3">
<META NAME="keywords" CONTENT="JOL">
<META NAME="robots" CONTENT="noindex, nofollow">
<META NAME="rating" CONTENT="general">
<META NAME="generator" CONTENT="vi">
<jsp:include page="styles.jsp"/>
</HEAD>
<BODY BGCOLOR="black" TEXT="red" LINK="yellow" VLINK="yellow" ALINK="yellow">
<jsp:include page="header.jsp"/>
<% if(page.equals("game")) {
    application.getNamedDispatcher("GameServlet").include(request,response);
    } else { %>
<table><tr>
<% if(params.getPlayer() != null) { %>
<td><jsp:include page="player.jsp"/></td>
<%  } %>
<td><jsp:include page="<% out.write(page); %>.jsp"/></td>
<td><jsp:include page="news.jsp"/></td>
</tr></table>
<%  } %>
</BODY>

    
