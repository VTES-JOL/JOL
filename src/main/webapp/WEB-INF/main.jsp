<%@ page import="net.deckserver.JolAdmin" %>
<%@ page import="net.deckserver.services.VersionService" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <title>V:TES Online</title>
    <script>
        const BASE_URL = "<%= System.getenv().getOrDefault("BASE_URL", "https://static.dev.deckserver.net") %>";
    </script>
    <!-- Required by Bootstrap -->
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/css/styles.css?version=<%= VersionService.getVersion() %>"/>
    <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/css/<%= System.getenv().getOrDefault("TYPE", "dev") %>.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery-ui.min.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery-ui.structure.min.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery-ui.theme.min.css"/>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/light.css"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/lipis/flag-icons@7.3.2/css/flag-icons.min.css"/>
    <link rel="shortcut icon" href="https://static.deckserver.net/assets/images/favicon.ico"/>
    <link href="https://fonts.googleapis.com/css?family=IM+Fell+English" rel="stylesheet">
</head>
<body>
<div id="wrapper">
    <jsp:include page="/WEB-INF/jsps/topbar.jsp"/>
    <div id="content" class="container-fluid">
        <div id="main">
            <jsp:include page="/WEB-INF/jsps/main/layout.jsp"/>
        </div>

        <div id="game" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/game/layout.jsp"/>
        </div>

        <div id="active" style="display:none;">
            <jsp:include page="/WEB-INF/jsps/watch/layout.jsp"/>
        </div>

        <div id="deck" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/decks/layout.jsp"/>
        </div>

        <div id="lobby" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/lobby/layout.jsp"/>
        </div>

        <div id="admin" style="display: none;">
            <jsp:include page="/WEB-INF/jsps/admin/layout.jsp"/>
        </div>

        <div id="tournament" style="display: none">
            <%--                <jsp:include page="/WEB-INF/jsps/tournament/layout.jsp"/>--%>
        </div>

        <div id="profile" style="display:none">
            <jsp:include page="/WEB-INF/jsps/profile/layout.jsp"/>
        </div>

    </div>
    <footer class="footer d-none d-sm-block" id="footer">
        <div class="container-fluid p-2 justify-content-center justify-content-md-between d-flex bg-secondary-subtle fw-bold">
            <span id="timeStamp" class="d-none d-md-inline"></span>
            <span id="message"></span>
            <span class="d-none d-md-inline">Version: <%= VersionService.getVersion() %></span>
        </div>
    </footer>
</div>

<!-- Bootstrap -->
<!-- jQuery first, then Popper.js, then Bootstrap JS -->
<script src="${pageContext.request.contextPath}/js/jquery-3.7.1.min.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-throttle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui.min.js"></script>
<script src='${pageContext.request.contextPath}/js/moment-with-locales.min.js'></script>
<script src='${pageContext.request.contextPath}/js/moment-timezone-with-data.min.js'></script>
<script src="${pageContext.request.contextPath}/js/popper.min.js"></script>
<script src="${pageContext.request.contextPath}/js/tippy.all.min.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<script src='${pageContext.request.contextPath}/dwr/engine.js'></script>
<script src='${pageContext.request.contextPath}/dwr/interface/DS.js'></script>
<script src='${pageContext.request.contextPath}/dwr/util.js'></script>
<script src='${pageContext.request.contextPath}/js/ds.js?version=<%= VersionService.getVersion() %>'></script>
<script src="${pageContext.request.contextPath}/js/card-modal.js?version=<%= VersionService.getVersion() %>"></script>
<script>
    <jsp:include page="notification.jsp"/>
</script>
</body>
</html>
