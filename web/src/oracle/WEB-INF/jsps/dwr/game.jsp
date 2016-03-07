<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<TABLE width=100% border=2>
   <TR>
    <TD valign=top WIDTH="30%" id="hand"/>
    <td id="dsForm"><form onsubmit="javascript:return dosubmit();">
     <table>
      <tr id="phasecommand"><td>Phase:</td><td><select id="phase" name="phase"/></td></tr>
      <tr><td>Command:</td>
         <td><input name="command" type="text" id="command" size=25 maxlength=100/></td>
      </tr>
      <tr><td>Chat:</td>
      <td><input name="chat" type="text" id="chat" size=25 maxlength=100/>
      </td></tr>
      <tr><td>Ping: </td><td><select id="ping" name="ping"/></td></tr>
      <tr id="endcommand"><td>End turn?</td><td><select id="endturn" name="endturn"><option value="No">No</option><option value="Yes">Yes</option></select></td></tr>
      <tr><td><input name="Submit" type="submit" value="Submit"/></td></tr>
      <tr><td colspan="2"><span id="status"/></td></tr>
     </table>
    </form></td>
   <td colspan=2 valign=top>Global notes and pending actions: <br />
     <textarea rows="4" cols="50" name="global" id="global"></textarea><br />
     <div id="playerPad">
      Private notepad:<br>
     <textarea rows="4" cols="50" name="notes" id="notes"></textarea>
     </div>
   </td>
  </tr>
<tr>
<td align=left><font color=yellow><table><tr><td align=left><div id="gamename"></div></td><td align=right><div id="gamestamp"></div></tr></table></font></td>
<td align=center><font color=white>Current Turn: <span id="turnlabel"/>.</td>
<td align=left><a name="cd"><select id="cards" name="cards" onchange="selectCard()"></a>
    <option value="NOCARD">Show history</option>
  </select></td>
<td align=right>Turn selector:<select id="turns" name="turns" onchange="getHistory()" /></td>
</tr>
   <tr>
    <TD colspan="2" width="60%"><div class="history" id="curturn"><table class="chattable" cellspacing=0 cellpadding=0 border=0 id="curturntable"></table> </div></TD>
    <td colspan="2"><div class="history" id="extra"><input type=hidden id="extraSelect" value="history"/><div id="history"/></div></td>
   </TR>
   <TR>
    <TD colspan="4">
      <span id="state"/>
    </TD>
   </TR>
  </TABLE>