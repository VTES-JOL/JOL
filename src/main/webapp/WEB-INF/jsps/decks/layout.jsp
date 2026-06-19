<div class="row g-2 flex-fill align-items-stretch" style="min-height: 0">
    <div class="col-md-4 col-lg-3 d-flex flex-column" style="min-height: 0">
        <jsp:include page="deck-list.jsp"/>
    </div>
    <div class="col-md-8 col-lg-9 d-flex flex-column" style="min-height: 0">
        <div class="d-none d-flex flex-column flex-fill" id="deckEditorCol" style="min-height: 0">
            <jsp:include page="deck-editor.jsp"/>
        </div>
        <div class="d-flex flex-column flex-fill" id="deckPreviewCol" style="min-height: 0">
            <jsp:include page="deck-preview.jsp"/>
        </div>
    </div>
</div>
