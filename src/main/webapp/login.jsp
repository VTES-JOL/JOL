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
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
          integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

    <link rel="stylesheet" type="text/css" href="css/new.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.structure.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/jquery-ui.theme.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/light.css"/>
    <link rel="shortcut icon" href="images/favicon.ico"/>
    <link href="https://fonts.googleapis.com/css?family=IM+Fell+English" rel="stylesheet">
</head>
<body class="container-fluid" style="max-width:15cm">
    <div style="min-height:calc(100vh - 4rem)"><!--100vh - {footer}-->
        <h1>V:TES Online</h1>
        <form id="loginForm" method="post" action="/jol/login">
            <p>Welcome to V:TES Online, the unofficial home to play Vampire: The Eternal Struggle online.</p>
            <h2>Welcome back</h2>
            <input type="text" class="form-control-lg w-100"
                   id="dsuserin" name="username" autocomplete="username" placeholder="Username"/>
            <input type="password" class="form-control-lg w-100 mt-1"
                   id="dspassin" name="password" autocomplete="current-password" placeholder="Password"/>
            <button type="submit" id="loginBtn" name="login" value="Log in"
                class="btn btn-primary btn-lg btn-block mt-1">Log In</button>
            <div class="mt-2" style="text-align:center">
                <button type="button" class="btn btn-link"
                    onclick="$('#registerForm').show(); $('#loginForm').hide();">Need an account?</button>
            </div>
        </form>
        <form id="registerForm" method="post" action="/jol/register" style="display:none">
            <p>
                <a href="https://www.vekn.net/what-is-v-tes" target="_blank">
                    What is Vampire: The Eternal Struggle?
                </a>
            </p>
            <p class="mb-0">Play using text commands, in a format that suits your availability:</p>
            <ul>
                <li>Real time (RT)</li>
                <li>Check during breaks at work (WT)</li>
                <li>Check one or more times a day (QK)</li>
            </ul>
            <p>Import decks from your favorite deck building program.  Play multiple games simultaneously.  Test a deck before a tournament.</p>
            <h2>Join us</h2>
            <input type="text" class="form-control-lg w-100" name="newplayer" id="newplayer" placeholder="Username"/>
            <input type="password" class="form-control-lg w-100 mt-1" name="newpassword" autocomplete="new-password"
                   id="newpassword" placeholder="Password"/>
            <input type="email" class="form-control-lg w-100 mt-1" name="newemail" autocomplete="email" id="newemail"
                   placeholder="E-mail address"/>
            <div class="g-recaptcha mt-1"
                 data-sitekey="<%= System.getenv("JOL_RECAPTCHA_KEY") %>"></div>
            <button type="submit" name="register" value="Register"
                class="btn btn-primary btn-lg btn-block mt-1">Register</button>
            <div class="col mt-2" style="text-align:center">
                <button type="button" class="btn btn-link"
                    onclick="$('#loginForm').show(); $('#registerForm').hide();">
                    Already have an account?</button>
            </div>
        </form>
    </div>

    <!-- Footer -->
    <div style="height:4rem; text-align:center; padding-top:1rem">
        <a href="https://discord.gg/fJjac75" target="_blank">Discord</a>
        | <a href="https://www.facebook.com/groups/jolstatus/" target="_blank">Status</a>
        | <a href="mailto:admin@deckserver.net">Admin &#7367;</a></li>
        <br/><%= System.getenv("JOL_VERSION") %>
    </div>

    <!-- Bootstrap -->
    <!-- jQuery first, then Popper.js, then Bootstrap JS -->
    <script type="text/javascript" src="js/jquery-3.4.1.js"></script>
    <script src="https://unpkg.com/popper.js@1.15.0/dist/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
            integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
            crossorigin="anonymous"></script>

    <script type='text/javascript' src="js/ga.js"></script>
    <script src='https://www.google.com/recaptcha/api.js'></script>

    <script type='text/javascript'>
        $(document).ready(function() {
            $('#dsuserin').focus();
        });
    </script>
</body>
</html>
