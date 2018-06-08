<div id="topbar" class="row">
    <div class="col">
        <h3 id="title" class="inline">V:TES Online
            <small>v<%= System.getenv("JOL_VERSION")%></small>
        </h3>
        <span id="chatstamp" class="label label-light label-basic"></span>
    </div>
    <div class="col">
        <div id="buttons">
        </div>
        <div id="clockdiv" style="display: none;"></div>
    </div>
    <div class="col">
        <form method="post" style="display: inline;">
            <span id="loginInputs">
                <input type="text" size=15 id="dsuserin" name="dsuserin" autocomplete="username" placeholder="Username"/>
                <input type="password" size=15 id="dspassin" name="dspassin" autocomplete="current-password" placeholder="Password"/>
            </span>
            <input type="submit" id="login" name="login" value="Log in"/>
        </form>
    </div>
</div>
