<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<div class="row g-2 flex-fill align-items-stretch" style="min-height: 0">
    <div class="col-md-4 col-lg-3 col-xl-3 d-flex flex-column">
        <jsp:include page="my-games.jsp"/>
    </div>
    <div class="col-md-8 col-lg-6 col-xl-6 d-flex flex-column">
        <jsp:include page="global-chat.jsp"/>
    </div>
    <div class="d-md-none d-lg-flex col-lg-3 col-xl-3 flex-column gap-2">
        <jsp:include page="online-users.jsp"/>
        <jsp:include page="resources.jsp"/>
    </div>
</div>
