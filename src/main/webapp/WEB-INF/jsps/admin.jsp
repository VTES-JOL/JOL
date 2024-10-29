<div class="row">
    <div class="col-sm-6">
        <div>
            <label for="globalMessage">Message</label>
            <input id="globalMessage" size="50" class="full-width"/>
            <br/><br/>
            <button class="btn btn-primary" onclick="updateMessage()">Update</button>
        </div>
        <h4 class="header">Add Role</h4>
        <div>
            <label for="adminPlayerList">Players</label>
            <select id="adminPlayerList"></select>
            <label for="adminRoleList">Roles</label>
            <select id="adminRoleList">
                <option value="Judge">Judge</option>
                <option value="SuperUser">Super User</option>
                <option value="Admin">Admin</option>
            </select>
            <button onclick="addRole()" class="btn btn-primary">Add Role</button>
        </div>
        <h4 class="header">Player Roles</h4>
        <table class="clean-table light">
            <thead>
            <tr>
                <th>Name</th>
                <th>Last Online</th>
                <th>Judge</th>
                <th>Super User</th>
                <th>Admin</th>
            </tr>
            </thead>
            <tbody id="userRoles"></tbody>
        </table>
        <div class="footer">
        </div>
    </div>
    <div class="col-sm-6">
        <h4 class="header">Replace player</h4>
        <div>
            <label for="adminGameList">Games</label>
            <select id="adminGameList" onchange="adminChangeGame()"></select>
            <br/>
            <label for="adminReplacePlayerList">Player to replace:</label>
            <select id="adminReplacePlayerList"></select>
            <br/>
            <label for="adminReplacementList">Substitute</label>
            <select id="adminReplacementList"></select>
            <button onclick="replacePlayer()" class="btn btn-primary">Replace player</button>
        </div>

        <h4 class="header">Idle Games</h4>
        <table class="clean-table light">
            <thead>
            <tr>
                <th>Name</th>
                <th>Last Update</th>
                <th>Player</th>
                <th>Timestamp</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody id="idleGameList"></tbody>
        </table>

        <h4 class="header">Idle Players</h4>
        <table class="clean-table light">
            <thead>
            <tr>
                <th>Name</th>
                <th>Last Online</th>
                <th>Legacy Decks</th>
                <th>Modern Decks</th>
                <th>Delete</th>
            </tr>
            </thead>
            <tbody id="deletePlayerList"></tbody>
        </table>
    </div>
</div>
