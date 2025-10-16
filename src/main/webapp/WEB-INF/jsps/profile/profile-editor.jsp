<%@ page import="net.deckserver.services.CountryService" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="card shadow mb-2">
    <div class="card-header bg-body-secondary">
        <h5>Profile</h5>
    </div>
    <div class="card-body">
        <label for="profileEmail" class="form-label">E-mail Address</label>
        <input type="email" name="email" id="profileEmail" class="form-control" autocomplete="email"/>

        <label for="profileCountry" class="form-label">Country</label>
        <select name="profileCountry" id="profileCountry" class="form-select">
            <option value="">-- Don't display country --</option>
            <c:forEach var="country" items="<%= CountryService.getCountryCodes() %>">
                <option value="${country}">${CountryService.getCountry(country)}</option>
            </c:forEach>
        </select>

        <label for="veknID" class="form-label mt-2">VEKN ID</label>
        <input type="text" name="veknID" id="veknID" class="form-control" inputmode="numeric" pattern="[0-9]*" aria-describedby="veknIdHelp"/>
        <div class="form-text" id="veknIdHelp">
            Link your account to your VEKN ID in order to be able to play sanctioned tournaments.
        </div>

        <label for="discordID" class="form-label mt-2">Discord User ID</label>
        <input type="text" name="discordID" id="discordID" class="form-control"
               inputmode="numeric" pattern="[0-9]*" aria-describedby="discordIdHelp"/>
        <div class="form-text" id="discordIdHelp">
            Link your account below to receive pings in Discord. Install the Discord app and enable push notifications
            to receive pings on your phone.
            <i>Pro tip: </i> Disable sound notifications for the Discord app to receive the visual banners without the
            pestering dings or vibrations.
            <a target="_blank"
               href="https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-">This
                article</a>
            explains how to get your user ID from Discord.
        </div>

        <script>
            (function () {
                function enforceDigitsOnly(el) {
                    el.addEventListener('input', function () {
                        const v = el.value;
                        const digits = v.replace(/\D+/g, '');
                        if (v !== digits) el.value = digits;
                    });
                }

                ['veknID', 'discordID'].forEach(function (id) {
                    const el = document.getElementById(id);
                    if (el) enforceDigitsOnly(el);
                });
            })();
        </script>
        <button id="updateProfileButton" class="btn btn-outline-secondary btn-sm mt-2" onclick="updateProfile()">Update
            Profile
        </button>
        <div id="profileUpdateResult" class="mt-2"></div>
    </div>
</div>