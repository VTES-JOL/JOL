<div class="row">
    <div class="col-sm">
        <h4 class="header">Update Profile</h4>
		<form onsubmit="return false;" class="clean-table light some-more-padding">
			<div class="form-group form-row mb-1">
				<label for="profileEmail" class="col-2 col-form-label mr-2">Email:</label>
				<div class="col">
					<input type="email" name="email" id="profileEmail" class="form-control"/>
				</div>
			</div>
			<div class="form-group row mb-1">
				<div class="col">
					<div class="form-check">
						<input type="checkbox" id="profileTurnSummary" class="form-check-input"/>
						<label for="profileTurnSummary" class="form-check-label">Receive Turn Summary</label>
					</div>
				</div>
			</div>
			<div class="form-group row mb-2">
				<div class="col">
					<div class="form-check">
						<input type="checkbox" id="profilePing" class="form-check-input"/>
						<label for="profilePing" class="form-check-label">Receive Ping</label>
					</div>
				</div>
			</div>
			<button class="btn btn-primary" onclick="updateProfile()">Update Profile</button>
		</form>
    </div>
    <div class="col-sm pl-sm-0">
        <h4 class="header">Change Password</h4>
		<form onsubmit="return false;" class="clean-table light some-more-padding">
			<input hidden="true" name="username" autocomplete="username">
			<div class="form-group form-row mb-1">
                <label for="profileNewPassword" class="col-5 col-form-label">New password:</label>
				<div class="col">
					<input type="password" id="profileNewPassword" autocomplete="new-password" class="form-control" placeholder="Should be unique across websites"/>
				</div>
			</div>
			<div class="form-group form-row mb-2">
                <label for="profileConfirmPassword" class="col-5 col-form-label">Confirm:</label>
				<div class="col">
					<input type="password" id="profileConfirmPassword" autocomplete="new-password" class="form-control"/>
				</div>
			</div>
            <button class="btn btn-primary" onclick="updatePassword()">Update Password</button>
            <p id="profilePasswordError"></p>
		</form>
    </div>
</div>
