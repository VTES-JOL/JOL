<div class="deck-layout">
    <div class="deck-col-left">
        <jsp:include page="deck-list.jsp"/>
    </div>
    <div class="deck-col-right">
        <div class="d-none d-flex flex-column flex-fill min-h-0" id="deckEditorCol">
            <jsp:include page="deck-editor.jsp"/>
        </div>
        <div class="d-flex flex-column flex-fill min-h-0" id="deckPreviewCol">
            <jsp:include page="deck-preview.jsp"/>
        </div>
    </div>
</div>
