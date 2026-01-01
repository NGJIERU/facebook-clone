import { defineStore } from 'pinia';
import { Client } from '@stomp/stompjs';
import { ref, computed } from 'vue';
import { useAuthStore } from './auth';
import api from '../utils/api';

export const useNotificationStore = defineStore('notification', () => {
    const notifications = ref([]);
    const client = ref(null);
    const connected = ref(false);
    const authStore = useAuthStore();

    const history = ref([]);
    const unreadCount = computed(() => history.value.filter(n => !n.read).length);

    const connect = () => {
        if (client.value && client.value.active) return;

        const userId = authStore.user?.id;
        // NotificationService sends to recipientId which is likely the email/username from the JWT.
        // authStore.user.username is set to email in auth.js login()
        const recipientId = authStore.user?.username || authStore.user?.email;

        if (!recipientId) {
            console.error("Cannot connect to WebSocket: Missing user ID/email");
            return;
        }

        // Fetch initial history
        fetchNotifications(recipientId);

        console.log("Connecting to WebSocket for user:", recipientId);

        client.value = new Client({
            brokerURL: 'ws://localhost:5173/ws', // Connect to standard WebSocket endpoint
            onConnect: () => {
                connected.value = true;
                console.log('Connected to WebSocket');

                // Subscribe to user specific notifications
                client.value.subscribe(`/topic/notifications/${recipientId}`, (message) => {
                    const notification = JSON.parse(message.body);
                    console.log("Received notification:", notification);

                    // Add to toasts
                    notifications.value.unshift(notification);

                    // Add to history (newest first)
                    history.value.unshift(notification);

                    // Auto-remove toast after 5 seconds
                    setTimeout(() => {
                        const index = notifications.value.indexOf(notification);
                        if (index > -1) {
                            notifications.value.splice(index, 1);
                        }
                    }, 5000);
                });
            },
            onDisconnect: () => {
                connected.value = false;
                console.log('Disconnected from WebSocket');
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            }
        });

        client.value.activate();
    };

    const fetchNotifications = async (userId) => {
        try {
            // Note: In real app, userId should come from token, but current backend requires param
            const response = await api.get('/notifications', { params: { userId } });
            history.value = response.data;
        } catch (error) {
            console.error("Failed to fetch notifications:", error);
        }
    };

    const markAsRead = async (id) => {
        try {
            await api.put(`/notifications/${id}/read`);
            const notification = history.value.find(n => n.id === id);
            if (notification) {
                notification.read = true;
            }
        } catch (error) {
            console.error("Failed to mark notification as read:", error);
        }
    };

    const disconnect = () => {
        if (client.value) {
            client.value.deactivate();
        }
    };

    return {
        notifications,
        history,
        unreadCount,
        connected,
        connect,
        disconnect,
        fetchNotifications,
        markAsRead,
        // Expose api for direct calls if needed, or keep encapsulated
    };
});
