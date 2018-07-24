<div class="row">
    <div class="col-sm-4">
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
<div id="userAdmins" class="clearfix">
</div>

<button id="userRoleTemplate" type="button" class="list-group-item list-group-item-action list-group-item-light d-none">
	<input type="checkbox" checked/>
	<span>Role</span>
</button>

<div id="userAdminTemplate" class="user-card card d-none">
	<div class="card-header">Name</div>
	<div class="list-group list-group-flush">
	</div>
	<div class="card-footer text-muted">
		Last online
		<br/><span>XX-MMM-YYYY 00:00 MST</span>
	</div>
</div>
