<div class="container">
    <div class="col col-4">
        <h4 class="header">Update Profile</h4>
        <table class="clean-table light">
            <tr>
                <td>
                    <label>Email:</label>
                </td>
                <td>
                    <input name="email" id="profileEmail" class="full-width"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label>Receive Turn Summary:</label>
                </td>
                <td>
                    <input type="checkbox" id="profileTurnSummary"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label>Receive Ping:</label>
                </td>
                <td>
                    <input type="checkbox" id="profilePing"/>
                </td>
            </tr>
        </table>
        <div class="footer">
            <button onclick="updateProfile()">Update Profile</button>
        </div>
    </div>
    <div class="col col-4">
        <h4 class="header">Change Password</h4>
        <table class="clean-table light">
            <form onsubmit="return false;">
                <input hidden="true" name="username" autocomplete="username">
            <tr>
                <td><label>New password:</label></td>
                <td><input type="password" id="profileNewPassword" autocomplete="new-password" class="full-width" placeholder="Should be unique across websites"/></td>
            </tr>
            <tr>
                <td><label>Confirm new password:</label></td>
                <td><input type="password" id="profileConfirmPassword" autocomplete="new-password" class="full-width"/></td>
            </tr>
            </form>
        </table>
        <div class="footer">
            <button onclick="updatePassword()">Update Password</button>
            <p id="profilePasswordError"></p>
        </div>
    </div>
</div>