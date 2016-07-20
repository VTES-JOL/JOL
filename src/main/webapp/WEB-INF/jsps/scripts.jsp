<%@page import="deckserver.util.WebParams" %>
<% WebParams params = (WebParams) request.getSession().getAttribute("wparams");
    if (params == null) return;
    String prefix = params.getPrefix();

%>
<SCRIPT type="text/javascript">
    var aWindow = '';
    function getCard(game, card) // Open card text in separate window (always on top)
    {
        var URL = '<% out.write(prefix); %>card?' + card;
        openWinImpl(URL, "card");
    }
    function openWinImpl(URL, win) {
        if (!aWindow.closed && aWindow.location) {
            aWindow.location.href = URL;
        }
        else {
            aWindow = window.open(URL, win, "toolbar=yes,width=750,height=250,scrollbars=yes,menubar=no");
            if (!aWindow.opener) aWindow.opener = self;
        }
        if (window.focus) {
            aWindow.focus()
        }
    }
</SCRIPT>
