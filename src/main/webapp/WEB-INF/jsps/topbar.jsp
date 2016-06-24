<%@page contentType="text/html" %>
<%@page pageEncoding="UTF-8" %>
<div id="topbar">
    <table width="100%">
        <tr>
            <td align="left">
                <div id="title">Jyhad On-Line (JOL)</div>
            </td>
            <td align="center">
                <div id="buttons"></div>
            </td>
            <td align="right">
                <form method="post">
                    <span id="logininputs">
                        <label for="dsuserin">Login:</label>
                        <input type="text" size=15 id="dsuserin" name="dsuserin"/>
                        <label for="dspassin">Password:</label>
                        <input type="password" size=15 id="dspassin" name="dspassin"/>
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