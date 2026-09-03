import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Switch } from '../../components/ui/Switch';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { FieldHint, InlineAlert } from '../../components/ui/FormFeedback';
import { api } from '../../api/client';
import type { Profile, SubscriptionStatus, TestSendResult } from '../../api/types';
import { showError } from '../../stores/toast';
import {
  getCurrentPushEndpoint,
  isPushSupported,
  subscribeToPush,
  unsubscribeFromPush,
} from '../../push/pushNotifications';

const SUBSCRIPTION_KEY = ['profile', 'subscription'];
const ENDPOINT_KEY = ['push', 'endpoint'];

/**
 * Turn-alert notifications, in the shape most subscription sites use: an
 * account-level master switch, then a per-browser "is this device connected"
 * control and a test-send, so a player can prove pushes reach *this* browser
 * without waiting for a real turn. "Connected" = this browser's push endpoint
 * is among the ones the server has for this account.
 */
export function NotificationsCard({
  profile,
  onSaved,
}: {
  profile: Profile;
  onSaved: (updated: Profile) => void;
}) {
  const queryClient = useQueryClient();
  const pushSupported = isPushSupported();
  const [permission, setPermission] = useState<NotificationPermission>(() =>
    pushSupported ? Notification.permission : 'denied',
  );
  const [busy, setBusy] = useState(false);
  const [testMsg, setTestMsg] = useState<{ kind: 'success' | 'danger'; text: string } | null>(null);

  const subQuery = useQuery({
    queryKey: SUBSCRIPTION_KEY,
    queryFn: () => api.get<SubscriptionStatus>('/subscription'),
    enabled: pushSupported,
  });
  const endpointQuery = useQuery({
    queryKey: ENDPOINT_KEY,
    queryFn: () => getCurrentPushEndpoint(),
    enabled: pushSupported,
  });

  const endpoints = subQuery.data?.endpoints ?? [];
  const thisEndpoint = endpointQuery.data ?? null;
  const thisBrowserConnected = !!thisEndpoint && endpoints.includes(thisEndpoint);
  const deviceCount = endpoints.length;
  const checking = subQuery.isPending || endpointQuery.isPending;

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: SUBSCRIPTION_KEY });
    queryClient.invalidateQueries({ queryKey: ENDPOINT_KEY });
  };

  /** Ensure the browser has granted permission, requesting it once if unasked. */
  const ensurePermission = async (): Promise<boolean> => {
    if (permission === 'granted') return true;
    if (permission === 'denied') return false;
    const result = await Notification.requestPermission();
    setPermission(result);
    return result === 'granted';
  };

  const toggleMaster = async (enabled: boolean) => {
    setBusy(true);
    try {
      if (enabled) {
        if (!(await ensurePermission())) return;
        await subscribeToPush();
      }
      onSaved(await api.put<Profile>('/profile/notifications', { enabled }));
      invalidate();
    } catch {
      showError(enabled ? 'Unable to enable notifications.' : 'Unable to update notifications.');
    } finally {
      setBusy(false);
    }
  };

  const connectThisBrowser = async () => {
    setBusy(true);
    try {
      if (!(await ensurePermission())) return;
      await subscribeToPush();
      invalidate();
    } catch {
      showError('Unable to connect this browser.');
    } finally {
      setBusy(false);
    }
  };

  const disconnectThisBrowser = async () => {
    setBusy(true);
    try {
      await unsubscribeFromPush();
      invalidate();
    } catch {
      showError('Unable to disconnect this browser.');
    } finally {
      setBusy(false);
    }
  };

  const sendTest = async () => {
    setTestMsg(null);
    setBusy(true);
    try {
      const result = await api.post<TestSendResult>('/subscription/test');
      if (result.sent > 0) {
        setTestMsg({ kind: 'success', text: `Sent to ${result.sent} device${result.sent === 1 ? '' : 's'}.` });
      } else {
        setTestMsg({
          kind: 'danger',
          text: result.failed > 0 ? 'No device accepted the test push.' : 'No subscribed devices to send to.',
        });
      }
      invalidate();
    } catch (e) {
      setTestMsg({ kind: 'danger', text: e instanceof Error && e.message ? e.message : 'Test send failed.' });
    } finally {
      setBusy(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Notifications</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-4">
        {!pushSupported ? (
          <p className="text-sm text-ink-muted">This browser doesn’t support push notifications.</p>
        ) : (
          <>
            <div>
              <Switch
                id="enableNotifications"
                label="Turn alerts"
                checked={profile.notificationsEnabled}
                disabled={busy || permission === 'denied'}
                onChange={(e) => void toggleMaster(e.target.checked)}
              />
              <FieldHint>
                Get a notification when it’s your turn to act. Also pings Discord if you’ve linked your Discord ID.
              </FieldHint>
            </div>

            {permission === 'denied' ? (
              <InlineAlert kind="danger">
                Notifications are blocked for this site in your browser settings. Enable them there, then reload this
                page.
              </InlineAlert>
            ) : (
              profile.notificationsEnabled && (
                <div className="flex flex-col gap-3 border-t border-line pt-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-sm text-ink">This browser</span>
                    {checking ? (
                      <Badge variant="muted">Checking…</Badge>
                    ) : thisBrowserConnected ? (
                      <Badge variant="online">Connected</Badge>
                    ) : (
                      <Badge variant="muted">Not connected</Badge>
                    )}
                    <span className="ml-auto">
                      {thisBrowserConnected ? (
                        <Button variant="secondary" size="sm" disabled={busy} onClick={() => void disconnectThisBrowser()}>
                          Disconnect
                        </Button>
                      ) : (
                        <Button
                          variant="secondary"
                          size="sm"
                          disabled={busy || checking}
                          onClick={() => void connectThisBrowser()}
                        >
                          Connect this browser
                        </Button>
                      )}
                    </span>
                  </div>

                  <div className="flex flex-col gap-2 items-start">
                    <Button
                      variant="secondary"
                      size="sm"
                      disabled={busy || !thisBrowserConnected}
                      onClick={() => void sendTest()}
                    >
                      Send test notification
                    </Button>
                    {testMsg && <InlineAlert kind={testMsg.kind}>{testMsg.text}</InlineAlert>}
                  </div>

                  {deviceCount > 0 && (
                    <p className="text-xs text-ink-muted">
                      Receiving on {deviceCount} device{deviceCount === 1 ? '' : 's'}.
                    </p>
                  )}
                </div>
              )
            )}
          </>
        )}
      </CardBody>
    </Card>
  );
}
