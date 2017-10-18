<div class="container">
    <div class="col-3">
        <div class="box">
            <h4>Create Game</h4>
            <input type="text" size="30" name="newGameName" maxlength="30"/>
            <button onclick="createGame()">Create</button>
        </div>
        <div class="box">
            <h4>Current Games</h4>
            <select id="endGameSelector"></select>
            <button onclick="closeGame()">End game</button>
        </div>
    </div>
    <div class="col-8">
        <div class="box">
            <div class="section">
                <h4>Invite Players and Start Games</h4>
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