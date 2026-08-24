import { useState, type ChangeEvent } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import type { Profile } from '../../api/types';
import { isPushSupported, subscribeToPush, unsubscribeFromPush } from '../../push/pushNotifications';
import { alertDialog } from '../../components/dialog';
import { showError } from '../../components/toast';

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
      Notification.requestPermission()
        .then((permission) => {
          if (permission !== 'granted') return;
          return subscribeToPush().then(() => setUserPreferences(profile.imageTooltipPreference, true));
        })
        .catch((err) => {
          console.error('Unable to subscribe to push notifications', err);
          showError('Unable to subscribe to push notifications.');
        })
        .finally(() => setNotificationsBusy(false));
    } else {
      setNotificationsBusy(true);
      unsubscribeFromPush()
        .finally(() => setUserPreferences(profile.imageTooltipPreference, false))
        .finally(() => setNotificationsBusy(false));
    }
  };

  return (
    <Card className="mb-2" style={{ overflow: 'visible' }}>
      <CardHeader>
        <CardTitle>Preferences</CardTitle>
      </CardHeader>
      <div className="card-body" id="playerPreferences">
        <div className="form-check form-switch">
          <input
            className="form-check-input"
            type="checkbox"
            role="switch"
            id="imageTooltips"
            checked={profile.imageTooltipPreference}
            onChange={toggleImageTooltips}
          />
          <label className="form-check-label" htmlFor="imageTooltips">
            Enable Image tooltips
          </label>
        </div>
        <div className="d-flex justify-content-start align-items-center">
          <input
            type="color"
            id="edgecolorpicker"
            style={{ width: '8%' }}
            value={profile.edgeColor ?? '#000000'}
            onChange={setEdgeColor}
          />
          <label className="form-check-label m-1" htmlFor="edgecolorpicker">
            Choose Edge Color
          </label>
        </div>
        <div className="form-check form-switch">
          <input
            className="form-check-input"
            type="checkbox"
            role="switch"
            id="enableNotifications"
            checked={profile.notificationsEnabled}
            disabled={!pushSupported || notificationsBusy}
            title={pushSupported ? undefined : 'Notifications are not supported in this browser.'}
            onChange={toggleNotifications}
          />
          <label className="form-check-label" htmlFor="enableNotifications">
            Enable notifications
          </label>
        </div>
      </div>
    </Card>
  );
}
