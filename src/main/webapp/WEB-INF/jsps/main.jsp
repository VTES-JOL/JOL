<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<div class="row">
	<div class="col">
		<div id="messages" style="display: none;"></div>
	</div>
</div>
<div class="row">
    <div id="player" class="col-sm-3" style="display: none;">
        <h4 class="header">Your games:</h4>
        <table id="ownGames" class="clean-table light"></table>
    </div>
    <div id="welcome" class="col-sm-5">
		<h4 class="header">Welcome to V:TES Online</h4>
		<form method="post" class="light some-padding">
			<div class="form-row">
				<div class="col">
					<input type="text" class="form-control" id="dsuserin" name="dsuserin" autocomplete="username" placeholder="Username"/>
				</div>
				<div class="col">
					<input type="password" class="form-control" id="dspassin" name="dspassin" autocomplete="current-password" placeholder="Password"/>
				</div>
			</div>
			<div class="row">
				<div class="col mt-1">
					<button type="submit" id="login" name="login" value="Log in" class="btn btn-primary w-100">Log In</button>
				</div>
			</div>
		</form>
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
    <div id="globalchat" class="col-sm-6 p-sm-0" style="display: none;">
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
				<div id="newChatAlert">~ New messages ~</div>
				<input type="text" maxlength="200" id="gchat" placeholder="Chat with players"/>
			</form>
		</div>
    </div>
    <div id="register" class="col-sm-4 p-sm-0">
        <h4 class="header">Register</h4>
        <form method="post" class="light some-padding">
            <span>Register to create decks and join games!</span>

            <div class="form-group form-row mb-1">
                <label for="newplayer" class="col-3 col-form-label">Name</label>
                <div class="col-9">
                    <input type="text" class="form-control" name="newplayer" id="newplayer"/>
                </div>
            </div>
            <div class="form-group form-row mb-1">
                <label for="newpassword" class="col-3 col-form-label">Password</label>
                <div class="col-9">
                    <input type="password" class="form-control" name="newpassword" autocomplete="new-password"
                           id="newpassword"/>
                </div>
            </div>
            <div class="form-group form-row mb-1">
                <label for="newemail" class="col-3 col-form-label">Email</label>
                <div class="col-9">
                    <input type="email" class="form-control" name="newemail" autocomplete="email" id="newemail"/>
                </div>
            </div>
            <div class="form-group form-row mb-1">
                <div class="g-recaptcha"
                     data-sitekey="<%= System.getenv("JOL_RECAPTCHA_KEY") %>"></div>
            </div>
            <button type="submit" name="register" value="Register" class="btn btn-primary w-100">Register</button>
        </form>
    </div>
    <div class="col-sm-3">
        <h4 class="header">User Ranks:</h4>
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
        <h4 class="header">Links:</h4>
        <ul class="condensed-list light">
            <li>
                <a href="http://www.vekn.net/rulebook" target="_blank">V:TES Rulebook</a>
            </li>
            <li>
                <a href="https://www.facebook.com/groups/jolstatus/" target="_blank">Facebook Status Group</a>
            </li>
            <li>
                <a href="https://discord.gg/fJjac75" target="_blank">Discord Channel</a>
            </li>
            <li>
                <a href="https://amaranth.vtes.co.nz/" target="_blank">Amaranth Deck Builder</a>
            </li>

        </ul>
        <h4 class="header">Contact:</h4>
        <ul class="condensed-list light">
            <li><a href="mailto:admin@deckserver.net">Contact Site Administrator</a></li>
        </ul>
    </div>
</div>
