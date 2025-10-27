console.log('Service Worker loaded and parsed');

// Listen for the 'push' event
self.addEventListener('push', function(event) {
    console.log('[SW] Push event received:', event);

    var data = event.data.text();
    var title = 'Your Turn';
    var options = {
        body: data,
        icon: '/jol/images/icon.png',
        badge: '/jol/images/badge.png',
        tag: 'game-notification',
        requireInteraction: false,
        vibrate: [200, 100, 200]
    };

    console.log('[SW] About to show notification with options:', options);

    event.waitUntil(
        self.registration.showNotification(title, options)
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
});

self.addEventListener('activate', function(event) {
    console.log('[SW] Service Worker activated');
});

self.addEventListener('install', function(event) {
    console.log('[SW] Service Worker installed');
});