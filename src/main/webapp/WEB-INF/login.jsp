<%@ page import="net.deckserver.dwr.model.JolAdmin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html" %>
<%@ page pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
    <title>V:TES Online</title>
    <!-- Required by Bootstrap -->
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
    <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="css/styles.css"/>
    <link rel="stylesheet" type="text/css" href="css/<%= System.getenv().getOrDefault("TYPE", "dev") %>.css"/>
    <link rel="shortcut icon" href="images/favicon.ico"/>
    <link href="https://fonts.googleapis.com/css?family=IM+Fell+English" rel="stylesheet">
    <script src="https://challenges.cloudflare.com/turnstile/v0/api.js" async defer></script>
</head>
<body>
    <div id="wrapper" class="container-fluid">
        <div class="w-md-50 m-auto mt-4" id="content">
            <h1>V:TES Online</h1>
            <div class="card" id="loginPanel">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span class="fs-5">Welcome back</span>
                    <button type="button" class="btn btn-link" onclick="$('#registerPanel').show(); $('#loginPanel').hide();">Need an account?</button>
                </div>
                <div class="card-body">
                    <p>Welcome to V:TES Online, the unofficial home to play Vampire: The Eternal Struggle online.</p>
                    <p>
                        <a href="https://www.vekn.net/what-is-v-tes" target="_blank">What is Vampire: The Eternal Struggle?</a>
                    </p>
                    <form id="loginForm" method="post" action="/jol/login">
                        <div class="form-floating mb-3">
                            <input type="text" class="form-control" id="dsuserin" name="username" autocomplete="username" placeholder="Username">
                            <label for="dsuserin">Username</label>
                        </div>
                        <div class="form-floating mb-3">
                            <input type="password" class="form-control" id="dspassin" name="password" autocomplete="password" placeholder="Password">
                            <label for="dspassin">Password</label>
                        </div>
<%--                        <c:if test='<%= System.getenv().getOrDefault("ENABLE_CAPTCHA", "true").equals("true") %>'>--%>
<%--                            <div class="cf-turnstile" data-sitekey="<%= System.getenv().get("JOL_RECAPTCHA_KEY") %>" data-theme="light"></div>--%>
<%--                        </c:if>--%>
                        <button type="submit" id="loginBtn" name="login" value="Log in" class="btn btn-outline-secondary btn-lg mt-2">Log In</button>
                    </form>
                </div>
            </div>
            <div class="card" id="registerPanel" style="display: none;">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span class="fs-5">Join us</span>
                    <button type="button" class="btn btn-link" onclick="$('#loginPanel').show(); $('#registerPanel').hide();">Already have an account?</button>
                </div>
                <div class="card-body">
                    <div class="mb-2">
                        <div>Import decks from your favorite deck building program.  Play multiple games simultaneously.  Test a deck before a tournament.</div>
                    </div>
                    <form id="registerForm" method="post" action="/jol/register">
                        <div class="form-floating mb-3">
                            <input type="text" class="form-control" id="newplayer" name="newplayer" placeholder="Username">
                            <label for="newplayer">Username</label>
                        </div>
                        <div class="form-floating mb-3">
                            <input type="password" class="form-control" id="newpassword" name="newpassword" placeholder="Password">
                            <label for="newpassword">Password</label>
                        </div>
                        <div class="form-floating mb-3">
                            <input type="email" class="form-control" id="newemail" name="newemail" placeholder="user@example.org">
                            <label for="newemail">E-mail address</label>
                        </div>
                        <c:if test='<%= System.getenv().getOrDefault("ENABLE_CAPTCHA", "true").equals("true") %>'>
                            <div class="cf-turnstile" data-sitekey="<%= System.getenv().get("JOL_RECAPTCHA_KEY") %>" data-theme="light"></div>
                        </c:if>
                        <button type="submit" name="register" value="Register" class="btn btn-outline-secondary btn-lg mt-1">Register</button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap -->
    <!-- jQuery first, then Popper.js, then Bootstrap JS -->
    <script src="js/jquery-3.7.1.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script type='text/javascript'>
        $(document).ready(function() {
            $('#dsuserin').focus();
        });
    </script>
</body>
</html>
