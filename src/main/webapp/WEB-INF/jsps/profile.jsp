<div class="max-width-md">
    <form onsubmit="return false;">
        <h2>Profile</h2>
        <label for="profileEmail">E-mail Address</label>
        <input type="email" name="email" id="profileEmail" class="form-control"/>

        <h3 class="mt-3">Discord Ping</h3>
        <p class="px-1">
            Link your account below to receive pings in Discord.  Install the Discord app and enable push notifications to receive pings on your phone.
            <i>Pro tip: </i> Disable sound notifications for the Discord app to receive the visual banners without the pestering dings or vibrations.
        </p>
        <label for="discordID">Discord User ID</label>
        <input type="text" name="discordID" id="discordID" class="form-control"/>
        <span class="form-text text-muted">
            <a target="_blank" href="https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-">This article</a>
            explains how to get your user ID from Discord.
        </span>
        <button id="updateProfileButton" class="btn btn-primary btn-block" onclick="updateProfile()">Update Profile</button>
        <p id="profileUpdateResult" class="ml-1" style="font-weight:bold;display:inline"></p>
    </form>
</div>
<div class="max-width-md">
    <form onsubmit="return false;">
        <h2>Password</h2>
        <input hidden="true" name="username" autocomplete="username">
        <input type="password" id="profileNewPassword" placeholder="New password"
            autocomplete="new-password" class="form-control mb-2"/>
        <input type="password" id="profileConfirmPassword" placeholder="Confirm password"
            autocomplete="new-password" class="form-control mb-2"/>
        <button class="btn btn-primary btn-block" onclick="updatePassword()">Change Password</button>
        <p id="profilePasswordError" style="min-height:2rem;"></p>
    </form>
</div>
