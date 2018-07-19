<div class="container">
    <div class="col col-4">
        <div class="box">
            <h4 class="header">Add Role</h4>
            <div class="footer">
                <input id="allPlayersList" size="40"></input>
                <select id="roles">
                    <option value="admin">Prince</option>
                    <option value="judge">Justicar</option>
                    <option value="super">Inner Circle</option>
                </select>
                <button id="addRole" onclick="addRole()">Update</button>
            </div>
        </div>
    </div>
    <div class="col col-8">
        <div class="box">
            <h4 class="header">Players</h4>
            <table class="clean-table light">
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Last Online</th>
                    <th>Admin</th>
                    <th>Super User</th>
                    <th>Judge</th>
                    <th></th>
                </tr>
                </thead>
                <tbody id="adminPlayerList"></tbody>
            </table>
        </div>
    </div>
</div>