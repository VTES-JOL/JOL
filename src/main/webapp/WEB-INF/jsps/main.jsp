<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<table width="100%">
    <tr>
        <td width="25%" class="layout" rowspan="2">
            <div id="player" style="display: none;">
                <h4 class="header">Your games:</h4>
                <table id="ownGames" class="clean-table light"></table>
            </div>
            <div id="register">
                <h4 class="header">Register</h4>
                <form method="post" class="light some-padding">
                    <span>Register to create decks and join games!</span>
                    <table class="clean-no-border">
                        <tr>
                            <td><label for="newplayer">Name:</label></td>
                            <td><input style="width:100%;" type="text" size="30" name="newplayer" id="newplayer"/></td>
                        </tr>
                        <tr>
                            <td><label for="newpassword">Password:</label></td>
                            <td><input style="width:100%;" type="password" size="30" name="newpassword" autocomplete="new-password"
                                       id="newpassword"/></td>
                        </tr>
                        <tr>
                            <td><label for="newemail">Email:</label></td>
                            <td><input style="width:100%;" type="text" size=30 name="newemail" autocomplete="email" id="newemail"/></td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <div class="g-recaptcha"
                                     data-sitekey="<%= System.getenv("JOL_RECAPTCHA_KEY") %>"></div>
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
                <h4 class="header">Welcome to V:TES Online</h4>
                <div class="light padded">
                    <p>V:TES Online is the unofficial home to play Vampire: The Eternal Struggle online.</p>

                    <p>Register an account, create a deck, or import from your favorite deck building programs.</p>

                    <p>
                        Play using text commands, in a format that suits your availability.
                        <ul>
                            <li>Real time (RT)</li>
                            <li>Check during breaks at work (WT)</li>
                            <li>Check one or more times a day (QK)</li>
                        </ul>
                    </p>

                    <p>
                        Play multiple games simultaneously, test a deck before a tournament.
                    </p>

                    <p>Our Prince's are available to help create games for you</p>
                </div>
            </div>

            <div id="globalchat" style="display: none;">
                <div>
                    <h4 class="header">
                        Online Users:
                    </h4>
                    <div id="whoson" class="light some-padding"></div>
                </div>

                <div>
                    <h4 class="header">Global Chat:</h4>
                    <div class="info-area" id="gchatwin">
                        <div id="globalChatOutput" class="scrollable side-padded"></div>
                    </div>
                    <form id="globalChatForm" action="javascript:doGlobalChat();" autocomplete='off'>
                        <input type="text" maxlength="200" id="gchat" placeholder="Chat with players"/>
                    </form>
                </div>

            </div>
        </td>
        <td width="20%" class="layout" rowspan="2">
            <h4 class="header collapse">User Ranks:</h4>
            <ul class="condensed-list light">
                <li>
                    <span class="label label-light">Embrace</span>
                    <p>Can make decks, play, chat</p>
                </li>
                <li>
                    <span class="label label-dark">Prince</span>
                    <p>Creates games, Invites players, Closes games</p>
                </li>
                <li>
                    <span class="label label-light label-bold">Justicar</span>
                    <p>Can chat in games they are not playing to attend to rulings</p>
                </li>
                <li>
                    <span class="label label-warning">Inner Circle</span>
                    <p>Administer games for all other Princes</p>
                </li>
            </ul>
            <h4 class="header collapse">Contact</h4>
            <ul class="condensed-list light">
                <li><a href="mailto:admin@deckserver.net">Contact Site Administrator</a></li>
            </ul>
            <h4 class="header collapse">Links:</h4>
            <ul class="condensed-list light">
                <li>
                    <a href="https://www.facebook.com/groups/jolstatus/" target="_blank">Facebook Status Group</a>
                </li>
                <li>
                    <a href="https://www.facebook.com/groups/jol-development/" target="_blank">Facebook Development
                        Group</a>
                </li>
                <li>
                    <a href="https://discord.gg/fJjac75" target="_blank">Discord Channel</a>
                </li>
                <li>
                    <a href="/jol-news/">Patch Notes / News</a>
                </li>
                <li>
                    <a href="https://test.deckserver.net/jol/" target="_blank">Test Server</a>
                </li>

            </ul>
        </td>
    </tr>
</table>
