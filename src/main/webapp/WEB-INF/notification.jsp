function initialiseState() {
    console.log("Initialising");

    // Check if desktop notifications are supported
    if (!('showNotification' in ServiceWorkerRegistration.prototype)) {
        console.warn('Notifications aren\'t supported.');
        return;
    }

    // Check if user has disabled notifications
    // If a user has manually disabled notifications in his/her browser for
    // your page previously, they will need to MANUALLY go in and turn the
    // permission back on. In this statement you could show some UI element
    // telling the user how to do so.
    if (Notification.permission === 'denied') {
        console.warn('The user has blocked notifications.');
        return;
    }

    // Check if push API is supported
    if (!('PushManager' in window)) {
        console.warn('Push messaging isn\'t supported.');
        return;
    }

    navigator.serviceWorker.ready.then(function (serviceWorkerRegistration) {

        console.log("Service worker ready");
        // Get the push notification subscription object
        serviceWorkerRegistration.pushManager.getSubscription().then(function (subscription) {

            // If this is the user's first visit we need to set up
            // a subscription to push notifications
            if (!subscription) {
                subscribe();
                return;
            }

            // Update the server state with the new subscription
            sendSubscriptionToServer(subscription);
        })
            .catch(function(err) {
                // Handle the error - show a notification in the GUI
                console.warn('Error during getSubscription()', err);
            });
    });
}

const vapidPublicKey =
    '<%= System.getenv("VAPID_PUBLIC_KEY") %>';

function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding)
        .replace(/-/g, '+')
        .replace(/_/g, '/');
    const rawData = atob(base64);
    return Uint8Array.from([...rawData].map(ch => ch.charCodeAt(0)));
}

function subscribe() {
    navigator.serviceWorker.ready.then(function (serviceWorkerRegistration) {

        // Contact the third party push server. Which one is contacted by
        // pushManager is configured internally in the browser, so we don't
        // need to worry about browser differences here.
        //
        // When .subscribe() is invoked, a notification will be shown in the
        // user's browser, asking the user to accept push notifications from
        // <yoursite.com>. This is why it is async and requires a catch.
        serviceWorkerRegistration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(vapidPublicKey)
        }).then(function (subscription) {
            // Update the server state with the new subscription
            return sendSubscriptionToServer(subscription);
        })
            .catch(function (e) {
                if (Notification.permission === 'denied') {
                    console.warn('Permission for Notifications was denied');
                } else {
                    console.error('Unable to subscribe to push.', e);
                }
            });
    });
}

function sendSubscriptionToServer(subscription) {
    // Get public key and user auth from the subscription object
    const key = subscription.getKey ? subscription.getKey('p256dh') : '';
    const auth = subscription.getKey ? subscription.getKey('auth') : '';

    const subscriptionData = {
        endpoint: subscription.endpoint,
        key: key ? btoa(String.fromCharCode.apply(null, new Uint8Array(key))) : '',
        auth: auth ? btoa(String.fromCharCode.apply(null, new Uint8Array(auth))) : ''
    };

    console.log('Sending subscription data:', subscriptionData);

    localStorage.setItem("notifications-subscribed", "true");
    return fetch('/jol/api/subscription', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(subscriptionData)
    });
}

async function enableNotifications() {
    // Request notification permission from user
    const permission = await Notification.requestPermission();

    if (permission !== 'granted') {
        alert('You need to allow notifications to enable push messages.');
        return;
    }

    // Register service worker if not already done
    const registration = await navigator.serviceWorker.register('sw.js');
    console.log('Service worker registered:', registration);

    // Subscribe user to push
    const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(vapidPublicKey)
    });

    console.log('Push subscription:', subscription);

    // Send subscription to server
    await fetch('/jol/api/subscription', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(subscription)
    });

    alert('Notifications enabled successfully!');
}