<%@ page contentType="text/html" %>
<%@ page pageEncoding="UTF-8" %>
<html>
    <head>
        <title>JOL3</title>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/engine.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/interface/DS.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/util.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/ds.js'></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/styles.css"/>
    </head>
    <body onload="init();">
        <div id="dsdebug"></div>
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
