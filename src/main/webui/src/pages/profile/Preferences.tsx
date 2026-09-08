import { type ChangeEvent } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Switch } from '../../components/ui/Switch';
import { FieldHint } from '../../components/ui/FormFeedback';
import { api } from '../../api/client';
import type { Profile } from '../../api/types';
import { runRequest } from '../../api/mutate';
import { useNavRefresh } from '../../auth/useNav';
import { useSave } from './saveState';
import { SaveNote } from './SaveNote';

// Appearance moved to the always-visible switcher in the top bar (see
// components/ThemePicker.tsx) — it's a server-persisted per-player preference,
// no longer a light/dark toggle.

const DEFAULT_EDGE_COLOR = '#FFFFFF';

export function Preferences({ profile, onSaved }: { profile: Profile; onSaved: (updated: Profile) => void }) {
  const edgeSave = useSave();
  // The game screen reads imageTooltipPreference off the /nav cache — refresh
  // it so a toggle here takes effect there without a reload.
  const refreshNav = useNavRefresh();
  const edgeColor = (profile.edgeColor ?? DEFAULT_EDGE_COLOR).toUpperCase();
  const isDefaultEdge = edgeColor === DEFAULT_EDGE_COLOR;

  const toggleImageTooltips = (e: ChangeEvent<HTMLInputElement>) => {
    runRequest(
      api
        .put<Profile>('/profile/preferences', {
          imageTooltips: e.target.checked,
          notificationsEnabled: profile.notificationsEnabled,
        })
        .then((updated) => {
          onSaved(updated);
          refreshNav();
        }),
      'Failed to update preference',
    );
  };

  const applyEdgeColor = (color: string) =>
    void edgeSave.run(api.put<Profile>('/profile/edge-color', { color }).then(onSaved), {
      fallbackError: 'Failed to save edge colour.',
    });

  return (
    <Card>
      <CardHeader>
        <CardTitle>Preferences</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-4" id="playerPreferences">
        <div>
          <Switch
            id="imageTooltips"
            label="Enable image tooltips"
            checked={profile.imageTooltipPreference}
            onChange={toggleImageTooltips}
          />
          <FieldHint>Hover a card name anywhere in the app to see its full card image.</FieldHint>
        </div>

        <div className="flex flex-col gap-1.5">
          <span className="text-sm text-ink">Edge marker colour</span>
          <div className="flex items-center gap-2">
            <input
              type="color"
              id="edgecolorpicker"
              aria-label="Edge marker colour"
              className="h-7 w-10 rounded border border-line bg-transparent cursor-pointer"
              value={edgeColor}
              onChange={(e) => applyEdgeColor(e.target.value)}
            />
            <span className="text-xs tabular-nums text-ink-muted">{edgeColor}</span>
            {!isDefaultEdge && (
              <button
                type="button"
                className="text-xs text-accent hover:underline"
                onClick={() => applyEdgeColor(DEFAULT_EDGE_COLOR)}
              >
                Reset
              </button>
            )}
          </div>
          <FieldHint>
            Background colour of the <span className="whitespace-nowrap">◄ Edge ►</span> marker shown on the game board
            beside whoever currently holds the Edge.
          </FieldHint>
          <SaveNote state={edgeSave.state} error={edgeSave.error} savedText="Edge colour saved." />
        </div>
      </CardBody>
    </Card>
  );
}
