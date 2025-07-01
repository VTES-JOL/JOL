<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="net.deckserver.dwr.model.JolAdmin" %>
<%@ page import="net.deckserver.game.ui.state.CardDetail" %>
<html>
<head>
  <title>V:TES Online</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/bootstrap.min.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
  <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/styles.css?version=<%= JolAdmin.INSTANCE.getVersion() %>"/>
  <link href="https://fonts.googleapis.com/css?family=IM+Fell+English" rel="stylesheet">
</head>
<style>
  .test-container {
    display: grid;
    grid-template-columns: 50% 50%;
  }

  .vtes-card {
    container: vtes-card / inline-size;
    display: inline-block;
  }

  h3, h4 {
    color: silver;
  }

</style>
<%
  CardDetail vampire = new CardDetail("1", "Khadija Al-Kindi", "201599", "ShanDow");
  CardDetail ally = new CardDetail("2", "Renegade Garou", "101602", "ShanDow");
  CardDetail imbued = new CardDetail("3", "Inez \"Nurse216\" Villagrande", "200632", "ShanDow");
  CardDetail location = new CardDetail("4", "Academic Hunting Ground", "100015", "ShanDow");
  CardDetail master = new CardDetail("5", "Dreams of the Sphinx", "100588", "ShanDow");
  master.setLocked(true);
  request.setAttribute("detail", vampire);
  request.setAttribute("ally", ally);
  request.setAttribute("imbued", imbued);
  request.setAttribute("location", location);
  request.setAttribute("master", master);
%>
<body style="background-image: url('images/grass.jpg'); background-size: cover">
<div class="test-container p-2">
  <div id="player-container">
    <h3>Player</h3>
    <h4>Ready Region</h4>
    <div class="region">
      <jsp:include page="/WEB-INF/jsps/card/card.jsp">
        <jsp:param name="detail" value="detail"/>
        <jsp:param name="visible" value="true"/>
      </jsp:include>
      <jsp:include page="/WEB-INF/jsps/card/card.jsp">
        <jsp:param name="detail" value="ally"/>
        <jsp:param name="visible" value="true"/>
      </jsp:include>
      <jsp:include page="/WEB-INF/jsps/card/card.jsp">
        <jsp:param name="detail" value="master"/>
        <jsp:param name="visible" value="true"/>
      </jsp:include>
      <jsp:include page="/WEB-INF/jsps/card/card.jsp">
        <jsp:param name="detail" value="imbued"/>
        <jsp:param name="visible" value="true"/>
      </jsp:include>
      <jsp:include page="/WEB-INF/jsps/card/card.jsp">
        <jsp:param name="detail" value="location"/>
        <jsp:param name="visible" value="true"/>
      </jsp:include>
    </div>
    <h4>Uncontrolled Region</h4>
    <h4>Ash-heap / Hand</h4>
    <h4>Library / Crypt</h4>
  </div>
  <div id="other-container">
    <h3>Other</h3>
    <h4>Ready Region</h4>
    <h4>Uncontrolled Region</h4>
    <h4>Ash-heap / Hand</h4>
    <h4>Library / Crypt</h4>
  </div>
</div>
<script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
</body>
</html>
