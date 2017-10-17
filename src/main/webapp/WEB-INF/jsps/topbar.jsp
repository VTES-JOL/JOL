<div id="topbar">
    <table width="100%">
        <tr>
            <td width="25%" align="left">
                <h2 id="title">V:TES Online <small>v<%= System.getProperty("jol.version")%></small></h2>
            </td>
            <td width="50%" align="left">
                <div id="buttons">
                </div>
                <div id="clockdiv" style="display: none;"></div>
                <span id="chatstamp" class="btn-vtes-info"></span>
            </td>
            <td width="20%" align="right">
                <form method="post" style="display: inline;">
                    <span id="logininputs">
                        <input type="text" size=15 id="dsuserin" name="dsuserin" placeholder="Username"/>
                        <input type="password" size=15 id="dspassin" name="dspassin" placeholder="Password"/>
                    </span>
                    <span id="loggedin">
                        <span id="username"></span> is logged in.
                    </span>
                    <input type="submit" id="login" name="login" value="Log in"/>
                </form>
            </td>
        </tr>
    </table>
</div>