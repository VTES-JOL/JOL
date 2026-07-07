<div class="p-3 border-bottom">
    <div class="d-flex gap-2 align-items-end">
        <div class="flex-grow-1">
            <label for="adminPlayerList" class="form-label mb-1">Player</label>
            <select id="adminPlayerList" class="form-select form-select-sm"></select>
        </div>
        <div class="flex-grow-1">
            <label for="adminRoleList" class="form-label mb-1">Role</label>
            <select id="adminRoleList" class="form-select form-select-sm">
                <option value="JUDGE">Judge</option>
                <option value="SUPER_USER">Super User</option>
                <option value="ADMIN">Admin</option>
                <option value="TOURNAMENT_ADMIN">Tournament Admin</option>
                <option value="PLAYTESTER">Playtester</option>
            </select>
        </div>
        <button onclick="addRole()" class="btn btn-outline-secondary btn-sm">Add</button>
    </div>
</div>
<div class="scrollable" style="overflow-x:auto; max-height:70dvh;">
    <table class="table table-sm table-hover mb-0">
        <thead>
        <tr>
            <th>Name</th>
            <th>Last Online</th>
            <th>Roles</th>
        </tr>
        </thead>
        <tbody id="userRoles"></tbody>
    </table>
</div>
