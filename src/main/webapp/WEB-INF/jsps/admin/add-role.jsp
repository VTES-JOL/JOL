<div class="card shadow mt-2">
  <div class="card-header bg-body-secondary">
    <h5>Add Role</h5>
  </div>
  <div class="card-body">
    <label for="adminPlayerList" class="form-label">Players</label>
    <select id="adminPlayerList" class="form-select"></select>
    <label for="adminRoleList" class="form-label">Roles</label>
    <select id="adminRoleList" class="form-select">
      <option value="Judge">Judge</option>
      <option value="SuperUser">Super User</option>
      <option value="Admin">Admin</option>
    </select>
    <button onclick="addRole()" class="btn btn-outline-secondary btn-sm mt-2">Add Role</button>
  </div>
</div>