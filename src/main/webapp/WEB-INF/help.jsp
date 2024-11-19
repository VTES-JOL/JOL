<%@ page import="net.deckserver.dwr.model.JolAdmin" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <title>V:TES Online</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"/>
  <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css"/>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
  <link rel="stylesheet" type="text/css" href="css/styles.css?version=<%= JolAdmin.INSTANCE.getVersion() %>"/>
  <link rel="stylesheet" type="text/css" href="css/jquery-ui.min.css"/>
  <link rel="stylesheet" type="text/css" href="css/jquery-ui.structure.min.css"/>
  <link rel="stylesheet" type="text/css" href="css/jquery-ui.theme.min.css"/>
  <link rel="stylesheet" type="text/css" href="css/light.css"/>
  <link rel="shortcut icon" href="images/favicon.ico"/>
  <link href="https://fonts.googleapis.com/css?family=IM+Fell+English" rel="stylesheet">
</head>
<body>
  <jsp:include page="WEB-INF/jsps/help/layout.jsp"/>
  <script src="js/jquery-3.7.1.min.js"></script>
  <script src="js/jquery-throttle.min.js"></script>
  <script src="js/jquery-ui.min.js"></script>
  <script src='js/moment-with-locales.min.js'></script>
  <script src='js/moment-timezone-with-data.min.js'></script>
  <script src="js/popper.min.js"></script>
  <script src="js/tippy.all.min.js"></script>
  <script src="js/bootstrap.min.js"></script>
</body>
</html>
