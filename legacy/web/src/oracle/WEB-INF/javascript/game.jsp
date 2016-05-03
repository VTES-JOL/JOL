<%@page import="deckserver.util.*"%>
<% WebParams params = (WebParams) request.getSession().getAttribute("wparams");
   String prefix = params.getPrefix();
   
   %>
  <SCRIPT LANGUAGE="JavaScript">
   <!-- Hide from older browsers
   var aWindow = '';
   function openWin(card) // Open card text in separate window (always on top)
   {
    var URL = '<% out.write(prefix); %>card?' + card;
    openWinImpl(URL,"card");
   }
   function openWinImpl(URL,win)
   {
    if (!aWindow.closed && aWindow.location)
    {
     aWindow.location.href = URL;
    }
    else
    {
     aWindow=window.open(URL,win,"toolbar=yes,width=750,height=250,scrollbars=yes,menubar=no");
     if (!aWindow.opener) aWindow.opener = self;
    }
    if (window.focus) {aWindow.focus()}
   }
  // var tWindow = '';
   function openTurnWin() // Open old turn in separate window (always on top)
   {
    var turn = document.getElementById("oldturns").value;
    var URL = '<% out.write(prefix); %>turn?' + turn;
    openWinImpl(URL,"turn");
   }
   function openHelpWin() // Open command help window
   {
    var URL = '/doc/commands.html';
    openWinImpl(URL,"help");
   }
   function details(thistag) // Toggle region details on/off
   {
    var region = document.getElementById("region" + thistag);
    if (region.style.display=='none') // Details not displayed
    {
     region.style.display = ''; // Show details
     document.getElementById(thistag).innerHTML="-";
    } 
    else // Details already displayed
    {
     region.style.display = 'none'; // Hide details
     document.getElementById(thistag).innerHTML="+";
    }
   }
   function collapse() // Hide all region details
   {
    for(i=1; i<6; i++) // For each player (1-5)
    {
	//var region = document.getElementById("regionr" + i);
	//document.getElementById("r"+i).innerHTML="+";
	//region.style.display = 'none'; // Hide ready region details
        var region = document.getElementById("regiont" + i);
	document.getElementById("t"+i).innerHTML="+";
	region.style.display = 'none'; // Hide topor region details
	region = document.getElementById("regioni" + i);
	document.getElementById("i"+i).innerHTML="+";
	region.style.display = 'none'; // Hide inactive region details
	region = document.getElementById("regiona" + i);
	document.getElementById("a"+i).innerHTML="+";
	region.style.display = 'none'; // Hide ashheap region details
    }
   }
   // Stop hiding from older browsers -->
  </SCRIPT>