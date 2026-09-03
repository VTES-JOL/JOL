import { api } from '../api/client';
import { getVapidPublicKey } from '../api/config';

// Ported from ds.js's registerServiceWorker/subscribeToPush/unsubscribeFromPush
// — same /jol/sw.js worker, same /subscription REST endpoints (NotificationResource).
let swRegistration: ServiceWorkerRegistration | null = null;

function urlBase64ToUint8Array(base64String: string): BufferSource {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = atob(base64);
  return Uint8Array.from([...rawData].map((ch) => ch.charCodeAt(0))).buffer;
}

export function isPushSupported(): boolean {
  return 'Notification' in window && 'serviceWorker' in navigator && 'PushManager' in window;
}

function registerServiceWorker(): Promise<ServiceWorkerRegistration | null> {
  if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
    return Promise.resolve(null);
  }
  if (swRegistration) return Promise.resolve(swRegistration);
  return navigator.serviceWorker
    .register('/jol/sw.js')
    .then((registration) => {
      swRegistration = registration;
      navigator.serviceWorker.addEventListener('message', (event) => {
        // Full navigation rather than a React Router push — correct
        // regardless of whether the clicked notification's target view has
        // been converted to React yet, same as any other plain <a href>.
        if (event.data && event.data.type === 'notification-navigate' && event.data.url) {
          window.location.assign(event.data.url);
        }
      });
      return registration;
    })
    .catch((err) => {
      console.warn('Service worker registration failed', err);
      return null;
    });
}

/**
 * The push endpoint this browser is currently subscribed with, or null if it
 * isn't subscribed / push isn't supported. Compared against the server's
 * endpoint list to tell whether "this browser" is connected.
 */
export function getCurrentPushEndpoint(): Promise<string | null> {
  if (!isPushSupported()) return Promise.resolve(null);
  return registerServiceWorker()
    .then((registration) => (registration ? registration.pushManager.getSubscription() : null))
    .then((subscription) => subscription?.endpoint ?? null)
    .catch(() => null);
}

export function subscribeToPush(): Promise<PushSubscription> {
  return registerServiceWorker()
    .then((registration) => {
      if (!registration) throw new Error('Push messaging is not supported in this browser.');
      return getVapidPublicKey().then((vapidPublicKey) => {
        if (!vapidPublicKey) throw new Error('Push messaging is not configured on this server.');
        return registration.pushManager
          .getSubscription()
          .then(
            (existing) =>
              existing ??
              registration.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: urlBase64ToUint8Array(vapidPublicKey),
              }),
          );
      });
    })
    .then((subscription) => {
      const key = subscription.getKey('p256dh');
      const auth = subscription.getKey('auth');
      return api
        .post('/subscription', {
          endpoint: subscription.endpoint,
          key: key ? btoa(String.fromCharCode(...new Uint8Array(key))) : '',
          auth: auth ? btoa(String.fromCharCode(...new Uint8Array(auth))) : '',
        })
        .then(() => subscription);
    });
}

export function unsubscribeFromPush(): Promise<void> {
  return registerServiceWorker()
    .then((registration) => (registration ? registration.pushManager.getSubscription() : null))
    .then((subscription) => {
      if (!subscription) return;
      const endpoint = subscription.endpoint;
      return subscription
        .unsubscribe()
        .then(() => api.del('/subscription', { endpoint }))
        .then(() => undefined);
    });
}
