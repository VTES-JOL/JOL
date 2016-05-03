<%@page contentType="text/html"%>
<table width=100%>
<tr>
<td width=25% align=top>
<div id="player" style="display: none;">
Your games:
<div id="owngamediv" class="gamediv">
<table class="gametable" id="owngames"  border="1" cellspacing="1" cellpadding="1" width="100%">
</table>
</div>
</div>
<div id="register">
Register for deckserver.net to create decks and join games!
<form method=post>
Name:<input type="text" size=15 name="newplayer"/>
<br />
Password:<input type="password" size=15" name="newpassword"/>
<br />
Email:<input type="text" size=30 name="newemail"/>
<br />
<input type=submit name="register" value="Register"/>
</form>
</div>
</td>
<td width=50%>
<p>
Welcome to JOL-4 beta, the latest version of Jyhad-OnLine, where you can play Vampire-The Eternal Struggle(VTES) card games online over
the web.
To play games on this server, register, login, create some decks, and use chat below to organize games.
</p>
<div id="globalchat" style="display: none;">
Now logged on: <span id="whoson"></span><br />
Admins currently on: <span id="adson"></span>
<br />Chat to organize new games: 
<div class=history id="gchatwin">
<table width=100% class="chattable" id="gchattable" cellspacing=0 cellpadding=0 border=0>
</table>
</div>
<form action="javascript: globchat();">
<span id="chatstamp"></span> Chat: <input type="text" style="width:100%" maxlength=100 id="gchat"/>
</form>
</div>
Currently active games:
<div class="gamediv">
<table id="activegames" border="1" cellspacing="1" cellpadding="1" width="100%">
</table>
</div>
</td>
<td width=20% align=top>
<div id="news">
</div>
<b>The 2009 JOL tournament is under way.<br><br>
The JOL Tournament is 3 simultaneous rounds with a
five month time limit.  Standard tournament rules apply.
Public deals only.  Normal transfers.  Players are
expected to check their games daily, or give notice to
fellow players if they will be unavailable for more than
one day.
</b>
<script type="text/javascript"><!--
google_ad_client = "pub-9814664900369745";
google_alternate_color = "000000";
google_ad_width = 180;
google_ad_height = 150;
google_ad_format = "180x150_as";
google_ad_type = "text";
google_ad_channel = "";
google_color_border = "000000";
google_color_bg = "000000";
google_color_link = "FFFF66";
google_color_text = "CC0000";
google_color_url = "FFFF66";
//-->
</script>
<script type="text/javascript"
  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script>
<script type="text/javascript"><!--
google_ad_client = "pub-9814664900369745";
google_alternate_color = "000000";
google_ad_width = 200;
google_ad_height = 90;
google_ad_format = "200x90_0ads_al";
google_ad_channel = "";
google_color_border = "000000";
google_color_bg = "000000";
google_color_link = "FFFF66";
google_color_text = "CC0000";
google_color_url = "FFFF66";
//-->
</script>
<script type="text/javascript"
  src="http://pagead2.googlesyndication.com/pagead/show_ads.js">
</script>
</td></tr></table>