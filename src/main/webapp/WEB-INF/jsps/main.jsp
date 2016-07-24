<%@page contentType="text/html" %>
<table width="100%">
    <tr>
        <td width="25%" valign="top">
            <div id="player" style="display: none;">
                <span>Your games:</span>
                <div id="owngamediv" class="gamediv">
                    <table class="gametable" id="owngames" border="1" cellspacing="1" cellpadding="1"
                           width="100%"></table>
                </div>
            </div>
            <div id="register">
                <span>Register for deckserver.net to create decks and join games!</span>
                <form method="post">
                    <table>
                        <tr>
                            <td><label for="newplayer">Name:</label></td>
                            <td><input type="text" size="30" name="newplayer" id="newplayer"/></td>
                        </tr>
                        <tr>
                            <td><label for="newpassword">Password:</label></td>
                            <td><input type="password" size="30" name="newpassword" id="newpassword"/></td>
                        </tr>
                        <tr>
                            <td><label for="newemail">Email:</label></td>
                            <td><input type="text" size=30 name="newemail" id="newemail"/></td>
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
                Welcome to JOL-4 beta, the latest version of Jyhad-OnLine, where you can play Vampire-The Eternal
                Struggle(VTES) card games online over
                the web.
                To play games on this server, register, login, create some decks, and use chat below to organize games,
                more regular games are organized via the league, see thelink on the right.
            </p>
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
            <span>Currently active games:</span>
            <div class="gamediv">
                <table id="activegames" border="1" cellspacing="1" cellpadding="1" width="100%"></table>
            </div>
        </td>
        <td width="20%" align="top">
            <div id="news"></div>
        </td>
    </tr>
</table>
