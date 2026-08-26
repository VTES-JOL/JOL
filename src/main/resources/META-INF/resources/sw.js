console.log('Service Worker loaded and parsed');

const NOTIFICATION_ICON = 'https://static.deckserver.net/assets/images/favicon.ico';

// Listen for the 'push' event
self.addEventListener('push', function(event) {
    console.log('[SW] Push event received:', event);

    let data = {};
    try {
        data = event.data.json();
    } catch (e) {
        data = {title: 'Your Turn', body: event.data.text()};
    }

    var options = {
        body: data.body,
        icon: NOTIFICATION_ICON,
        badge: NOTIFICATION_ICON,
        tag: 'game-notification',
        requireInteraction: false,
        vibrate: [200, 100, 200],
        data: {url: data.url || '/jol/'}
    };

    console.log('[SW] About to show notification with options:', options);

    event.waitUntil(
        self.registration.showNotification(data.title || 'Your Turn', options)
            .then(function() {
                console.log('[SW] Notification shown successfully');
            })
            .catch(function(error) {
                console.error('[SW] Error showing notification:', error);
            })
    );
});

self.addEventListener('notificationclick', function(event) {
    console.log('[SW] Notification clicked:', event);
    event.notification.close();

    const url = event.notification.data && event.notification.data.url ? event.notification.data.url : '/jol/';

    event.waitUntil(
        clients.matchAll({type: 'window', includeUncontrolled: true}).then(function (clientList) {
            for (const client of clientList) {
                if ('focus' in client) {
                    client.postMessage({type: 'notification-navigate', url});
                    return client.focus();
                }
            }
            if (clients.openWindow) {
                return clients.openWindow(url);
            }
        })
    );
});

self.addEventListener('activate', function(event) {
    console.log('[SW] Service Worker activated');
});

self.addEventListener('install', function(event) {
    console.log('[SW] Service Worker installed');
});
