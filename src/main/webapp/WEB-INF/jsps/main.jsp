<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<table width="100%">
    <tr>
        <td width="25%" valign="top" rowspan="2">
            <div id="player" style="display: none;">
                <span>Your games:</span>
                <div id="owngamediv" class="gamediv">
                    <table class="gametable" id="owngames" border="1" cellspacing="1" cellpadding="1"
                           width="100%"></table>
                </div>
            </div>
            <div id="register">
                <span>Register to create decks and join games!</span>
                <form method="post">
                    <table>
                        <tr>
                            <td><label for="newplayer">Name:</label></td>
                            <td><input style="width:100%;" type="text" size="30" name="newplayer" id="newplayer"/></td>
                        </tr>
                        <tr>
                            <td><label for="newpassword">Password:</label></td>
                            <td><input style="width:100%;" type="password" size="30" name="newpassword"
                                       id="newpassword"/></td>
                        </tr>
                        <tr>
                            <td><label for="newemail">Email:</label></td>
                            <td><input style="width:100%;" type="text" size=30 name="newemail" id="newemail"/></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <div class="g-recaptcha"
                                     data-sitekey="<%= System.getProperty("recaptcha.key") %>"></div>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><input type=submit name="register" value="Register"/></td>
                        </tr>
                    </table>
                </form>
            </div>
        </td>
        <td width="50%" valign="top">
            <p>
                Welcome to V:TES Online, where you can play Vampire-The Eternal Struggle (V:TES) card games online over
                the web.
            </p>
            <p>
                To play games on this server login, create some decks, and use chat below to organize games.
            </p>

            <c:if test="${applicationScope.get('environment') eq 'test'}">
                <h3>TEST SERVER - DATA ON THIS SERVER SUBJECT TO CHANGE</h3>
            </c:if>

            <div id="globalchat" style="display: none;">
                <p>Now logged on: <span id="whoson"></span></p>
                <p>Admins currently on: <span id="adson"></span></p>
                <span>Chat to organize new games:</span>
                <div class="history" id="gchatwin">
                    <table width="100%" class="chattable" id="gchattable" cellspacing="0" cellpadding="0"
                           border="0"></table>
                </div>
                <form action="javascript:globalChat();">
                    <span>
                        <span id="chatstamp"></span>
                        <label for="gchat">Chat:</label>
                        <input type="text" style="width:100%" maxlength="200" id="gchat"/>
                    </span>
                </form>
            </div>
        </td>
        <td width="20%" valign="top">
            <p>
                <a href="https://www.facebook.com/groups/jolstatus/" target="_blank">Facebook Status Group</a>
            </p>
            <c:if test="${applicationScope.get('environment') eq 'production'}">
                <p><a href="/jol-news/">Patch Notes / News</a></p>
                <p><a href="https://test.deckserver.net/jol/" target="_blank">Test Server</a></p>
                <p><a href="https://www.kickstarter.com/projects/254514360/white-lily-miniseries-issue-1-expanded-print-run?ref=8xoa9e" target="_blank">White Lily Kickstarter</a></p>
            </c:if>
        </td>
    </tr>
    <tr id="gameRow">
        <td>
            <div class="gamediv">
                <span>Currently active games:</span>
                <table border="1" cellspacing="1" cellpadding="1" width="100%">
                    <thead>
                    <tr>
                        <th>Game Name</th>
                        <th>Last Access</th>
                        <th>Current Turn</th>
                        <th>Active Players</th>
                        <th>Game Admin</th>
                    </tr>
                    </thead>
                    <tbody id="activegames">

                    </tbody>
                </table>
            </div>
        </td>
    </tr>
</table>
