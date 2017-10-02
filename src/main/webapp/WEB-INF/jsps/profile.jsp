<div class="container">
    <div class="col-4">
        <div>
            <h3>Update Profile</h3>
            <table>
                <tr>
                    <td>
                        <label>Email:</label>
                    </td>
                    <td>
                        <input name="email" id="profileEmail" size="40"/>
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
                <tr>
                    <td colspan="2">
                        <button onclick="updateProfile()">Update Profile</button>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <div class="col-4">
        <div>
            <h3>Change Password</h3>
            <table>
                <tr>
                    <td><label>Password:</label></td>
                    <td><input type="password" id="profileNewPassword" size="40"/></td>
                </tr>
                <tr>
                    <td><label>Confirm Password:</label></td>
                    <td><input type="password" id="profileConfirmPassword" size="40"/></td>
                </tr>
                <tr>
                    <td colspan="2"><button onclick="updatePassword()">Update Password</button> </td>
                </tr>
            </table>
            <p id="profilePasswordError"></p>
        </div>
    </div>
</div>