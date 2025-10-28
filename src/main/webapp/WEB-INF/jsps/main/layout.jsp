<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" %>
<div class="row mt-2">
    <div class="col-md-4 col-lg-3 col-xl-3 d-flex flex-column gap-2">
        <jsp:include page="my-games.jsp"/>
        <jsp:include page="ousted-games.jsp"/>
    </div>
    <div class="col-md-8 col-lg-6 col-xl-6 mt-2 mt-md-0 d-flex flex-column gap-2">
        <jsp:include page="global-chat.jsp"/>
    </div>
    <div class="d-md-none d-lg-flex col-lg-3 col-xl-3 mt-2 mt-lg-0 flex-column flex-md-row flex-lg-column gap-2">
        <jsp:include page="online-users.jsp"/>
        <jsp:include page="links.jsp"/>
        <jsp:include page="dark-pack.jsp"/>
    </div>
</div>