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
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css" integrity="sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB" crossorigin="anonymous">

        <link rel="stylesheet" type="text/css" href="css/styles.css"/>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.min.css"/>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.structure.min.css"/>
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.theme.min.css"/>
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

				<div class="row">  <!-- Footer -->
					<div class="col">
						<small>v<%= System.getenv("JOL_VERSION")%></small>
						<span id="chatstamp" class="label label-light label-basic navbar-text"></span>
					</div>
				</div>
				<div class="row mb-5">
					<div class="col">
						<a id="toggleMobileViewLink" href="#" onclick="toggleMobileView(event);">Desktop view</a>
					</div>
				</div>
            </div>
        </div>

        <!-- Bootstrap -->
        <!-- jQuery first, then Popper.js, then Bootstrap JS -->
        <script type="text/javascript" src="js/jquery-3.2.1.js"></script>
        <!--<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>-->
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js" integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49" crossorigin="anonymous"></script>
        <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js" integrity="sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T" crossorigin="anonymous"></script>

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
