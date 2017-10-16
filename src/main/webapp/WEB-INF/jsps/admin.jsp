<div class="container">
    <div class="col-3">
        <div class="box">
            <h3>Create Game</h3>
            <input type="text" size="30" name="newGameName" maxlength="30"/>
            <button onclick="createGame()">Create</button>
        </div>
        <div class="box">
            <h3>Current Games</h3>
            <select id="endGameSelector"></select>
            <button onclick="closeGame()">End game</button>
        </div>
    </div>
    <div class="col-8">
        <div class="box">
            <div class="section">
                <h3>Invite Players and Start Games</h3>
                <label>Players:</label>
                <input id="playerList" size="30"/>
                <select id="gameList"></select>
                <button onclick="invitePlayer()">Invite</button>
            </div>
            <table id="currentGames" class="gamediv">
            </table>
        </div>
    </div>
</div>