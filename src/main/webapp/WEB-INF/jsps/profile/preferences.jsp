<div class="card shadow mb-2" id="playerPreferences">
    <div class="card-header bg-body-secondary">
        <h5>Preferences</h5>
    </div>
    <div class="card-body">
        <div class="form-check">
            <input class="form-check-input" type="radio" name="toolTipPreferences" id="textTooltipPreference"
                   onclick="setImageTooltip(false);">
            <label class="form-check-label" for="textTooltipPreference">
                Show text tooltips
            </label>
        </div>
        <div class="form-check">
            <input class="form-check-input" type="radio" name="toolTipPreferences" id="imageTooltipPreference"
                   onclick="setImageTooltip(true);">
            <label class="form-check-label" for="imageTooltipPreference">
                Show image tooltips
            </label>
        </div>
    </div>
</div>