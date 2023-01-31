<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
	<a class="navbar-brand" href="/jol/" name="top" onclick="return doNav('main');">
		<span id="titleLink">V:TES Online</span>
		<sub style="font-size:.5em"><%= System.getenv("JOL_VERSION") %></sub>
	</a>
	<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavAltMarkup" aria-controls="navbarNavAltMarkup" aria-expanded="false" aria-label="Toggle navigation">
		<span class="navbar-toggler-icon"></span>
	</button>
	<div class="collapse navbar-collapse" id="navbarNavAltMarkup">
		<div id="gameButtonsNav" class="navbar-nav">
			<div class="nav-item dropdown">
				<a class="nav-link dropdown-toggle" href="#" id="myGamesLink" role="button"
				   data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">My Games</a>
				<div id="gameButtons" class="dropdown-menu" aria-labelledby="myGamesLink">
				</div>
			</div>
		</div>
		<div id="buttons" class="navbar-nav"></div>
		<div id="logout" class="navbar-nav">
			<form method="post" class="form-inline" action="/jol/logout">
				<button type="submit" name="logout" value="logout" class="btn btn-link nav-item nav-link">Log Out</button>
			</form>
		</div>
	</div>
</nav>
