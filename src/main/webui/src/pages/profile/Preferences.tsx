import { type ChangeEvent } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Switch } from '../../components/ui/Switch';
import { FieldHint } from '../../components/ui/FormFeedback';
import { api } from '../../api/client';
import type { Profile } from '../../api/types';
import { runRequest } from '../../api/mutate';
import { setThemePref, useThemePref, type ThemePref } from '../../theme';
import { useSave } from './saveState';
import { SaveNote } from './SaveNote';

const THEME_OPTIONS: { value: ThemePref; label: string }[] = [
  { value: 'system', label: 'System' },
  { value: 'light', label: 'Light' },
  { value: 'dark', label: 'Dark' },
];

const DEFAULT_EDGE_COLOR = '#FFFFFF';

function AppearanceControl() {
  const pref = useThemePref();
  return (
    <div className="flex flex-col gap-1.5">
      <span className="text-sm text-ink">Appearance</span>
      <div className="inline-flex w-fit overflow-hidden rounded border border-line-accent" role="group" aria-label="Appearance">
        {THEME_OPTIONS.map((option, i) => (
          <button
            key={option.value}
            type="button"
            aria-pressed={pref === option.value}
            onClick={() => setThemePref(option.value)}
            className={`px-3 py-1 text-xs transition-colors cursor-pointer ${
              i > 0 ? 'border-l border-line-accent' : ''
            } ${
              pref === option.value
                ? 'bg-accent text-surface'
                : 'text-ink-secondary hover:bg-hover hover:text-ink'
            }`}
          >
            {option.label}
          </button>
        ))}
      </div>
    </div>
  );
}

export function Preferences({ profile, onSaved }: { profile: Profile; onSaved: (updated: Profile) => void }) {
  const edgeSave = useSave();
  const edgeColor = (profile.edgeColor ?? DEFAULT_EDGE_COLOR).toUpperCase();
  const isDefaultEdge = edgeColor === DEFAULT_EDGE_COLOR;

  const toggleImageTooltips = (e: ChangeEvent<HTMLInputElement>) => {
    runRequest(
      api
        .put<Profile>('/profile/preferences', {
          imageTooltips: e.target.checked,
          notificationsEnabled: profile.notificationsEnabled,
        })
        .then(onSaved),
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
        <AppearanceControl />

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
