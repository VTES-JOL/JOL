<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" %>
<%@ page pageEncoding="UTF-8" %>
<html>
    <head>
        <title>V:TES Online</title>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/engine.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/interface/DS.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/util.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/ds.js'></script>
        <script src='https://www.google.com/recaptcha/api.js'></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/styles.css"/>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico" />
    </head>
    <body onload="init();" class="${applicationScope.get("environment")}">
        <div id="loadmsg">Loading...</div>
        <div id="loaded" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/topbar.jsp"/>

            <div id="content">
                <input type="hidden" name="contentselect" id="contentselect" value="main"/>

                <div id="main">
                    <jsp:include page="/WEB-INF/jsps/main.jsp"/>
                </div>

                <div id="game" style="display :none;">
                    <jsp:include page="/WEB-INF/jsps/game.jsp"/>
                </div>

                <div id="deck" style="display :none;">
                    <jsp:include page="/WEB-INF/jsps/deck.jsp"/>
                </div>

                <div id="admin" style="display :none;">
                    <jsp:include page="/WEB-INF/jsps/admin.jsp"/>
                </div>

                <div id="help" style="display :none;">
                    <jsp:include page="/WEB-INF/jsps/commands.jsp"/>
                </div>
            </div>
        </div>
    </body>
</html>