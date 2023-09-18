<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<div class="row">
	<div class="col">
		<div id="messages" style="display: none;"></div>
	</div>
</div>
<div class="row">
	<div id="player" class="col-sm-3">
		<h4 class="header">Your games:</h4>
		<table id="ownGames" class="clean-table light"></table>
	</div>
	<div id="globalchat" class="col-sm-6 p-sm-0">
    <div id="chatLog"></div>
		<div>
			<h4 class="header">Global Chat:</h4>
			<div id="globalChatOutput" class="scrollable side-padded"></div>
			<form id="globalChatForm" action="javascript:doGlobalChat();" autocomplete='off'>
				<div id="newChatAlert">~ New messages ~</div>
				<input type="text" maxlength="200" id="gchat" placeholder="Chat with players"/>
			</form>
		</div>
	</div>
	<div class="col-sm-3">
		<div id="onlineUsers">
			<h4 class="header">Online Users:</h4>
			<div id="whoson" class="light some-padding"></div>
			<div class="footer">
				<a data-toggle="collapse" href="#userRanks" aria-expanded="false" aria-controls="userRanks">User Ranks</a>
				<div id="userRanks" class="collapse">
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
				</div>
			</div>
		</div>
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
			<li>
				<a href="https://codex-of-the-damned.org/" target="_blank">Codex of the Damned</a>
				<div style="margin-left:1em;font-size:0.9em">Rulings and strategy</div>
			</li>
		</ul>
		<h4 class="header">Contact:</h4>
		<ul class="condensed-list light">
			<li><a href="mailto:admin@deckserver.net">Contact Site Administrator</a></li>
		</ul>
	</div>
</div>
