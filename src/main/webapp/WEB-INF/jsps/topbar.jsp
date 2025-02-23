<nav class="navbar navbar-expand-lg bg-dark px-2" id="navbar" data-bs-theme="dark">
    <a class="navbar-brand" href="/jol/" onclick="return doNav('main');">
        <span id="titleLink">V:TES Online</span>
    </a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNavAltMarkup"
            aria-controls="navbarNavAltMarkup" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarNavAltMarkup">
        <div id="gameButtonsNav" class="navbar-nav">
            <div class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" id="myGamesLink" role="button"
                   data-bs-toggle="dropdown" aria-expanded="false">My Games</a>
                <ul id="gameButtons" class="dropdown-menu" aria-labelledby="myGamesLink">
                </ul>
            </div>
        </div>
        <div id="buttons" class="navbar-nav"></div>
        <div id="logout" class="navbar-nav">
            <form method="post" class="form-inline" action="/jol/logout">
                <button type="submit" name="logout" value="logout" class="btn btn-link nav-item nav-link">Log Out
                </button>
            </form>
        </div>
    </div>
    <span id="connectionMessage" class="navbar-text text-warning">Connection issue. Retrying...</span>
</nav>
