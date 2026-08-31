import { useState, type ChangeEvent } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Switch } from '../../components/ui/Switch';
import { api } from '../../api/client';
import type { Profile } from '../../api/types';
import { isPushSupported, subscribeToPush, unsubscribeFromPush } from '../../push/pushNotifications';
import { alertDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';

export function Preferences({ profile, onSaved }: { profile: Profile; onSaved: (updated: Profile) => void }) {
  const [notificationsBusy, setNotificationsBusy] = useState(false);
  const pushSupported = isPushSupported();

  const setUserPreferences = (imageTooltips: boolean, notificationsEnabled: boolean) =>
    api.put<Profile>('/profile/preferences', { imageTooltips, notificationsEnabled }).then(onSaved);

  const toggleImageTooltips = (e: ChangeEvent<HTMLInputElement>) => {
    setUserPreferences(e.target.checked, profile.notificationsEnabled);
  };

  const setEdgeColor = (e: ChangeEvent<HTMLInputElement>) => {
    api.put<Profile>('/profile/edge-color', { color: e.target.value }).then(onSaved);
  };

  const toggleNotifications = async (e: ChangeEvent<HTMLInputElement>) => {
    const enabling = e.target.checked;
    if (enabling) {
      if (Notification.permission === 'denied') {
        await alertDialog('Notifications are blocked for this site in your browser settings.');
        return;
      }
      setNotificationsBusy(true);
      runRequest(
        Notification.requestPermission().then((permission) => {
          if (permission !== 'granted') return;
          return subscribeToPush().then(() => setUserPreferences(profile.imageTooltipPreference, true));
        }),
        'Unable to subscribe to push notifications',
      ).finally(() => setNotificationsBusy(false));
    } else {
      setNotificationsBusy(true);
      unsubscribeFromPush()
        .finally(() => setUserPreferences(profile.imageTooltipPreference, false))
        .finally(() => setNotificationsBusy(false));
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Preferences</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-3" id="playerPreferences">
        <Switch
          id="imageTooltips"
          label="Enable Image tooltips"
          checked={profile.imageTooltipPreference}
          onChange={toggleImageTooltips}
        />

        <div className="flex items-center gap-2">
          <input
            type="color"
            id="edgecolorpicker"
            className="h-7 w-10 rounded border border-line bg-transparent cursor-pointer"
            value={profile.edgeColor ?? '#000000'}
            onChange={setEdgeColor}
          />
          <label htmlFor="edgecolorpicker" className="text-sm text-ink">
            Choose Edge Color
          </label>
        </div>

        <Switch
          id="enableNotifications"
          label="Enable notifications"
          checked={profile.notificationsEnabled}
          disabled={!pushSupported || notificationsBusy}
          title={pushSupported ? undefined : 'Notifications are not supported in this browser.'}
          onChange={toggleNotifications}
        />
      </CardBody>
    </Card>
  );
}
