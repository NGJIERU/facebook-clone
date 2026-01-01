import { defineStore } from 'pinia';
import { Client } from '@stomp/stompjs';
import { ref } from 'vue';
import { useAuthStore } from './auth';

export const useNotificationStore = defineStore('notification', () => {
    const notifications = ref([]);
    const client = ref(null);
    const connected = ref(false);
    const authStore = useAuthStore();

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
                    notifications.value.unshift(notification);
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

    const disconnect = () => {
        if (client.value) {
            client.value.deactivate();
        }
    };

    return {
        notifications,
        connected,
        connect,
        disconnect
    };
});
