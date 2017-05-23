<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<div id="topbar">
    <table width="100%">
        <tr>
            <td width="25%" align="left">
                <h2 id="title">V:TES Online</h2>
            </td>
            <td width="50%" align="left">
                <div id="buttons">
                </div>
                <div id="clockdiv" style="display: none;"></div>
            </td>
            <td width="20%" align="right">
                <form method="post" style="display: inline;">
                    <span id="logininputs">
                        <label for="dsuserin">Login:</label>
                        <input type="text" size=15 id="dsuserin" name="dsuserin"/>
                        <label for="dspassin">Password:</label>
                        <input type="password" size=15 id="dspassin" name="dspassin"/>
                    </span>
                    <span id="loggedin">
                        <span id="username"></span> is logged in.
                    </span>
                    <input type="submit" class="btn-vtes-default" id="login" name="login" value="Log in"/>
                </form>
            </td>
        </tr>
    </table>
</div>