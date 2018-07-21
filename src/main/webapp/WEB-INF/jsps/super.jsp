<div class="row">
    <div class="col-sm-4">
        <div class="box">
            <h4 class="header">Add Role</h4>
            <div class="footer">
                <input type="text" id="allPlayersList" class="full-width"></input>
                <select id="roles">
                    <option value="admin">Prince</option>
                    <option value="judge">Justicar</option>
                    <option value="super">Inner Circle</option>
                </select>
                <button class="btn btn-primary" id="addRole" onclick="addRole()">Update</button>
            </div>
        </div>
    </div>
    <div class="col-sm-8 pl-sm-0">
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
