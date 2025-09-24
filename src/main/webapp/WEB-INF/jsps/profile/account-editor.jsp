<div class="card shadow mb-2">
    <div class="card-header bg-body-secondary">
        <h5>Account</h5>
    </div>
    <div class="card-body">
        <label for="profileNewPassword" class="form-label">New Password</label>
        <input type="password" id="profileNewPassword" placeholder="New password" autocomplete="new-password"
               class="form-control"/>
        <label for="profileConfirmPassword" class="form-label mt-2">Confirm Password</label>
        <input type="password" id="profileConfirmPassword" placeholder="Confirm password" autocomplete="new-password"
               class="form-control"/>
        <button class="btn btn-outline-secondary btn-sm mt-2" onclick="updatePassword()">Change Password</button>
        <div id="profilePasswordError" class="mt-2"></div>
    </div>
</div>