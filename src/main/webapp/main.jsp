<%@ page import="net.deckserver.dwr.model.JolAdmin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <title>V:TES Online</title>
    <!-- Required by Bootstrap -->
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
    <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css"/>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" type="text/css" href="css/styles.css?version=<%= System.getenv("JOL_VERSION") %>"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.structure.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.theme.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/light.css"/>
    <link rel="shortcut icon" href="images/favicon.ico"/>
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
                <jsp:include page="/WEB-INF/jsps/tournament/layout.jsp"/>
            </div>

            <div id="help" style="display :none;">
                <jsp:include page="/WEB-INF/jsps/help/layout.jsp"/>
            </div>

            <div id="profile" style="display:none">
                <jsp:include page="/WEB-INF/jsps/profile/layout.jsp"/>
            </div>

        </div>
        <footer class="footer" id="footer">
            <div class="container-fluid p-2 justify-content-center justify-content-md-between d-flex bg-secondary-subtle fw-bold">
                <span id="timeStamp" class="d-none d-md-inline"></span>
                <span id="message"></span>
                <span class="d-none d-md-inline">Version: <%= JolAdmin.INSTANCE.getVersion() %></span>
            </div>
        </footer>
    </div>

    <!-- Bootstrap -->
    <!-- jQuery first, then Popper.js, then Bootstrap JS -->
    <script src="js/jquery-3.7.1.min.js"></script>
    <script src="js/jquery-throttle.min.js"></script>
    <script src="js/jquery-ui.min.js"></script>
    <script src='js/moment-with-locales.min.js'></script>
    <script src='js/moment-timezone-with-data.min.js'></script>
    <script src="js/popper.min.js"></script>
    <script src="js/tippy.all.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script src='dwr/engine.js'></script>
    <script src='dwr/interface/DS.js'></script>
    <script src='dwr/util.js'></script>
    <script src='js/ds.js?version=<%= JolAdmin.INSTANCE.getVersion() %>'></script>
    <script src="js/card-modal.js?version=<%= JolAdmin.INSTANCE.getVersion() %>"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            const informationPanel = $("#informationPanel");
            informationPanel.on('show.bs.collapse', function (event) {
                $(this).data('isShowing', true);
            })
            informationPanel.on('hide.bs.collapse', function (event) {
                console.log(event);
                if (!$(this).data('isShowing')) {
                    event.preventDefault();
                }
                $(this).data('isShowing', false);
            })
            $("#historyPanel").on('show.bs.collapse', getHistory);
        })
    </script>
</body>
</html>
