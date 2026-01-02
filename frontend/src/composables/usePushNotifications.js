import { ref } from 'vue';

const isSupported = ref('Notification' in window);
const permission = ref(isSupported.value ? Notification.permission : 'denied');

export function usePushNotifications() {
    const requestPermission = async () => {
        if (!isSupported.value) {
            console.warn('Push notifications are not supported in this browser');
            return false;
        }

        try {
            const result = await Notification.requestPermission();
            permission.value = result;
            return result === 'granted';
        } catch (error) {
            console.error('Error requesting notification permission:', error);
            return false;
        }
    };

    const showNotification = (title, options = {}) => {
        if (!isSupported.value || permission.value !== 'granted') {
            console.warn('Cannot show notification - not supported or not permitted');
            return null;
        }

        const defaultOptions = {
            icon: '/favicon.ico',
            badge: '/favicon.ico',
            vibrate: [200, 100, 200],
            requireInteraction: false,
            ...options
        };

        try {
            const notification = new Notification(title, defaultOptions);
            
            notification.onclick = () => {
                window.focus();
                notification.close();
                if (options.onClick) {
                    options.onClick();
                }
            };

            // Auto-close after 5 seconds if not requiring interaction
            if (!defaultOptions.requireInteraction) {
                setTimeout(() => notification.close(), 5000);
            }

            return notification;
        } catch (error) {
            console.error('Error showing notification:', error);
            return null;
        }
    };

    // Notification types
    const notifyNewMessage = (senderName, messagePreview) => {
        return showNotification(`New message from ${senderName}`, {
            body: messagePreview,
            tag: 'new-message',
        });
    };

    const notifyFriendRequest = (senderName) => {
        return showNotification('New Friend Request', {
            body: `${senderName} sent you a friend request`,
            tag: 'friend-request',
        });
    };

    const notifyPostLike = (userName, postPreview) => {
        return showNotification(`${userName} liked your post`, {
            body: postPreview?.substring(0, 50) || 'Your post',
            tag: 'post-like',
        });
    };

    const notifyComment = (userName, commentPreview) => {
        return showNotification(`${userName} commented on your post`, {
            body: commentPreview?.substring(0, 50) || '',
            tag: 'comment',
        });
    };

    const notifyEventReminder = (eventName, eventTime) => {
        return showNotification('Event Reminder', {
            body: `${eventName} is starting ${eventTime}`,
            tag: 'event-reminder',
            requireInteraction: true,
        });
    };

    return {
        isSupported,
        permission,
        requestPermission,
        showNotification,
        notifyNewMessage,
        notifyFriendRequest,
        notifyPostLike,
        notifyComment,
        notifyEventReminder,
    };
}
