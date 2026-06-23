<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<div class="main-layout">
    <div class="main-col-left">
        <jsp:include page="my-games.jsp"/>
    </div>
    <div class="main-col-center">
        <jsp:include page="global-chat.jsp"/>
    </div>
    <div class="main-col-right">
        <jsp:include page="online-users.jsp"/>
        <jsp:include page="resources.jsp"/>
    </div>
</div>
