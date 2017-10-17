<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<table width="100%">
    <tr>
        <td width="25%" valign="top" rowspan="2">
            <div id="player" style="display: none;">
                <h4>Your games:</h4>
                <div id="owngamediv" class="gamediv">
                    <table id="owngames" class="clean-table"></table>
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
            <div id="messages" style="display: none;"></div>

            <div id="globalchat" style="display: none;">
                <div>
                    <h4>
                        Online Users:
                    </h4>
                    <div id="whoson" class="some-padding"></div>
                </div>

                <div>
                    <h4>Global Chat:</h4>
                    <div class="history" id="gchatwin">
                        <table width="100%" class="chattable" id="gchattable" cellspacing="0" cellpadding="0"
                               border="0"></table>
                    </div>
                    <form id="globalChatForm" action="javascript:globalChat();" autocomplete='off'>
                        <input type="text" maxlength="200" id="gchat" placeholder="Chat with players"/>
                    </form>
                </div>

            </div>
        </td>
        <td width="20%" valign="top">
            <h4>News:</h4>
            <ul>
                <li>
                    Donate to keep JOL running
                    <form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
                        <input type="hidden" name="cmd" value="_s-xclick">
                        <input type="hidden" name="hosted_button_id" value="BJ4GNJK6CDGLS">
                        <input type="image" src="https://www.paypalobjects.com/en_AU/i/btn/btn_donateCC_LG.gif"
                               border="0" name="submit" alt="PayPal – The safer, easier way to pay online!">
                        <img alt="" border="0" src="https://www.paypalobjects.com/en_AU/i/scr/pixel.gif" width="1"
                             height="1">
                    </form>
                </li>
                <li>
                    <a href="https://www.facebook.com/groups/jolstatus/" target="_blank">Facebook Status Group</a>
                    <p>Scheduled / Unplanned outages</p>
                </li>
                <li>
                    <a href="https://www.facebook.com/groups/jol-development/" target="_blank">Facebook Development
                        Group</a>
                    <p>Request a fix, Suggest improvements</p>
                </li>

                <c:if test="${applicationScope.get('environment') eq 'production'}">

                    <li>
                        <a href="/jol-news/">Patch Notes / News</a>
                        <p>Implemented / upcoming changes / announcements</p>
                    </li>
                    <li>
                        <a href="https://test.deckserver.net/jol/" target="_blank">Test Server</a>
                        <p>Staging / Playtesting environment</p>
                    </li>
                </c:if>

            </ul>
            <div>
                <h4>User Legend:</h4>
                <div class="some-padding">
                    <span class="player">Player</span>
                    <span class="player player-admin">Admin</span>
                    <span class="player player-superUser">Super</span>
                </div>
            </div>

        </td>
    </tr>
    <tr id="gameRow">
        <td>
            <h4>Currently active games:</h4>
            <div class="gamediv">
                <table class="clean-table">
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
