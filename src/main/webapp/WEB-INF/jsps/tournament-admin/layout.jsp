<div class="tour-admin-layout">
    <div class="tour-admin-col-left">
        <jsp:include page="tournament-list.jsp"/>
    </div>
    <div class="tour-admin-col-right">
        <div class="d-none d-flex flex-column flex-fill min-h-0" id="tourEditCol">
            <jsp:include page="tournament-admin.jsp"/>
        </div>
        <div class="d-none d-flex flex-column flex-fill min-h-0" id="tourTablesCol">
            <jsp:include page="tournament-manager.jsp"/>
        </div>
    </div>
</div>
