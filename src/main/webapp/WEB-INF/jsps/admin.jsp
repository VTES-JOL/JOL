<div class="row">
    <div class="col-sm-6">
        <div>
            <h4 class="header">Create Game</h4>
            <div class="light">
                <input type="text" class="full-width" id="newGameName" name="newGameName" maxlength="60"
                       placeholder="Game name cannot &quot; or &lsquo; characters"/>
                Make this game public? <input type="checkbox" name="publicFlag" id="publicFlag">
            </div>
            <div class="footer">
                <button class="btn btn-primary" onclick="doCreateGame()">Create Game</button>
            </div>
        </div>
        <div>
            <h4 class="header">My Games</h4>
            <table id="currentGames" class="clean-table light games">
            </table>
            <div class="footer">
                <label>Players:</label>
                <input id="playerList" placeholder="Start typing a player name"/>
                <select id="myGameList"></select>
                <button class="btn btn-primary" onclick="invitePlayer()">Invite</button>
            </div>
        </div>
    </div>
    <div class="col-sm-6">
        <div>
            <h4 class="header">Public Games</h4>
            <table id="publicGames" class="clean-table light games"></table>
        </div>

        <div>
            <h4 class="header">Register Deck</h4>
            <table class="light clean-table games" id="invitedGames"></table>
            <div class="footer">
                <select id="invitedGamesList"></select>
                <select id="mydeckList"></select>
                <button class="btn btn-primary" onclick="doRegister()">Register</button>
            </div>
        </div>
    </div>
</div>
