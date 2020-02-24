<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" %>
<%@ page pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
    <head>
        <title>V:TES Online</title>

        <!-- Required by Bootstrap -->
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <!-- Bootstrap CSS -->
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

        <link rel="stylesheet" type="text/css" href="css/styles.css"/>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.min.css"/>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.structure.min.css"/>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.theme.min.css"/>
        <link rel="stylesheet" type="text/css" href="css/light.css"/>
        <link rel="shortcut icon" href="images/favicon.ico" />
        <link href="https://fonts.googleapis.com/css?family=IM+Fell+English" rel="stylesheet">
    </head>
    <body>
        <div id="loadMessage" class="col text-center">
            <h1>Loading...</h1>
        </div>
        <div id="loaded" style="display :none;">
            <jsp:include page="/WEB-INF/jsps/topbar.jsp"/>

            <div id="content" class="container-fluid">
                <div id="main">
                    <jsp:include page="/WEB-INF/jsps/main.jsp"/>
                </div>

                <div id="game" style="display :none;">
                    <jsp:include page="/WEB-INF/jsps/game.jsp"/>
                </div>

                <div id="active" style="display:none;">
                    <jsp:include page="/WEB-INF/jsps/active.jsp"/>
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

                <div id="profile" style="display:none">
                    <jsp:include page="/WEB-INF/jsps/profile.jsp"/>
                </div>

                <div id="super" style="display:none">
                    <jsp:include page="/WEB-INF/jsps/super.jsp"/>
                </div>
				<!-- Footer -->
				<div class="row mt-2">
					<div class="col">
						<small>v<%= System.getenv("JOL_VERSION")%></small>
						<span id="chatstamp" class="label label-light label-basic navbar-text"></span>
					</div>
				</div>
            </div>
        </div>

        <!-- Bootstrap -->
        <!-- jQuery first, then Popper.js, then Bootstrap JS -->
        <script type="text/javascript" src="js/jquery-3.4.1.js"></script>
        <script src="https://unpkg.com/popper.js@1.15.0/dist/umd/popper.min.js"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>

        <script type='text/javascript' src="js/ga.js"></script>
        <script type='text/javascript' src='js/moment-with-locales.min.js'></script>
        <script type='text/javascript' src='js/moment-timezone-with-data.min.js'></script>
        <script type="text/javascript" src="js/jquery-ui.js"></script>
        <script type="text/javascript" src="js/jquery-throttle.js"></script>
        <script type='text/javascript' src='dwr/engine.js'></script>
        <script type='text/javascript' src='dwr/interface/DS.js'></script>
        <script type='text/javascript' src='dwr/util.js'></script>
        <script type='text/javascript' src="js/tippy.all.min.js"></script>
        <script type='text/javascript' src='js/ds.js'></script>
        <script src='https://www.google.com/recaptcha/api.js'></script>
    </body>
</html>
