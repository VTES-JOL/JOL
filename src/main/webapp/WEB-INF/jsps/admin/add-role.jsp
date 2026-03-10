<div class="card shadow mt-2">
    <div class="card-header bg-body-secondary">
        <h5>Add Role</h5>
    </div>
    <div class="card-body">
        <label for="adminPlayerList" class="form-label">Players</label>
        <select id="adminPlayerList" class="form-select"></select>
        <label for="adminRoleList" class="form-label">Roles</label>
        <select id="adminRoleList" class="form-select">
            <option value="JUDGE">Judge</option>
            <option value="SUPER_USER">Super User</option>
            <option value="ADMIN">Admin</option>
            <option value="TOURNAMENT_ADMIN">Tournament Admin</option>
            <option value="PLAYTESTER">Playtester</option>
        </select>
        <button onclick="addRole()" class="btn btn-outline-secondary btn-sm mt-2">Add Role</button>
    </div>
</div>