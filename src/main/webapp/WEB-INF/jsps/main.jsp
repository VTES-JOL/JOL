<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<table width="100%">
    <tr>
        <td width="25%" class="layout" rowspan="2">
            <div id="player" style="display: none;">
                <h4>Your games:</h4>
                <table id="owngames" class="clean-table light"></table>
            </div>
            <div id="register">
                <h4>Register</h4>
                <form method="post" class="light some-padding">
                    <span>Register to create decks and join games!</span>
                    <table class="clean-no-border">
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
        <td width="50%" class="layout">
            <div id="messages" style="display: none;"></div>

            <div id="welcome">
                <h4>Welcome to V:TES Online</h4>
                <div class="light">

                </div>
            </div>

            <div id="globalchat" style="display: none;">
                <div>
                    <h4>
                        Online Users:
                    </h4>
                    <div id="whoson" class="light some-padding"></div>
                </div>

                <div>
                    <h4>Global Chat:</h4>
                    <div class="info-area history" id="gchatwin">
                        <div id="gchattable"></div>
                    </div>
                    <form id="globalChatForm" action="javascript:globalChat();" autocomplete='off'>
                        <input type="text" maxlength="200" id="gchat" placeholder="Chat with players"/>
                    </form>
                </div>

            </div>
        </td>
        <td width="20%" class="layout" rowspan="2">
            <h4>Links:</h4>
            <ul class="condensed-list light">
                <li>
                    <a href="https://www.facebook.com/groups/jolstatus/" target="_blank">Facebook Status Group</a>
                </li>
                <li>
                    <a href="https://www.facebook.com/groups/jol-development/" target="_blank">Facebook Development
                        Group</a>
                </li>
                <li>
                    <a href="/jol-news/">Patch Notes / News</a>
                </li>
                <li>
                    <a href="https://test.deckserver.net/jol/" target="_blank">Test Server</a>
                </li>

            </ul>
            <h4>User Ranks:</h4>
            <ul class="condensed-list light">
                <li>
                    <span class="label label-light">Embrace</span>
                    <p>Can make decks, play, chat</p>
                </li>
                <li>
                    <span class="label label-dark">Justicar</span>
                    <p>Creates games, Invites players, Closes games</p>
                </li>
                <li>
                    <span class="label label-warning">Inner Circle</span>
                    <p>Administer games for all other Justicars</p>
                </li>
            </ul>
            <h4>Donate:</h4>
            <ul class="condensed-list light">
                <li>
                    <span>Goes towards keeping JOL running.  Any donation appreciated.</span>
                    <form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top" id="donateBox">
                        <input type="hidden" name="cmd" value="_s-xclick">
                        <input type="hidden" name="hosted_button_id" value="BJ4GNJK6CDGLS">
                        <input type="image" src="https://www.paypalobjects.com/en_AU/i/btn/btn_donateCC_LG.gif"
                               border="0" name="submit" alt="PayPal – The safer, easier way to pay online!">
                        <img alt="" border="0" src="https://www.paypalobjects.com/en_AU/i/scr/pixel.gif" width="1"
                             height="1">
                    </form>
                </li>
            </ul>

        </td>
    </tr>
    <tr id="gameRow">
        <td>
            <h4>Currently active games:</h4>
            <div>
                <table class="clean-table light">
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
