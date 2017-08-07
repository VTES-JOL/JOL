<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" %>
<%@ page pageEncoding="UTF-8" %>
<html>
    <head>
        <title>V:TES Online</title>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/engine.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/interface/DS.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/util.js'></script>
        <script type='text/javascript' src='${pageContext.request.contextPath}/js/ds.js?version=${project.version}'></script>
        <script src='https://www.google.com/recaptcha/api.js'></script>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/styles.css?version=${project.version}"/>
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

                <div id="_guides" style="display: none;">
                    <jsp:include page="/WEB-INF/jsps/guides.jsp"/>
                </div>
            </div>
        </div>
        <script>
            (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
                    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
                m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
            })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

            ga('create', 'UA-88229809-1', 'auto');
            ga('send', 'pageview');
        </script>
    </body>
</html>