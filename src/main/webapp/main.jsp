<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="deckserver.dwr.Utils" %>
<%@page contentType="text/html" %>

<%
    String player = Utils.getPlayer(request);
    request.setAttribute("player", player);
    request.setAttribute("loggedIn", player != null);
%>

<html>
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>VOL</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
          integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css"
          integrity="sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r" crossorigin="anonymous">
    <script src="https://code.jquery.com/jquery-1.11.1.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"
            integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS"
            crossorigin="anonymous"></script>
</head>
<body>
<nav class="navbar navbar-default">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                    data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="${pageContext.request.contextPath}">JOL</a>
        </div>

        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav">
                <li><a href="${pageContext.request.contextPath}/beta/">Game Lobby</a></li>
                <c:if test="${loggedIn}">
                    <li><a href="${pageContext.request.contextPath}/player">Player Home</a></li>
                    <li><a href="${pageContext.request.contextPath}/deck">Deck Construction</a></li>
                </c:if>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <c:if test="${!loggedIn}">
                    <li><a href="${pageContext.request.contextPath}/register">Register</a></li>
                    <li><a href="${pageContext.request.contextPath}/login">Login</a></li>
                </c:if>
                <c:if test="${loggedIn}">
                    <li><p class="navbar-text">Logged in as ${player}</p></li>
                    <li><a href="${pageContext.request.contextPath}/login?logout">Logout</a></li>
                </c:if>
            </ul>
        </div>
    </div>
</nav>

<div class="container">
    <div class="page-header">
        <h1>VOL </h1>
    </div>
    <div class="row">
        <div class="col-md-6">
            <div class="panel panel-default">
                <div class="panel-heading">
                    News
                </div>
                <div class="panel-body">
                    <p>Welcome to the new JOL server</p>
                    <p>If you are coming from the current server you can login with your existing account details,
                        otherwise click <a href="${pageContext.request.contextPath}/register">register</a> to sign up
                        and start using JOL.</p>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
