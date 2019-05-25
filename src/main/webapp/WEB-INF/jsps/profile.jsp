<div class="max-width-md">
	<h4 class="header">Profile</h4>
	<form onsubmit="return false;" class="clean-table light some-more-padding">
		<div class="form-group form-row mb-1">
			<label for="profileEmail" class="col-4 col-form-label text-right">Email</label>
			<div class="col">
				<input type="email" name="email" id="profileEmail" class="form-control"/>
			</div>
		</div>
		<div class="form-group form-row mb-1">
			<label for="discordID" class="col-4 col-form-label text-right">Discord User ID</label>
			<div class="col">
				<input type="text" name="discordID" id="discordID" class="form-control"/>
				<span class="form-text text-muted">
					<a target="_blank" href="https://support.discordapp.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-">This article</a>
					explains how to get your user ID from Discord.
				</span>
			</div>
		</div>
		<div class="form-group row mb-2">
			<div class="col-4"></div>
			<div class="col">
				<div class="form-check">
					<input type="checkbox" id="pingDiscord" class="form-check-input"/>
					<label for="pingDiscord" class="form-check-label">Ping me on Discord!</label>
				</div>
			</div>
		</div>
		<div class="form-group row mb-2">
			<div class="col-4"></div>
			<div class="col">
				<button id="updateProfileButton" class="btn btn-primary" onclick="updateProfile()">Update Profile</button>
				<p id="profileUpdateResult" class="ml-1" style="font-weight:bold;display:inline"></p>
			</div>
		</div>
	</form>
</div>
<div class="max-width-md">
	<h4 class="header">Change Password</h4>
	<form onsubmit="return false;" class="clean-table light some-more-padding">
		<input hidden="true" name="username" autocomplete="username">
		<div class="form-group form-row mb-1">
			<label for="profileNewPassword" class="col-4 col-form-label text-right">New password</label>
			<div class="col">
				<input type="password" id="profileNewPassword" autocomplete="new-password" class="form-control" placeholder="Should be unique across websites"/>
			</div>
		</div>
		<div class="form-group form-row mb-2">
			<label for="profileConfirmPassword" class="col-4 col-form-label text-right">Confirm</label>
			<div class="col">
				<input type="password" id="profileConfirmPassword" autocomplete="new-password" class="form-control"/>
			</div>
		</div>
		<div class="form-group row mb-2">
			<div class="col-4"></div>
			<div class="col">
				<button class="btn btn-primary" onclick="updatePassword()">Update Password</button>
				<p id="profilePasswordError"></p>
			</div>
		</div>
	</form>
</div>
