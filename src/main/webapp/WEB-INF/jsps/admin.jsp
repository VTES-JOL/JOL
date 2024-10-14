<div class="row">
    <div class="col-sm-6">
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

    </div>
</div>
