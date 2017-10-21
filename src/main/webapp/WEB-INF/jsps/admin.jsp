<div class="container">
    <div class="col col-4">
        <div class="box">
            <h4 class="header">Create Game</h4>
            <div class="light">
                <input type="text" class="full-width" name="newGameName" maxlength="30" placeholder="Game name cannot &quot; or &lsquo; characters"/>
            </div>
            <div class="footer">
                <button onclick="doCreateGame()">Create</button>
            </div>
        </div>

    </div>
    <div class="col col-4">
        <div class="box">
            <h4 class="header">Invite Players and Start Games</h4>
            <table id="currentGames" class="clean-table light"></table>
            <div class="footer">
                <label>Players:</label>
                <input id="playerList" placeholder="Start typing a player name"/>
                <select id="gameList"></select>
                <button onclick="invitePlayer()">Invite</button>
            </div>
        </div>
    </div>
    <div class="col col-4">
        <div class="box">
            <h4 class="header">Current Games</h4>
            <div class="light">
                <input id="endGameList" class="full-width" placeholder="Start typing a game name"/>
            </div>
            <div class="footer">
                <button onclick="closeGame()">End game</button>
            </div>
        </div>
    </div>
</div>