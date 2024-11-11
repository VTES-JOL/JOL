<div class="card shadow mb-2">
    <div class="card-header bg-body-secondary">
        <h5>Profile</h5>
    </div>
    <div class="card-body">
        <label for="profileEmail" class="form-label">E-mail Address</label>
        <input type="email" name="email" id="profileEmail" class="form-control" autocomplete="email"/>
        <label for="veknID" class="form-label mt-2">VEKN ID</label>
        <input type="text" name="veknID" id="veknID" class="form-control"/>
        <div class="form-text">
            Link your account to your VEKN ID in order to be able to play sanctioned tournaments.
        </div>
        <label for="discordID" class="form-label mt-2">Discord User ID</label>
        <input type="text" name="discordID" id="discordID" class="form-control"/>
        <div class="form-text">
            Link your account below to receive pings in Discord. Install the Discord app and enable push notifications
            to receive pings on your phone.
            <i>Pro tip: </i> Disable sound notifications for the Discord app to receive the visual banners without the
            pestering dings or vibrations.
            <a target="_blank"
               href="https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-">This
                article</a>
            explains how to get your user ID from Discord.
        </div>
        <button id="updateProfileButton" class="btn btn-outline-secondary btn-sm mt-2" onclick="updateProfile()">Update
            Profile
        </button>
        <div id="profileUpdateResult" class="mt-2"></div>
    </div>
</div>